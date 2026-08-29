package app.alertbox.io.data

import android.content.Context
import androidx.core.content.edit
import app.alertbox.io.core.model.AlertNotification
import app.alertbox.io.core.model.CitySuggestion
import app.alertbox.io.core.model.DeviceRegistration
import app.alertbox.io.core.model.EmptyRequest
import app.alertbox.io.core.model.Giveaway
import app.alertbox.io.core.model.InvitationRequest
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.PreferencesUpdate
import app.alertbox.io.core.model.ProfileUpdate
import app.alertbox.io.core.model.Promotion
import app.alertbox.io.core.model.PromotionRedemption
import app.alertbox.io.core.model.Redemption
import app.alertbox.io.core.model.SavedItem
import app.alertbox.io.core.model.SearchItem
import app.alertbox.io.core.model.Survey
import app.alertbox.io.core.model.SurveyAnswers
import app.alertbox.io.core.model.UserProfile
import app.alertbox.io.core.network.AlertBoxApi
import app.alertbox.io.core.network.unwrap
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.TimeZone

data class DashboardData(
    val notifications: List<AlertNotification> = emptyList(),
    val promotions: List<Promotion> = emptyList(),
    val giveaways: List<Giveaway> = emptyList(),
    val surveys: List<Survey> = emptyList(),
    val loyalty: List<LoyaltyProgram> = emptyList(),
    val organizations: List<Organization> = emptyList(),
)

class AlertBoxRepository(
    context: Context,
    private val api: AlertBoxApi,
    private val firebaseConfigured: Boolean,
) {
    private val preferences = context.getSharedPreferences("alertbox_local", Context.MODE_PRIVATE)

    var hasSeenOnboarding: Boolean
        get() = preferences.getBoolean("has_seen_onboarding", false)
        set(value) { preferences.edit { putBoolean("has_seen_onboarding", value) } }

    var darkMode: Boolean?
        get() = if (preferences.contains("dark_mode")) preferences.getBoolean("dark_mode", false) else null
        set(value) {
            preferences.edit {
                if (value == null) remove("dark_mode") else putBoolean("dark_mode", value)
            }
        }

    suspend fun syncAccount(): UserProfile = api.syncAccount(EmptyRequest).unwrap()
    suspend fun profile(): UserProfile = api.profile().unwrap()
    suspend fun updateProfile(update: ProfileUpdate): UserProfile = api.updateProfile(update).unwrap()
    suspend fun deleteProfile() = api.deleteProfile().unwrap()
    suspend fun updatePreferences(update: PreferencesUpdate): UserProfile = api.updatePreferences(update).unwrap()
    suspend fun searchCities(query: String, language: String): List<CitySuggestion> =
        api.searchCities(query, language).unwrap().results

    suspend fun dashboard(): DashboardData {
        val notifications = runCatching { api.feed().unwrap() }.getOrDefault(emptyList())
        val promotions = runCatching { api.promotions().unwrap() }.getOrDefault(emptyList())
        val giveaways = runCatching { api.giveaways().unwrap() }.getOrDefault(emptyList())
        val surveys = runCatching { api.surveys().unwrap() }.getOrDefault(emptyList())
        val loyalty = runCatching { api.loyaltyPrograms().unwrap() }.getOrDefault(emptyList())
        val organizations = runCatching { api.organizations().unwrap() }.getOrDefault(emptyList())
        if (listOf(notifications, promotions, giveaways, surveys, loyalty, organizations).all { it.isEmpty() }) {
            api.profile().unwrap()
        }
        return DashboardData(notifications, promotions, giveaways, surveys, loyalty, organizations)
    }

    suspend fun organizations(search: String? = null) = api.organizations(search = search).unwrap()
    suspend fun organization(id: String) = api.organization(id).unwrap()
    suspend fun setFollowing(id: String, followed: Boolean) =
        if (followed) api.follow(id).unwrap() else api.unfollow(id).unwrap()
    suspend fun setChannel(id: String, subscribed: Boolean) =
        if (subscribed) api.subscribe(id).unwrap() else api.unsubscribe(id).unwrap()

    suspend fun notification(id: String) = api.notification(id).unwrap()
    suspend fun setNotificationState(id: String, state: String, enabled: Boolean) =
        if (enabled) api.enableNotificationState(id, state).unwrap() else api.disableNotificationState(id, state).unwrap()
    suspend fun markAllRead() = api.markAllRead().unwrap()

    suspend fun promotion(id: String) = api.promotion(id).unwrap()
    suspend fun claimPromotion(id: String): PromotionRedemption = api.claimPromotion(id).unwrap()
    suspend fun giveaway(id: String): Giveaway = api.giveaways().unwrap().firstOrNull { it.id == id }
        ?: throw NoSuchElementException("El sorteo ya no está disponible.")
    suspend fun setGiveawayEntry(id: String, entered: Boolean) =
        if (entered) api.enterGiveaway(id).unwrap() else api.withdrawGiveaway(id).unwrap()

    suspend fun survey(id: String) = api.survey(id).unwrap()
    suspend fun submitSurvey(id: String, answers: Map<String, Any>) {
        val response = api.startSurvey(id).unwrap()
        if (response.status != "completed") api.submitSurvey(response.id, SurveyAnswers(answers)).unwrap()
    }

    suspend fun loyaltyProgram(id: String) = api.loyaltyProgram(id).unwrap()
    suspend fun joinLoyalty(id: String) = api.joinLoyalty(id).unwrap()
    suspend fun redeemReward(id: String): Redemption = api.redeemReward(id).unwrap()

    suspend fun saved(): List<SavedItem> = api.saved().unwrap()
    suspend fun setSaved(type: String, id: String, saved: Boolean) =
        if (saved) api.save(type, id).unwrap() else api.unsave(type, id).unwrap()
    suspend fun search(query: String): List<SearchItem> = api.search(query).unwrap()
    suspend fun acceptInvitation(token: String) = api.acceptInvitation(InvitationRequest(token)).unwrap()

    suspend fun registerCurrentDevice() {
        if (!firebaseConfigured) return
        val token = FirebaseMessaging.getInstance().token.await()
        api.registerDevice(
            DeviceRegistration(token = token, locale = Locale.getDefault().toLanguageTag().take(10)),
        ).unwrap()
    }

    suspend fun unregisterCurrentDevice() {
        if (!firebaseConfigured) return
        val token = runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull() ?: return
        runCatching { api.unregisterDevice(app.alertbox.io.core.model.TokenRequest(token)).unwrap() }
        runCatching { FirebaseMessaging.getInstance().deleteToken().await() }
    }

    fun currentTimezone(): String = TimeZone.getDefault().id
}
