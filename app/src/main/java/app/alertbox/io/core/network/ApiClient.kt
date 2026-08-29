package app.alertbox.io.core.network

import app.alertbox.io.BuildConfig
import app.alertbox.io.core.model.ApiEnvelope
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    fun create(auth: FirebaseAuth?): AlertBoxApi {
        check(BuildConfig.API_BASE_URL.startsWith("https://")) { "AlertBox API must use HTTPS" }
        val gson = GsonBuilder()
            .registerTypeAdapter(Boolean::class.javaPrimitiveType, FlexibleBooleanAdapter())
            .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanAdapter().nullSafe())
            .create()
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .addInterceptor(ClientHeadersInterceptor(auth))
            .retryOnConnectionFailure(true)
            .build()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AlertBoxApi::class.java)
    }
}

private class ClientHeadersInterceptor(private val auth: FirebaseAuth?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runCatching {
            runBlocking { auth?.currentUser?.getIdToken(false)?.await()?.token }
        }.getOrNull()
        val request = chain.request().newBuilder()
            .header("Accept", "application/json")
            .header("X-Client", "AlertBox-Android")
            .apply { if (!token.isNullOrBlank()) header("Authorization", "Bearer $token") }
            .build()
        return chain.proceed(request)
    }
}

class AlertBoxApiException(
    val code: String,
    override val message: String,
) : RuntimeException(message)

fun <T> ApiEnvelope<T>.unwrap(): T = data ?: throw AlertBoxApiException(
    code = error ?: "invalid_response",
    message = message ?: "AlertBox ha recibido una respuesta inesperada.",
)
