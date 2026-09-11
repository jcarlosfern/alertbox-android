package app.alertbox.io.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.LoyaltyReward
import app.alertbox.io.ui.components.OrganizationAvatar
import app.alertbox.io.ui.theme.AlertOrange
import app.alertbox.io.ui.theme.AlertYellow

private val LoyaltySuccess = Color(0xFF16864B)

@Composable
internal fun LoyaltyOverview(programs: List<LoyaltyProgram>, modifier: Modifier = Modifier) {
    val availableRewards = programs.sumOf { program ->
        program.rewards.orEmpty().count { reward -> program.joined == true && reward.active && reward.pointsRequired <= program.points }
    }
    val summary = buildString {
        append(if (programs.size == 1) "1 programa activo" else "${programs.size} programas activos")
        if (availableRewards > 0) {
            append(" · ")
            append(if (availableRewards == 1) "1 premio disponible" else "$availableRewards premios disponibles")
        }
    }
    val shape = RoundedCornerShape(28.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(14.dp, shape, ambientColor = AlertOrange.copy(alpha = 0.2f), spotColor = AlertOrange.copy(alpha = 0.24f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(AlertOrange, Color(0xFFFF8A38), AlertYellow)))
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f), modifier = Modifier.size(58.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = Color.White, modifier = Modifier.size(27.dp))
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Tus ventajas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White)
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.86f))
        }
        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White.copy(alpha = 0.78f))
    }
}

