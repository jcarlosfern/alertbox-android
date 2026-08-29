package app.alertbox.io.ui

import android.app.Activity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.alertbox.io.ui.screens.AuthenticationScreen
import app.alertbox.io.ui.screens.FirebaseConfigurationScreen
import app.alertbox.io.ui.screens.MainShell
import app.alertbox.io.ui.screens.OnboardingScreen
import app.alertbox.io.ui.screens.ProfileSetupScreen
import app.alertbox.io.ui.screens.VerifyEmailScreen
import app.alertbox.io.ui.theme.AlertBoxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertBoxApp(
    viewModel: AppViewModel,
    notificationsGranted: Boolean,
    onRequestNotifications: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.error, state.message) {
        val feedback = state.error ?: state.message
        if (feedback != null) {
            snackbar.showSnackbar(feedback)
            viewModel.consumeFeedback()
        }
    }

    AlertBoxTheme(darkTheme = state.darkMode ?: androidx.compose.foundation.isSystemInDarkTheme()) {
        Scaffold(snackbarHost = { SnackbarHost(snackbar) }) { padding ->
            when (val session = state.session) {
            AppSession.Loading -> app.alertbox.io.ui.components.LoadingState("Preparando tu AlertBox…")
            AppSession.Unconfigured -> FirebaseConfigurationScreen()
            AppSession.Onboarding -> OnboardingScreen(onFinished = viewModel::finishOnboarding)
            AppSession.SignedOut -> AuthenticationScreen(
                contentPadding = padding,
                working = state.working,
                onSignIn = viewModel::signIn,
                onSignUp = viewModel::createAccount,
                onReset = viewModel::resetPassword,
                onGoogleSignIn = onGoogleSignIn,
                onAppleSignIn = onAppleSignIn,
            )
            is AppSession.VerifyEmail -> VerifyEmailScreen(
                email = session.email,
                working = state.working,
                onRefresh = viewModel::refreshVerification,
                onResend = viewModel::resendVerification,
                onSignOut = viewModel::signOut,
            )
            AppSession.ProfileSetup -> ProfileSetupScreen(viewModel = viewModel, working = state.working, onComplete = viewModel::completeProfile)
            AppSession.Ready -> MainShell(
                state = state,
                viewModel = viewModel,
                notificationsGranted = notificationsGranted,
                onRequestNotifications = onRequestNotifications,
            )
            }
        }
    }
}
