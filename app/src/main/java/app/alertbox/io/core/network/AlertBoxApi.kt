package app.alertbox.io.core.network

import app.alertbox.io.core.model.AlertNotification
import app.alertbox.io.core.model.ApiEnvelope
import app.alertbox.io.core.model.CitySearchResponse
import app.alertbox.io.core.model.DeviceRegistration
import app.alertbox.io.core.model.EmptyRequest
import app.alertbox.io.core.model.Giveaway
import app.alertbox.io.core.model.InvitationRequest
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.MutationResult
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.PreferencesUpdate
import app.alertbox.io.core.model.ProfileUpdate
import app.alertbox.io.core.model.AvatarUpdate
import app.alertbox.io.core.model.MediaAsset
import app.alertbox.io.core.model.Promotion
import app.alertbox.io.core.model.PromotionRedemption
import app.alertbox.io.core.model.Redemption
import app.alertbox.io.core.model.SavedItem
import app.alertbox.io.core.model.SearchItem
import app.alertbox.io.core.model.Survey
import app.alertbox.io.core.model.SurveyAnswers
import app.alertbox.io.core.model.SurveyResponse
import app.alertbox.io.core.model.TokenRequest
import app.alertbox.io.core.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface AlertBoxApi {
    @POST("auth/sync") suspend fun syncAccount(@Body body: EmptyRequest = EmptyRequest): ApiEnvelope<UserProfile>
    @GET("me") suspend fun profile(): ApiEnvelope<UserProfile>
    @PATCH("me") suspend fun updateProfile(@Body body: ProfileUpdate): ApiEnvelope<UserProfile>
    @PATCH("me") suspend fun updateAvatar(@Body body: AvatarUpdate): ApiEnvelope<UserProfile>
    @Multipart
    @POST("media/files")
    suspend fun uploadMedia(@Part("purpose") purpose: RequestBody, @Part file: MultipartBody.Part): ApiEnvelope<MediaAsset>
    @DELETE("me") suspend fun deleteProfile(): ApiEnvelope<MutationResult>
    @PATCH("me/preferences") suspend fun updatePreferences(@Body body: PreferencesUpdate): ApiEnvelope<UserProfile>
    @GET("locations/cities")
    suspend fun searchCities(@Query("query") query: String, @Query("language") language: String): ApiEnvelope<CitySearchResponse>
    @POST("me/devices") suspend fun registerDevice(@Body body: DeviceRegistration): ApiEnvelope<MutationResult>
    @HTTP(method = "DELETE", path = "me/devices", hasBody = true)
    suspend fun unregisterDevice(@Body body: TokenRequest): ApiEnvelope<MutationResult>

    @GET("organizations")
    suspend fun organizations(@Query("limit") limit: Int = 100, @Query("search") search: String? = null): ApiEnvelope<List<Organization>>
    @GET("organizations/{id}") suspend fun organization(@Path("id") id: String): ApiEnvelope<Organization>
    @PUT("organizations/{id}/follow") suspend fun follow(@Path("id") id: String): ApiEnvelope<MutationResult>
    @DELETE("organizations/{id}/follow") suspend fun unfollow(@Path("id") id: String): ApiEnvelope<MutationResult>
    @PUT("channels/{id}/subscription") suspend fun subscribe(@Path("id") id: String): ApiEnvelope<MutationResult>
    @DELETE("channels/{id}/subscription") suspend fun unsubscribe(@Path("id") id: String): ApiEnvelope<MutationResult>

    @GET("feed")
    suspend fun feed(@Query("limit") limit: Int = 100, @Query("type") type: String? = null, @Query("unreadOnly") unreadOnly: Boolean? = null): ApiEnvelope<List<AlertNotification>>
    @GET("notifications/{id}") suspend fun notification(@Path("id") id: String): ApiEnvelope<AlertNotification>
    @PUT("notifications/{id}/{state}") suspend fun enableNotificationState(@Path("id") id: String, @Path("state") state: String): ApiEnvelope<MutationResult>
    @DELETE("notifications/{id}/{state}") suspend fun disableNotificationState(@Path("id") id: String, @Path("state") state: String): ApiEnvelope<MutationResult>
    @POST("notifications/read-all") suspend fun markAllRead(): ApiEnvelope<MutationResult>

    @GET("promotions") suspend fun promotions(): ApiEnvelope<List<Promotion>>
    @GET("promotions/{id}") suspend fun promotion(@Path("id") id: String): ApiEnvelope<Promotion>
    @POST("promotions/{id}/claim") suspend fun claimPromotion(@Path("id") id: String): ApiEnvelope<PromotionRedemption>
    @GET("giveaways") suspend fun giveaways(): ApiEnvelope<List<Giveaway>>
    @PUT("giveaways/{id}/entry") suspend fun enterGiveaway(@Path("id") id: String): ApiEnvelope<MutationResult>
    @DELETE("giveaways/{id}/entry") suspend fun withdrawGiveaway(@Path("id") id: String): ApiEnvelope<MutationResult>

    @GET("surveys") suspend fun surveys(): ApiEnvelope<List<Survey>>
    @GET("surveys/{id}") suspend fun survey(@Path("id") id: String): ApiEnvelope<Survey>
    @POST("surveys/{id}/responses") suspend fun startSurvey(@Path("id") id: String): ApiEnvelope<SurveyResponse>
    @PUT("survey-responses/{id}") suspend fun submitSurvey(@Path("id") id: String, @Body body: SurveyAnswers): ApiEnvelope<MutationResult>

    @GET("loyalty") suspend fun loyaltyPrograms(): ApiEnvelope<List<LoyaltyProgram>>
    @GET("loyalty/{id}") suspend fun loyaltyProgram(@Path("id") id: String): ApiEnvelope<LoyaltyProgram>
    @POST("loyalty/{id}/join") suspend fun joinLoyalty(@Path("id") id: String): ApiEnvelope<MutationResult>
    @POST("loyalty/rewards/{id}/redeem") suspend fun redeemReward(@Path("id") id: String): ApiEnvelope<Redemption>

    @GET("saved") suspend fun saved(): ApiEnvelope<List<SavedItem>>
    @PUT("saved/{type}/{id}") suspend fun save(@Path("type") type: String, @Path("id") id: String): ApiEnvelope<MutationResult>
    @DELETE("saved/{type}/{id}") suspend fun unsave(@Path("type") type: String, @Path("id") id: String): ApiEnvelope<MutationResult>
    @GET("search") suspend fun search(@Query("q") query: String): ApiEnvelope<List<SearchItem>>
    @POST("invitations/accept") suspend fun acceptInvitation(@Body body: InvitationRequest): ApiEnvelope<MutationResult>
}
