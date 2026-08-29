package app.alertbox.io.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import app.alertbox.io.ui.AppUiState
import app.alertbox.io.ui.AppViewModel

object Routes {
    const val HOME = "home"
    const val INBOX = "inbox"
    const val LOYALTY = "loyalty"
    const val ORGANIZATIONS = "organizations"
    const val SETTINGS = "settings"
    const val SURVEYS = "surveys"
    const val SAVED = "saved"
    const val NOTIFICATION = "notification/{id}"
    const val PROMOTION = "promotion/{id}"
    const val GIVEAWAY = "giveaway/{id}"
    const val SURVEY = "survey/{id}"
    const val ORGANIZATION = "organization/{id}"
    const val LOYALTY_DETAIL = "loyalty/{id}"
    const val NOTIFICATION_SETTINGS = "settings/notifications"
    const val PROFILE_SETTINGS = "settings/profile"
    const val LANGUAGE_SETTINGS = "settings/language"
    const val PRIVACY_SETTINGS = "settings/privacy"
    const val HELP = "settings/help"
    const val LEGAL = "settings/legal"

    fun notification(id: String) = "notification/$id"
    fun promotion(id: String) = "promotion/$id"
    fun giveaway(id: String) = "giveaway/$id"
    fun survey(id: String) = "survey/$id"
    fun organization(id: String) = "organization/$id"
    fun loyalty(id: String) = "loyalty/$id"
}

private data class MainDestination(val route: String, val label: String, val icon: ImageVector)

@Composable
fun MainShell(
    state: AppUiState,
    viewModel: AppViewModel,
    notificationsGranted: Boolean,
    onRequestNotifications: () -> Unit,
) {
    val nav = rememberNavController()
    val destinations = listOf(
        MainDestination(Routes.HOME, "Inicio", Icons.Default.Home),
        MainDestination(Routes.INBOX, "Bandeja", Icons.Default.Inbox),
        MainDestination(Routes.LOYALTY, "Fidelización", Icons.Default.Star),
        MainDestination(Routes.ORGANIZATIONS, "Organizaciones", Icons.Default.Business),
        MainDestination(Routes.SETTINGS, "Ajustes", Icons.Default.Settings),
    )
    val entry by nav.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val showBar = currentRoute in destinations.map { it.route }

    LaunchedEffect(state.pendingNotificationId) {
        state.pendingNotificationId?.let { id ->
            nav.navigate(Routes.notification(id))
            viewModel.notificationOpened()
        }
    }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                nav.navigate(destination.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = nav, startDestination = Routes.HOME) {
            composable(Routes.HOME) { HomeScreen(state, padding, nav, viewModel) }
            composable(Routes.INBOX) { InboxScreen(state, padding, nav, viewModel) }
            composable(Routes.LOYALTY) { LoyaltyScreen(state, padding, nav) }
            composable(Routes.ORGANIZATIONS) { OrganizationsScreen(state, padding, nav, viewModel) }
            composable(Routes.SETTINGS) { SettingsScreen(state, padding, nav, viewModel) }
            composable(Routes.SURVEYS) { SurveysScreen(state, nav) }
            composable(Routes.SAVED) { SavedScreen(nav, viewModel) }
            detail(Routes.NOTIFICATION) { id -> NotificationDetailScreen(id, nav, viewModel) }
            detail(Routes.PROMOTION) { id -> PromotionDetailScreen(id, nav, viewModel) }
            detail(Routes.GIVEAWAY) { id -> GiveawayDetailScreen(id, nav, state, viewModel) }
            detail(Routes.SURVEY) { id -> SurveyDetailScreen(id, nav, viewModel) }
            detail(Routes.ORGANIZATION) { id -> OrganizationDetailScreen(id, nav, viewModel) }
            detail(Routes.LOYALTY_DETAIL) { id -> LoyaltyDetailScreen(id, nav, viewModel) }
            composable(Routes.NOTIFICATION_SETTINGS) {
                NotificationPreferencesScreen(state, nav, viewModel, notificationsGranted, onRequestNotifications)
            }
            composable(Routes.PROFILE_SETTINGS) { PersonalDataScreen(state, nav, viewModel) }
            composable(Routes.LANGUAGE_SETTINGS) { LanguageScreen(state, nav, viewModel) }
            composable(Routes.PRIVACY_SETTINGS) { PrivacyScreen(nav, viewModel) }
            composable(Routes.HELP) { HelpScreen(nav) }
            composable(Routes.LEGAL) { LegalScreen(nav) }
        }
    }
}

private fun androidx.navigation.NavGraphBuilder.detail(
    route: String,
    content: @Composable (String) -> Unit,
) {
    composable(route, arguments = listOf(navArgument("id") { type = NavType.StringType })) { entry ->
        content(entry.arguments?.getString("id").orEmpty())
    }
}
