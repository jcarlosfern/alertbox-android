package app.alertbox.io.core.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import app.alertbox.io.data.AlertBoxRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AuthenticatedUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val emailVerified: Boolean,
)

sealed interface AuthStatus {
    data object Loading : AuthStatus
    data object Unconfigured : AuthStatus
    data object SignedOut : AuthStatus
    data class SignedIn(val user: AuthenticatedUser) : AuthStatus
}

class AuthRepository(
    private val context: Context,
    private val auth: FirebaseAuth?,
    private val repository: AlertBoxRepository,
) {
    val status: Flow<AuthStatus> = callbackFlow {
        if (auth == null) {
            trySend(AuthStatus.Unconfigured)
            close()
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            trySend(if (user == null) AuthStatus.SignedOut else AuthStatus.SignedIn(user.toUser()))
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    val currentUser: FirebaseUser? get() = auth?.currentUser

    suspend fun signIn(email: String, password: String) {
        requireAuth().signInWithEmailAndPassword(email.trim(), password).await()
        repository.syncAccount()
    }

    suspend fun createAccount(email: String, password: String) {
        PasswordPolicy.validate(password)?.let { throw IllegalArgumentException(it) }
        val result = requireAuth().createUserWithEmailAndPassword(email.trim(), password).await()
        result.user?.sendEmailVerification()?.await()
        repository.syncAccount()
    }

    suspend fun signInWithGoogle(idToken: String) {
        requireAuth().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        repository.syncAccount()
    }

    suspend fun signInWithApple(activity: Activity) {
        val provider = OAuthProvider.newBuilder("apple.com").apply {
            scopes = listOf("email", "name")
        }
        requireAuth().startActivityForSignInWithProvider(activity, provider.build()).await()
        repository.syncAccount()
    }

    suspend fun resetPassword(email: String) {
        requireAuth().sendPasswordResetEmail(email.trim()).await()
    }

    suspend fun resendVerification() {
        requireNotNull(requireAuth().currentUser) { "No hay una sesión activa." }
            .sendEmailVerification().await()
    }

    suspend fun refreshVerification(): Boolean {
        val user = requireNotNull(requireAuth().currentUser) { "No hay una sesión activa." }
        user.reload().await()
        user.getIdToken(true).await()
        if (user.isEmailVerified) repository.syncAccount()
        return user.isEmailVerified
    }

    suspend fun signOut() {
        repository.unregisterCurrentDevice()
        requireAuth().signOut()
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    suspend fun deleteAccount() {
        val user = requireNotNull(requireAuth().currentUser) { "No hay una sesión activa." }
        val elapsed = System.currentTimeMillis() - user.metadata?.lastSignInTimestamp.orZero()
        require(elapsed in 0..RECENT_AUTH_MILLIS) {
            "Por seguridad, vuelve a iniciar sesión antes de eliminar la cuenta."
        }
        repository.unregisterCurrentDevice()
        repository.deleteProfile()
        user.delete().await()
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private fun requireAuth(): FirebaseAuth = requireNotNull(auth) {
        "Firebase todavía no está configurado para esta compilación."
    }

    private fun FirebaseUser.toUser() = AuthenticatedUser(uid, email, displayName, isEmailVerified)
    private fun Long?.orZero(): Long = this ?: 0L

    private companion object {
        const val RECENT_AUTH_MILLIS = 5 * 60 * 1000L
    }
}

object PasswordPolicy {
    const val MIN_LENGTH = 12
    const val MAX_LENGTH = 128

    fun validate(password: String): String? = when {
        password.length < MIN_LENGTH -> "Usa una frase de al menos $MIN_LENGTH caracteres."
        password.length > MAX_LENGTH -> "La contraseña es demasiado larga."
        else -> null
    }
}
