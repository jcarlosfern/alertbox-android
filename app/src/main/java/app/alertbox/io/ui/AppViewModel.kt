package app.alertbox.io.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.alertbox.io.AlertBoxApplication
import app.alertbox.io.core.auth.AuthStatus
import app.alertbox.io.core.model.AlertNotification
import app.alertbox.io.core.model.CitySuggestion
import app.alertbox.io.core.model.Giveaway
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.PreferencesUpdate
import app.alertbox.io.core.model.ProfileUpdate
import app.alertbox.io.core.model.Promotion
import app.alertbox.io.core.model.Redemption
import app.alertbox.io.core.model.SavedItem
import app.alertbox.io.core.model.Survey
import app.alertbox.io.core.model.UserProfile
import app.alertbox.io.core.model.UserPreferences
import app.alertbox.io.data.DashboardData
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.TimeZone

sealed interface AppSession {
    data object Loading : AppSession
    data object Unconfigured : AppSession
    data object Onboarding : AppSession
    data object SignedOut : AppSession
    data class VerifyEmail(val email: String) : AppSession
    data object ProfileSetup : AppSession
    data object Ready : AppSession
}

data class AppUiState(
    val session: AppSession = AppSession.Loading,
    val profile: UserProfile? = null,
    val dashboard: DashboardData = DashboardData(),
    val working: Boolean = false,
    val refreshing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val pendingNotificationId: String? = null,
    val darkMode: Boolean? = null,
)