@Composable
internal fun LoyaltyProgramCard(program: LoyaltyProgram, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val nextReward = program.nextLockedReward()
    val progress = program.progressTo(nextReward)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 650),
        label = "Progreso de fidelización",
    )
    val shape = RoundedCornerShape(26.dp)
    val surface = MaterialTheme.colorScheme.surface

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().shadow(7.dp, shape, ambientColor = Color.Black.copy(alpha = 0.08f)),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(surface, AlertYellow.copy(alpha = 0.12f), AlertOrange.copy(alpha = 0.08f))))
                .border(1.dp, AlertOrange.copy(alpha = 0.12f), shape)
                .padding(19.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(18.dp), ambientColor = AlertOrange.copy(alpha = 0.18f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.92f))
                        .padding(3.dp),
                ) {
                    OrganizationAvatar(program.organizationName, program.organizationLogo, 54)
                }
                Column(modifier = Modifier.padding(start = 13.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(program.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(program.organizationName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = CircleShape, color = AlertOrange.copy(alpha = 0.1f), modifier = Modifier.size(35.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Ver programa", tint = AlertOrange, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text("${program.points}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = AlertOrange)
                Text(" puntos", modifier = Modifier.padding(bottom = 6.dp), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                program.tier?.takeIf(String::isNotBlank)?.let { tier ->
                    Surface(shape = RoundedCornerShape(50), color = AlertYellow.copy(alpha = 0.22f)) {
                        Row(modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(15.dp))
                            Text(tier.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                    color = if (nextReward == null) LoyaltySuccess else AlertOrange,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        if (nextReward == null) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (nextReward == null) LoyaltySuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        nextReward?.let { "A ${it.pointsRequired - program.points} puntos de ${it.title}" } ?: "Todos los premios a tu alcance",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (nextReward == null) LoyaltySuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LoyaltyHero(program: LoyaltyProgram, modifier: Modifier = Modifier) {
    val nextReward = program.nextLockedReward()
    val animatedProgress by animateFloatAsState(
        targetValue = program.progressTo(nextReward),
        animationSpec = tween(durationMillis = 700),
        label = "Progreso del programa",
    )
    val shape = RoundedCornerShape(30.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(16.dp, shape, ambientColor = AlertOrange.copy(alpha = 0.22f), spotColor = AlertOrange.copy(alpha = 0.25f))
            .clip(shape)
            .background(Brush.linearGradient(listOf(AlertOrange, Color(0xFFFF8534), AlertYellow)))
            .padding(21.dp),
        verticalArrangement = Arrangement.spacedBy(17.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(22.dp)).background(Color.White).padding(4.dp),
            ) { OrganizationAvatar(program.organizationName, program.organizationLogo, 66) }
            Column(modifier = Modifier.padding(start = 13.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(program.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = Color.White)
                Text(program.organizationName, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.84f))
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            Text("${program.points}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = Color.White)
            Text(" puntos", modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.84f))
            Spacer(Modifier.weight(1f))
            program.tier?.takeIf(String::isNotBlank)?.let { tier ->
                Surface(shape = RoundedCornerShape(50), color = Color.White.copy(alpha = 0.17f)) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Text(tier.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.22f),
            )
            Row {
                Text(nextReward?.let { "Próximo: ${it.title}" } ?: "Todos los premios desbloqueados", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.88f), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                nextReward?.let { Text("${it.pointsRequired} pt", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }

        if (program.visits > 0 || program.stamps > 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (program.visits > 0) LoyaltyMetric("${program.visits}", "visitas", Modifier.weight(1f))
                if (program.stamps > 0) LoyaltyMetric("${program.stamps}", "sellos", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LoyaltyMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White.copy(alpha = 0.15f)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("$value $label", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
internal fun LoyaltyRewardCard(
    program: LoyaltyProgram,
    reward: LoyaltyReward,
    saved: Boolean,
    onSave: () -> Unit,
    onRedeem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isAvailable = reward.active && program.joined == true && program.points >= reward.pointsRequired
    val remaining = (reward.pointsRequired - program.points).coerceAtLeast(0)
    val progress = (program.points.toFloat() / reward.pointsRequired.coerceAtLeast(1)).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(progress, tween(600), label = "Progreso de recompensa")
    val accent = if (isAvailable) LoyaltySuccess else AlertOrange
    val shape = RoundedCornerShape(24.dp)

    Card(
        modifier = modifier.fillMaxWidth().border(if (isAvailable) 1.5.dp else 1.dp, accent.copy(alpha = if (isAvailable) 0.34f else 0.1f), shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAvailable) 4.dp else 1.dp),
    ) {
        Column(modifier = Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Surface(shape = RoundedCornerShape(14.dp), color = accent.copy(alpha = if (isAvailable) 1f else 0.1f), modifier = Modifier.size(43.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(if (isAvailable) Icons.Default.CardGiftcard else Icons.Default.Lock, contentDescription = null, tint = if (isAvailable) Color.White else accent, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(reward.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    reward.description?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                IconButton(onClick = onSave) {
                    Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = if (saved) "Quitar de guardados" else "Guardar recompensa", tint = if (saved) AlertOrange else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.1f)) {
                    Text("${reward.pointsRequired} pt", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = accent)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    when {
                        !reward.active -> "No disponible"
                        program.joined != true -> "Únete para desbloquear"
                        remaining > 0 -> "Faltan $remaining puntos"
                        else -> "Premio disponible"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent,
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().clip(CircleShape),
                color = accent,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            )

            Button(
                onClick = onRedeem,
                enabled = isAvailable,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(if (isAvailable) Icons.Default.AutoAwesome else Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    when {
                        isAvailable -> "Canjear ahora"
                        program.joined != true -> "Únete para desbloquear"
                        remaining > 0 -> "Te faltan $remaining puntos"
                        else -> "Canjear"
                    },
                )
            }
        }
    }
}

private fun LoyaltyProgram.nextLockedReward(): LoyaltyReward? = rewards.orEmpty()
    .filter { it.active && it.pointsRequired > points }
    .minByOrNull(LoyaltyReward::pointsRequired)

private fun LoyaltyProgram.progressTo(nextReward: LoyaltyReward?): Float = when {
    nextReward != null -> (points.toFloat() / nextReward.pointsRequired.coerceAtLeast(1)).coerceIn(0f, 1f)
    points > 0 -> 1f
    else -> 0f
}
