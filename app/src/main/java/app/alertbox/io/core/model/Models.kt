package app.alertbox.io.core.model

data class ApiEnvelope<T>(
    val status: Int = 0,
    val data: T? = null,
    val error: String? = null,
    val message: String? = null,
)

data class MutationResult(
    val enabled: Boolean? = null,
    val deleted: Boolean? = null,
    val updatedCount: Int? = null,
    val followed: Boolean? = null,
    val subscribed: Boolean? = null,
    val saved: Boolean? = null,
    val entered: Boolean? = null,
    val completed: Boolean? = null,
)

data class UserProfile(
    val id: String,
    val email: String? = null,
    val emailVerified: Boolean = false,
    val firstName: String? = null,
    val lastName: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val city: String? = null,
    val countryCode: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val locale: String? = null,
    val timezone: String? = null,
    val onboardingComplete: Boolean = false,
    val preferences: UserPreferences? = null,
    val unreadCount: Int? = null,
) {
    val greetingName: String
        get() = displayName?.takeIf(String::isNotBlank)
            ?: firstName?.takeIf(String::isNotBlank)
            ?: "tú"
}

data class UserPreferences(
    val pushEnabled: Boolean = false,
    val emailEnabled: Boolean = true,
    val privateMessagesEnabled: Boolean = true,
    val marketingEnabled: Boolean = false,
    val quietHours: QuietHours? = null,
    val travelMode: TravelMode? = null,
    val interests: List<String>? = null,
)

data class QuietHours(
    val enabled: Boolean = false,
    val start: String = "22:00",
    val end: String = "08:00",
    val days: List<String>? = null,
    val criticalOnly: Boolean? = null,
    val allowCritical: Boolean? = null,
)

data class TravelMode(
    val enabled: Boolean = false,
    val timezone: String? = null,
    val destination: String? = null,
    val returnDate: String? = null,
    val batchHours: Int? = null,
    val pauseSurveys: Boolean? = null,
    val criticalOnly: Boolean? = null,
)

data class ProfileUpdate(
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val city: String,
    val countryCode: String,
    val gender: String,
    val birthDate: String,
    val locale: String,
    val timezone: String,
    val onboardingComplete: Boolean,
)

data class CitySuggestion(
    val id: String,
    val name: String,
    val region: String = "",
    val country: String = "",
    val countryCode: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class CitySearchResponse(
    val results: List<CitySuggestion> = emptyList(),
)

data class PreferencesUpdate(
    val pushEnabled: Boolean,
    val emailEnabled: Boolean,
    val privateMessagesEnabled: Boolean,
    val marketingEnabled: Boolean,
    val quietHours: QuietHours?,
    val travelMode: TravelMode?,
    val interests: List<String>?,
)

data class Organization(
    val id: String,
    val slug: String? = null,
    val name: String,
    val legalName: String? = null,
    val description: String? = null,
    val category: String? = null,
    val logoUrl: String? = null,
    val coverUrl: String? = null,
    val contactEmail: String? = null,
    val phone: String? = null,
    val website: String? = null,
    val address: String? = null,
    val city: String? = null,
    val region: String? = null,
    val countryCode: String? = null,
    val verified: Boolean = false,
    val followers: Int? = null,
    val followed: Boolean = false,
    val channels: List<Channel>? = null,
)

data class Channel(
    val id: String,
    val name: String,
    val description: String? = null,
    val icon: String? = null,
    val color: String? = null,
    val type: String = "public",
    val visibility: String = "public",
    val subscribed: Boolean = false,
) {
    val requiresApproval: Boolean
        get() = type in setOf("approval", "membership", "event", "loyalty")
}

data class AlertNotification(
    val id: String,
    val organizationId: String,
    val organizationName: String,
    val organizationLogo: String? = null,
    val channelId: String? = null,
    val channelName: String? = null,
    val title: String,
    val subtitle: String? = null,
    val body: String,
    val coverImage: String? = null,
    val type: String = "announcement",
    val ctaLabel: String? = null,
    val ctaDestination: String? = null,
    val publishedAt: String? = null,
    val expiresAt: String? = null,
    val read: Boolean = false,
    val saved: Boolean = false,
    val createdAt: String? = null,
)

data class Promotion(
    val id: String,
    val organizationId: String,
    val organizationName: String,
    val organizationLogo: String? = null,
    val title: String,
    val description: String,
    val coverImage: String? = null,
    val offerType: String? = null,
    val originalPrice: Double? = null,
    val currentPrice: Double? = null,
    val discountPercent: Double? = null,
    val conditions: String? = null,
    val redemptionInstructions: String? = null,
    val redemptionMethod: String? = null,
    val ctaLabel: String? = null,
    val ctaDestination: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val claimed: Boolean = false,
    val saved: Boolean = false,
)

data class PromotionRedemption(
    val id: String,
    val redemptionCode: String,
)

data class Giveaway(
    val id: String,
    val organizationId: String,
    val organizationName: String,
    val organizationLogo: String? = null,
    val title: String,
    val description: String,
    val prize: String,
    val numberOfWinners: Int = 1,
    val coverImage: String? = null,
    val conditions: String? = null,
    val legalTerms: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val entries: Int = 0,
    val entered: Boolean = false,
)

data class Survey(
    val id: String,
    val organizationId: String,
    val organizationName: String,
    val organizationLogo: String? = null,
    val title: String,
    val description: String? = null,
    val introText: String? = null,
    val estimatedMinutes: Int? = null,
    val anonymous: Boolean = false,
    val incentive: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val thankYouMessage: String? = null,
    val questions: List<SurveyQuestion>? = null,
    val completed: Boolean = false,
)

data class SurveyQuestion(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: String = "open_text",
    val required: Boolean = false,
    val options: List<String>? = null,
    val order: Int = 0,
)

data class SurveyResponse(
    val id: String,
    val status: String,
)

data class LoyaltyProgram(
    val id: String,
    val organizationId: String,
    val organizationName: String,
    val organizationLogo: String? = null,
    val name: String,
    val description: String? = null,
    val programType: String,
    val coverImage: String? = null,
    val terms: String? = null,
    val rules: String? = null,
    val eligibility: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val points: Int = 0,
    val visits: Int = 0,
    val stamps: Int = 0,
    val tier: String? = null,
    val joined: Boolean? = null,
    val rewards: List<LoyaltyReward>? = null,
    val history: List<LoyaltyTransaction>? = null,
)

data class LoyaltyReward(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: String,
    val pointsRequired: Int,
    val inventory: Int? = null,
    val redemptionMethod: String? = null,
    val expiration: String? = null,
    val active: Boolean = true,
)

data class LoyaltyTransaction(
    val id: String,
    val type: String,
    val quantity: Int,
    val note: String? = null,
    val createdAt: String? = null,
)

data class Redemption(
    val id: String,
    val code: String,
    val status: String? = null,
    val createdAt: String? = null,
)

data class SavedItem(
    val id: String,
    val itemType: String,
    val itemId: String,
    val title: String,
    val subtitle: String? = null,
    val image: String? = null,
    val createdAt: String? = null,
) {
    val type: String
        get() = if (itemType == "loyalty_reward") "reward" else itemType
}

data class SearchItem(
    val type: String,
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val image: String? = null,
)

data class DeviceRegistration(
    val token: String,
    val platform: String = "android",
    val locale: String,
)

data class TokenRequest(val token: String)
data class SurveyAnswers(val answers: Map<String, Any>)
data class InvitationRequest(val token: String)
data object EmptyRequest
