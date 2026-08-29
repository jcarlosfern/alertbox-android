package app.alertbox.io.core.network

import app.alertbox.io.core.model.ApiEnvelope
import app.alertbox.io.core.model.AvatarUpdate
import app.alertbox.io.core.model.MediaAsset
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class ApiParsingTest {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.javaPrimitiveType, FlexibleBooleanAdapter())
        .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanAdapter().nullSafe())
        .create()

    @Test
    fun `accepts booleans returned as native values numbers and strings`() {
        assertTrue(gson.fromJson("true", Boolean::class.java))
        assertTrue(gson.fromJson("1", Boolean::class.java))
        assertTrue(gson.fromJson("\"yes\"", Boolean::class.java))
        assertFalse(gson.fromJson("0", Boolean::class.java))
        assertFalse(gson.fromJson("\"false\"", Boolean::class.java))
    }

    @Test
    fun `rejects ambiguous boolean strings`() {
        assertThrows(JsonSyntaxException::class.java) {
            gson.fromJson("\"enabled-maybe\"", Boolean::class.java)
        }
    }

    @Test
    fun `unwrap returns data and preserves safe API error`() {
        assertTrue(ApiEnvelope(data = true).unwrap())
        val error = assertThrows(AlertBoxApiException::class.java) {
            ApiEnvelope<Boolean>(error = "forbidden", message = "No autorizado").unwrap()
        }
        assertTrue(error.code == "forbidden")
        assertTrue(error.message == "No autorizado")
    }

    @Test
    fun `serializes avatar update and decodes uploaded media URL`() {
        assertTrue(gson.toJson(AvatarUpdate("https://images.example/avatar.jpg")).contains("avatarUrl"))
        val asset = gson.fromJson("{\"url\":\"https://images.example/avatar.jpg\"}", MediaAsset::class.java)
        assertTrue(asset.url.endsWith("avatar.jpg"))
    }
}
