package app.alertbox.io.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.nonBlank
import app.alertbox.io.ui.theme.AlertOrange
import app.alertbox.io.ui.theme.AlertYellow
import app.alertbox.io.ui.theme.brandColor
import coil.compose.AsyncImage

@Composable
fun OrganizationIdentityCard(organization: Organization, modifier: Modifier = Modifier, compact: Boolean = false) {
    val primary = brandColor(organization.brand?.primaryColor) ?: AlertOrange
    val accent = brandColor(organization.brand?.accentColor) ?: AlertYellow
    Card(
        modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(if (compact) 126.dp else 202.dp)) {
            Box(
                Modifier.fillMaxWidth().height(if (compact) 92.dp else 160.dp)
                    .background(Brush.linearGradient(listOf(primary, accent))),
            ) {
                organization.coverUrl.nonBlank()?.let { cover ->
                    AsyncImage(model = cover, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize())
                }
            }
            Box(Modifier.align(Alignment.BottomStart).padding(start = 20.dp)) {
                OrganizationAvatar(organization.name, organization.displayLogoUrl, if (compact) 76 else 96)
            }
        }
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(organization.name, fontWeight = FontWeight.ExtraBold,
                    style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f))
                if (organization.verified) Icon(Icons.Default.Verified, "Verificada", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                if (compact) Icon(if (organization.followed) Icons.Default.Star else Icons.Default.ChevronRight,
                    if (organization.followed) "Siguiendo" else null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(organization.categoryLabel, style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 5.dp))
            if (compact) organization.description.nonBlank()?.let {
                Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            organization.locationLabel?.let {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            organization.visibleFollowers?.let {
                Text("$it ${if (it == 1) "seguidor" else "seguidores"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
