package app.alertbox.io.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.OrganizationBrand
import app.alertbox.io.ui.components.OrganizationIdentityCard
import app.alertbox.io.ui.theme.AlertBoxTheme
import java.io.File
import org.junit.Rule
import org.junit.Test

class OrganizationProfileTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun emptyProductionProfileRemainsVisible() {
        compose.setContent {
            AlertBoxTheme(darkTheme = false) {
                Surface { Column(Modifier.padding(16.dp)) {
                    OrganizationIdentityCard(Organization(id = "company", name = "LG Laboral"), compact = true)
                } }
            }
        }
        compose.onNodeWithText("LG Laboral").assertIsDisplayed()
        compose.onNodeWithText("LL").assertIsDisplayed()
        compose.onNodeWithText("Organización").assertIsDisplayed()
        screenshot("organization-empty")
    }

    @Test
    fun brandedProfileShowsLogoDescriptionAndLocation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent {
            AlertBoxTheme(darkTheme = false) {
                Surface { Column(Modifier.padding(16.dp)) {
                    OrganizationIdentityCard(Organization(
                        id = "company", name = "Café Aroma", category = "Restauración",
                        description = "Café de especialidad y repostería artesanal en el corazón de la ciudad.",
                        city = "Las Palmas", verified = true, followed = true,
                        brand = OrganizationBrand(logoUrl = "android.resource://${context.packageName}/drawable/ic_launcher_foreground",
                            primaryColor = "210 70% 40%", accentColor = "175 65% 40%"),
                    ), compact = true)
                } }
            }
        }
        compose.onNodeWithText("Café Aroma").assertIsDisplayed()
        compose.onNodeWithText("Las Palmas").assertIsDisplayed()
        compose.onNodeWithContentDescription("Verificada").assertIsDisplayed()
        // The packaged test logo must replace the initials after Coil finishes loading.
        compose.waitUntil(10_000) {
            compose.onAllNodes(androidx.compose.ui.test.hasText("CA")).fetchSemanticsNodes().isEmpty()
        }
        compose.onNodeWithText("CA").assertDoesNotExist()
        screenshot("organization-branded")
    }

    @Test
    fun brokenLogoKeepsInitialsInDarkMode() {
        compose.setContent {
            AlertBoxTheme(darkTheme = true) {
                Surface { Column(Modifier.padding(16.dp)) {
                    OrganizationIdentityCard(Organization(id = "company", name = "Empresa con nombre largo para comprobar el diseño",
                        logoUrl = "invalid://missing-logo", followers = 42, showFollowerCount = false))
                } }
            }
        }
        compose.onNodeWithText("EC").assertIsDisplayed()
        compose.onNodeWithText("42 seguidores").assertDoesNotExist()
        screenshot("organization-dark")
    }

    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
