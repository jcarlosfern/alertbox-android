package app.alertbox.io.core.network

import app.alertbox.io.core.model.ApiEnvelope
import app.alertbox.io.core.model.AvatarUpdate
import app.alertbox.io.core.model.MediaAsset
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.OrganizationBrand
import app.alertbox.io.ui.theme.brandColor
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ApiParsingTest {
    private val gson = GsonBuilder()
        .registerTypeAdapter(Boolean::class.javaPrimitiveType, FlexibleBooleanAdapter())
        .registerTypeAdapter(Boolean::class.javaObjectType, FlexibleBooleanAdapter().nullSafe())
        .create()

    @Test
    fun `organization uses admin brand logo and complete contact data`() {
        val organization = gson.fromJson("""{
            "id":"company", "name":"Café Aroma", "logoUrl":"  ",
            "brand":{"logoUrl":"https://example.com/logo.png","primaryColor":"210 70% 40%","accentColor":"40 100% 57%"},
            "description":"Café de especialidad", "category":"Restauración",
            "contactEmail":"hola@example.com", "phone":"+34 928 000 000", "website":"https://example.com",
            "address":"Calle Mayor 12", "city":"Las Palmas", "region":"Las Palmas", "countryCode":"ES",
            "verified":true, "followed":false, "followers":12, "showFollowerCount":true
        }""", Organization::class.java)
        assertEquals("https://example.com/logo.png", organization.displayLogoUrl)
        assertEquals("210 70% 40%", organization.brand?.primaryColor)
        assertEquals("hola@example.com", organization.contactEmail)
        assertEquals("+34 928 000 000", organization.phone)
        assertEquals("https://example.com", organization.website)
        assertEquals("Calle Mayor 12, Las Palmas, ES", organization.fullAddress)
        assertEquals("Las Palmas", organization.locationLabel)
        assertEquals(12, organization.visibleFollowers)
        assertTrue(organization.verified)
        assertFalse(organization.followed)
    }

    @Test
    fun `organization supports production empty profile and respects follower visibility`() {
        val organization = gson.fromJson("""{"id":"company","name":"LG Laboral","logoUrl":null,"brand":null,
            "category":null,"city":null,"description":null,"followers":1,"followed":false,"verified":false}""", Organization::class.java)
        assertNull(organization.displayLogoUrl)
        assertNull(organization.fullAddress)
        assertNull(organization.visibleFollowers)
        assertEquals("Organización", organization.categoryLabel)
        assertNull(organization.copy(showFollowerCount = false).visibleFollowers)
        assertEquals(1, organization.copy(showFollowerCount = true).visibleFollowers)
        assertEquals("https://example.com/current.png", organization.copy(logoUrl = " https://example.com/current.png ",
            brand = OrganizationBrand(logoUrl = "https://example.com/old.png")).displayLogoUrl)
    }

    @Test
    fun `organization category is shown in Spanish for codes and legacy English names`() {
        val organization = gson.fromJson("""{"id":"company","name":"Café Aroma","category":"food_drink"}""", Organization::class.java)

        assertEquals("Alimentación y bebidas", organization.categoryLabel)
        assertEquals("Salud y bienestar", organization.copy(category = "Health & Fitness").categoryLabel)
        assertEquals("Restauración", organization.copy(category = "Restauración").categoryLabel)
    }

    @Test
    fun `brand colors decode CSS HSL and hex without accepting invalid values`() {
        val red = brandColor("0 100% 50%")!!
        assertEquals(1f, red.red, 0.001f)
        assertEquals(0f, red.green, 0.001f)
        val blue = brandColor("210 70% 40%")!!
        // Compose packs sRGB channels into 8 bits.
        assertEquals(0.12f, blue.red, 1f / 255)
        assertEquals(0.4f, blue.green, 1f / 255)
        assertEquals(0.68f, blue.blue, 1f / 255)
        assertEquals(brandColor("#ff0000"), red)
        assertNull(brandColor(null))
        assertNull(brandColor(""))
        assertNull(brandColor("NaN 100% 50%"))
        assertNull(brandColor("0 101% 50%"))
        assertNull(brandColor("361 100% 50%"))
        assertNull(brandColor("0 100 50"))
    }

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
