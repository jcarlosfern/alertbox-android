package app.alertbox.io.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.alertbox.io.core.model.AlertNotification
import app.alertbox.io.core.model.Giveaway
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.Promotion
import app.alertbox.io.core.model.Survey
import app.alertbox.io.ui.AppUiState
import app.alertbox.io.ui.AppViewModel
import app.alertbox.io.ui.components.AlertCard
import app.alertbox.io.ui.components.BrandMark
import app.alertbox.io.ui.components.CoverImage
import app.alertbox.io.ui.components.EmptyState
import app.alertbox.io.ui.components.LoadingState
import app.alertbox.io.ui.components.OrganizationAvatar
import app.alertbox.io.ui.components.OrganizationIdentityCard
import app.alertbox.io.ui.components.SectionTitle
import app.alertbox.io.ui.components.TwoLineText
import app.alertbox.io.ui.theme.AlertOrange

@Composable
fun HomeScreen(state: AppUiState, padding: PaddingValues, nav: NavHostController, viewModel: AppViewModel) {
    val data = state.dashboard
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Hola, ${state.profile?.greetingName ?: "tú"}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                    Text("Esto es lo importante de hoy", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BrandMark(size = 48)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                QuickStat("${data.notifications.count { !it.read }}", "Sin leer", Modifier.weight(1f)) { nav.navigate(Routes.INBOX) }
                QuickStat("${data.loyalty.size}", "Programas", Modifier.weight(1f)) { nav.navigate(Routes.LOYALTY) }
                QuickStat("${data.organizations.count { it.followed }}", "Siguiendo", Modifier.weight(1f)) { nav.navigate(Routes.ORGANIZATIONS) }
            }
        }
        if (state.refreshing) item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        data.notifications.firstOrNull()?.let { notification ->
            item {
                SectionTitle("Destacado", "Lo más reciente para ti")
                NotificationCard(notification) { nav.navigate(Routes.notification(notification.id)) }
            }
        }
        if (data.promotions.isNotEmpty()) {
            item { SectionTitle("Promociones", "Ventajas de organizaciones que sigues") }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(data.promotions, key = Promotion::id) { promotion ->
                        PromotionCard(promotion) { nav.navigate(Routes.promotion(promotion.id)) }
                    }
                }
            }
        }
        if (data.surveys.isNotEmpty()) {
            item { SectionTitle("Tu opinión cuenta", "Encuestas disponibles") { TextButton(onClick = { nav.navigate(Routes.SURVEYS) }) { Text("Ver todas") } } }
            items(data.surveys.filter { !it.completed }.take(3), key = Survey::id) { survey ->
                SurveyCard(survey) { nav.navigate(Routes.survey(survey.id)) }
            }
        }
        if (data.giveaways.isNotEmpty()) {
            item { SectionTitle("Sorteos activos") }
            items(data.giveaways.take(3), key = Giveaway::id) { giveaway ->
                GiveawayCard(giveaway) { nav.navigate(Routes.giveaway(giveaway.id)) }
            }
        }
        if (data.notifications.isEmpty() && data.promotions.isEmpty() && !state.refreshing) {
            item { EmptyState("🏢", "Tu inicio está listo", "Sigue organizaciones para recibir alertas y ventajas.", "Descubrir") { nav.navigate(Routes.ORGANIZATIONS) } }
        }
        item {
            FilledTonalButton(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Text("Actualizar", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun QuickStat(value: String, label: String, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.padding(vertical = 18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = AlertOrange)
            Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
fun InboxScreen(state: AppUiState, padding: PaddingValues, nav: NavHostController, viewModel: AppViewModel) {
    var filter by remember { mutableStateOf("Todo") }
    val filters = listOf("Todo", "Sin leer", "Ofertas", "Encuestas", "Guardado")
    val visible = state.dashboard.notifications.filter { notification ->
        when (filter) {
            "Sin leer" -> !notification.read
            "Ofertas" -> notification.type == "promotion"
            "Encuestas" -> notification.type == "survey_invitation"
            "Guardado" -> notification.saved
            else -> true
        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Bandeja", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            if (state.dashboard.notifications.any { !it.read }) {
                IconButton(onClick = viewModel::markAllRead) { Icon(Icons.Default.DoneAll, contentDescription = "Marcar todo como leído") }
            }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { item -> FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item) }) }
        }
        if (visible.isEmpty()) EmptyState("📭", "Nada por aquí", "No hay alertas para este filtro.")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(visible, key = AlertNotification::id) { notification ->
                NotificationCard(notification) { nav.navigate(Routes.notification(notification.id)) }
            }
        }
    }
}