class AppViewModel(private val app: AlertBoxApplication) : ViewModel() {
    private val auth = app.authRepository
    private val repository = app.repository
    private val _state = MutableStateFlow(AppUiState(darkMode = repository.darkMode))
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            auth.status.collectLatest { status ->
                when (status) {
                    AuthStatus.Loading -> _state.update { it.copy(session = AppSession.Loading) }
                    AuthStatus.Unconfigured -> _state.update { it.copy(session = AppSession.Unconfigured) }
                    AuthStatus.SignedOut -> _state.update {
                        it.copy(
                            session = if (repository.hasSeenOnboarding) AppSession.SignedOut else AppSession.Onboarding,
                            profile = null,
                            dashboard = DashboardData(),
                        )
                    }
                    is AuthStatus.SignedIn -> {
                        if (!status.user.emailVerified) {
                            _state.update { it.copy(session = AppSession.VerifyEmail(status.user.email.orEmpty())) }
                        } else {
                            bootstrap()
                        }
                    }
                }
            }
        }
    }

    fun finishOnboarding() {
        repository.hasSeenOnboarding = true
        _state.update { it.copy(session = AppSession.SignedOut) }
    }

    fun signIn(email: String, password: String) = launchWorking { auth.signIn(email, password) }
    fun createAccount(email: String, password: String) = launchWorking { auth.createAccount(email, password) }
    fun signInWithGoogle(idToken: String) = launchWorking { auth.signInWithGoogle(idToken) }
    fun signInWithApple(activity: Activity) = launchWorking { auth.signInWithApple(activity) }

    fun resetPassword(email: String) = launchWorking(success = "Te hemos enviado las instrucciones.") {
        auth.resetPassword(email)
    }

    fun resendVerification() = launchWorking(success = "Correo de verificación enviado.") {
        auth.resendVerification()
    }

    fun refreshVerification() = launchWorking {
        if (!auth.refreshVerification()) throw IllegalStateException("El correo todavía no está verificado.")
        bootstrap()
    }

    fun signOut() = launchWorking { auth.signOut() }
    fun deleteAccount() = launchWorking { auth.deleteAccount() }

    fun completeProfile(update: ProfileUpdate, interests: List<String>) = launchWorking {
        var profile = repository.updateProfile(update)
        val current = profile.preferences ?: UserPreferences()
        profile = repository.updatePreferences(
            PreferencesUpdate(
                pushEnabled = current.pushEnabled,
                emailEnabled = current.emailEnabled,
                privateMessagesEnabled = current.privateMessagesEnabled,
                marketingEnabled = current.marketingEnabled,
                quietHours = current.quietHours,
                travelMode = current.travelMode,
                interests = interests,
            ),
        )
        _state.update { it.copy(profile = profile, session = AppSession.Ready) }
        refresh()
    }

    fun updateProfile(update: ProfileUpdate) = launchWorking(success = "Datos personales guardados.") {
        val profile = repository.updateProfile(update)
        _state.update { it.copy(profile = profile) }
    }

    fun updateLocale(locale: String) = launchWorking(success = "Idioma guardado.") {
        val current = requireNotNull(_state.value.profile) { "No se ha podido cargar el perfil." }
        val profile = repository.updateProfile(
            ProfileUpdate(
                firstName = current.firstName.orEmpty(),
                lastName = current.lastName.orEmpty(),
                displayName = current.displayName.orEmpty(),
                city = current.city.orEmpty(),
                countryCode = current.countryCode ?: "ES",
                gender = current.gender.orEmpty(),
                birthDate = current.birthDate.orEmpty(),
                locale = locale,
                timezone = TimeZone.getDefault().id,
                onboardingComplete = current.onboardingComplete,
            ),
        )
        _state.update { it.copy(profile = profile) }
    }

    fun setDarkMode(enabled: Boolean) {
        repository.darkMode = enabled
        _state.update { it.copy(darkMode = enabled) }
    }

    fun updatePreferences(update: PreferencesUpdate, notificationsGranted: Boolean) = launchWorking(success = "Preferencias guardadas.") {
        if (update.pushEnabled && !notificationsGranted) {
            throw IllegalStateException("Concede permiso de notificaciones para activar los avisos push.")
        }
        val profile = repository.updatePreferences(update)
        if (update.pushEnabled) repository.registerCurrentDevice() else repository.unregisterCurrentDevice()
        _state.update { it.copy(profile = profile) }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(refreshing = true, error = null) }
            runCatching { repository.dashboard() }
                .onSuccess { dashboard -> _state.update { it.copy(dashboard = dashboard) } }
                .onFailure { error -> if (error !is CancellationException) setError(error) }
            _state.update { it.copy(refreshing = false) }
        }
    }

    fun setFollowing(id: String, followed: Boolean) = launchWorking {
        repository.setFollowing(id, followed)
        refresh()
    }

    fun setChannel(id: String, subscribed: Boolean) = launchWorking {
        repository.setChannel(id, subscribed)
    }

    fun markNotification(id: String, state: String, enabled: Boolean) = launchWorking {
        repository.setNotificationState(id, state, enabled)
        refresh()
    }

    fun markAllRead() = launchWorking { repository.markAllRead(); refresh() }
    fun claimPromotion(id: String, onCode: (String) -> Unit) = launchWorking {
        onCode(repository.claimPromotion(id).redemptionCode)
        refresh()
    }
    fun setGiveawayEntry(id: String, entered: Boolean) = launchWorking {
        repository.setGiveawayEntry(id, entered)
        refresh()
    }
    fun submitSurvey(id: String, answers: Map<String, Any>, onSuccess: () -> Unit = {}) = launchWorking(success = "Respuestas enviadas.") {
        repository.submitSurvey(id, answers)
        refresh()
        onSuccess()
    }
    fun joinLoyalty(id: String) = launchWorking { repository.joinLoyalty(id); refresh() }
    fun redeemReward(id: String, onRedemption: (Redemption) -> Unit) = launchWorking {
        onRedemption(repository.redeemReward(id))
        refresh()
    }
    fun setSaved(type: String, id: String, saved: Boolean) = launchWorking {
        repository.setSaved(type, id, saved)
        refresh()
    }

    suspend fun loadNotification(id: String): AlertNotification? = load { repository.notification(id) }
    suspend fun loadPromotion(id: String): Promotion? = load { repository.promotion(id) }
    suspend fun loadGiveaway(id: String): Giveaway? = load { repository.giveaway(id) }
    suspend fun loadSurvey(id: String): Survey? = load { repository.survey(id) }
    suspend fun loadLoyalty(id: String): LoyaltyProgram? = load { repository.loyaltyProgram(id) }
    suspend fun loadOrganization(id: String): Organization? = load { repository.organization(id) }
    suspend fun loadSaved(): List<SavedItem> = load { repository.saved() } ?: emptyList()
    suspend fun searchCities(query: String): List<CitySuggestion> = repository.searchCities(
        query = query,
        language = if (java.util.Locale.getDefault().language == "es") "es" else "en",
    )

    fun openNotification(id: String?) {
        if (!id.isNullOrBlank()) _state.update { it.copy(pendingNotificationId = id) }
    }

    fun notificationOpened() {
        _state.update { it.copy(pendingNotificationId = null) }
    }

    fun showError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun consumeFeedback() {
        _state.update { it.copy(error = null, message = null) }
    }

    private suspend fun bootstrap() {
        _state.update { it.copy(session = AppSession.Loading, error = null) }
        runCatching {
            repository.syncAccount()
            repository.profile()
        }
            .onSuccess { profile ->
                _state.update {
                    it.copy(
                        profile = profile,
                        session = if (profile.onboardingComplete) AppSession.Ready else AppSession.ProfileSetup,
                    )
                }
                if (profile.onboardingComplete) {
                    refresh()
                    if (profile.preferences?.pushEnabled == true) {
                        viewModelScope.launch { runCatching { repository.registerCurrentDevice() } }
                    }
                }
            }
            .onFailure { error ->
                setError(error)
                _state.update { it.copy(session = AppSession.Ready) }
            }
    }

    private fun launchWorking(success: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(working = true, error = null, message = null) }
            runCatching { block() }
                .onSuccess { _state.update { it.copy(message = success) } }
                .onFailure { error -> if (error !is CancellationException) setError(error) }
            _state.update { it.copy(working = false) }
        }
    }

    private suspend fun <T> load(block: suspend () -> T): T? = runCatching { block() }
        .onFailure { error -> if (error !is CancellationException) setError(error) }
        .getOrNull()

    private fun setError(error: Throwable) {
        val message = when (error) {
            is FirebaseAuthException -> when (error.errorCode) {
                "ERROR_INVALID_EMAIL" -> "El correo no es válido."
                "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> "El correo o la contraseña no son correctos."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Ya existe una cuenta con ese correo."
                "ERROR_TOO_MANY_REQUESTS" -> "Demasiados intentos. Espera unos minutos."
                else -> "No se pudo completar la autenticación."
            }
            else -> error.message ?: "No se pudo completar la operación."
        }
        _state.update { it.copy(error = message) }
    }

    class Factory(private val app: AlertBoxApplication) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(app) as T
    }
}
