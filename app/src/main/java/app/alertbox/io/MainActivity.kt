package app.alertbox.io

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.alertbox.io.ui.AlertBoxApp
import app.alertbox.io.ui.AppViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val notificationsGrantedState = mutableStateOf(false)

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.Factory(application as AlertBoxApplication)
    }

    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsGrantedState.value = granted
        if (!granted) viewModel.showError("Las notificaciones están desactivadas. Puedes habilitarlas en Ajustes de Android.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        notificationsGrantedState.value = notificationsGranted()
        handleIntent(intent)
        setContent {
            AlertBoxApp(
                viewModel = viewModel,
                notificationsGranted = notificationsGrantedState.value,
                onRequestNotifications = ::requestNotificationPermission,
                onGoogleSignIn = ::launchGoogleSignIn,
                onAppleSignIn = { viewModel.signInWithApple(this) },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    @SuppressLint("DiscouragedApi") // Firebase genera este recurso solo al procesar google-services.json.
    private fun launchGoogleSignIn() {
        val resourceId = resources.getIdentifier("default_web_client_id", "string", packageName)
        val webClientId = resourceId.takeIf { it != 0 }?.let(::getString).orEmpty()
        if (webClientId.isBlank()) {
            viewModel.showError("Google Sign-In estará disponible cuando se añada google-services.json.")
            return
        }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        lifecycleScope.launch {
            try {
                val result = CredentialManager.create(this@MainActivity).getCredential(this@MainActivity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    viewModel.signInWithGoogle(token)
                } else {
                    viewModel.showError("Google no ha devuelto una credencial válida.")
                }
            } catch (_: NoCredentialException) {
                viewModel.showError("No hay ninguna cuenta de Google disponible en este dispositivo.")
            } catch (_: Exception) {
                viewModel.showError("No se pudo completar el acceso con Google.")
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        val fromPush = intent?.getStringExtra(EXTRA_NOTIFICATION_ID)
        val fromLink = notificationIdFrom(intent?.data)
        viewModel.openNotification(fromPush ?: fromLink)
    }

    private fun notificationIdFrom(uri: Uri?): String? {
        if (uri == null) return null
        if (uri.scheme == "alertbox") return uri.getQueryParameter("notificationId")
        val segments = uri.pathSegments
        return if (segments.size >= 2 && segments[0] == "inbox") segments[1] else null
    }

    private fun notificationsGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        const val EXTRA_NOTIFICATION_ID = "notificationId"
    }
}