@Composable
fun LoyaltyScreen(state: AppUiState, padding: PaddingValues, nav: NavHostController) {
    val programs = state.dashboard.loyalty
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Text("Fidelización", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.padding(18.dp))
        if (programs.isEmpty()) EmptyState("⭐", "Tus ventajas aparecerán aquí", "Sigue organizaciones con programas de puntos o sellos.")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            items(programs, key = LoyaltyProgram::id) { program ->
                AlertCard(modifier = Modifier.clickable { nav.navigate(Routes.loyalty(program.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OrganizationAvatar(program.organizationName, program.organizationLogo, 54)
                        TwoLineText(program.name, program.organizationName, Modifier.padding(start = 12.dp).weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                    Text("${program.points} puntos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = AlertOrange)
                    program.tier?.let { AssistChip(onClick = {}, label = { Text(it) }) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationsScreen(state: AppUiState, padding: PaddingValues, nav: NavHostController, viewModel: AppViewModel) {
    var query by remember { mutableStateOf("") }
    var followedOnly by remember { mutableStateOf(false) }
    val organizations = state.dashboard.organizations.filter {
        (!followedOnly || it.followed) && (query.isBlank() || it.name.contains(query, true) || it.category.orEmpty().contains(query, true))
    }
    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Organizaciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            IconButton(onClick = { viewModel.refresh() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar empresas")
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Nombre o categoría") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = { IconButton(onClick = { followedOnly = !followedOnly }) { Icon(if (followedOnly) Icons.Default.Star else Icons.Default.FilterList, contentDescription = "Filtrar") } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            singleLine = true,
        )
        if (organizations.isEmpty()) EmptyState("🔎", "Sin resultados", "Prueba con otro nombre o muestra todas las organizaciones.")
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(organizations, key = Organization::id) { organization ->
                OrganizationIdentityCard(organization, compact = true,
                    modifier = Modifier.clickable { nav.navigate(Routes.organization(organization.id)) })
            }
        }
    }
}

@Composable
fun SurveysScreen(state: AppUiState, nav: NavHostController) {
    var filter by remember { mutableStateOf("Pendientes") }
    val surveys = state.dashboard.surveys.filter {
        when (filter) {
            "Completadas" -> it.completed
            "Con recompensa" -> it.incentive != null
            else -> !it.completed
        }
    }
    Scaffold(topBar = { SimpleTopBar("Encuestas", nav) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(contentPadding = PaddingValues(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Pendientes", "Completadas", "Con recompensa")) { item -> FilterChip(selected = filter == item, onClick = { filter = item }, label = { Text(item) }) }
            }
            if (surveys.isEmpty()) EmptyState("📋", "No hay encuestas", "Las nuevas encuestas aparecerán aquí.")
            else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(surveys, key = Survey::id) { survey -> SurveyCard(survey) { nav.navigate(Routes.survey(survey.id)) } }
            }
        }
    }
}

@Composable
private fun NotificationCard(notification: AlertNotification, onClick: () -> Unit) {
    AlertCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BadgedBox(badge = { if (!notification.read) Badge() }) { OrganizationAvatar(notification.organizationName, notification.organizationLogo) }
            TwoLineText(notification.title, notification.body, Modifier.padding(start = 12.dp).weight(1f))
            if (notification.saved) Icon(Icons.Default.Bookmark, contentDescription = "Guardada", tint = AlertOrange)
        }
    }
}

@Composable
private fun PromotionCard(promotion: Promotion, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.size(width = 250.dp, height = 260.dp), shape = RoundedCornerShape(22.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CoverImage(promotion.coverImage, promotion.title, 128)
            Text(promotion.organizationName, style = MaterialTheme.typography.labelMedium, color = AlertOrange)
            Text(promotion.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            promotion.discountPercent?.let { Text("-${it.toInt()} %", color = AlertOrange, fontWeight = FontWeight.Black) }
        }
    }
}

@Composable
private fun SurveyCard(survey: Survey, onClick: () -> Unit) {
    AlertCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📋", style = MaterialTheme.typography.headlineMedium)
            TwoLineText(survey.title, "${survey.estimatedMinutes ?: 2} min · ${survey.organizationName}", Modifier.padding(start = 12.dp).weight(1f))
            if (survey.completed) Icon(Icons.Default.DoneAll, contentDescription = "Completada") else Icon(Icons.Default.ChevronRight, null)
        }
    }
}

@Composable
private fun GiveawayCard(giveaway: Giveaway, onClick: () -> Unit) {
    AlertCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎁", style = MaterialTheme.typography.headlineMedium)
            TwoLineText(giveaway.title, giveaway.prize, Modifier.padding(start = 12.dp).weight(1f))
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}
