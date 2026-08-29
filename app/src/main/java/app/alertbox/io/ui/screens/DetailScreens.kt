package app.alertbox.io.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.alertbox.io.core.model.AlertNotification
import app.alertbox.io.core.model.Giveaway
import app.alertbox.io.core.model.LoyaltyProgram
import app.alertbox.io.core.model.Organization
import app.alertbox.io.core.model.Promotion
import app.alertbox.io.core.model.SavedItem
import app.alertbox.io.core.model.Survey
import app.alertbox.io.core.model.SurveyQuestion
import app.alertbox.io.ui.AppUiState
import app.alertbox.io.ui.AppViewModel
import app.alertbox.io.ui.components.AlertCard
import app.alertbox.io.ui.components.CoverImage
import app.alertbox.io.ui.components.EmptyState
import app.alertbox.io.ui.components.LoadingState
import app.alertbox.io.ui.components.OrganizationAvatar
import app.alertbox.io.ui.components.SectionTitle
import app.alertbox.io.ui.components.TwoLineText
import app.alertbox.io.ui.theme.AlertOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, nav: NavHostController) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = nav::navigateUp) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás") } },
    )
}

@Composable
fun NotificationDetailScreen(id: String, nav: NavHostController, viewModel: AppViewModel) {
    var item by remember { mutableStateOf<AlertNotification?>(null) }
    val context = LocalContext.current
    LaunchedEffect(id) {
        item = viewModel.loadNotification(id)
        item?.let { if (!it.read) viewModel.markNotification(it.id, "read", true) }
    }
    DetailScaffold("Alerta", nav, item) { notification ->
        CoverImage(notification.coverImage, notification.title, 230)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrganizationAvatar(notification.organizationName, notification.organizationLogo, 54)
            TwoLineText(notification.organizationName, notification.channelName, Modifier.padding(start = 12.dp).weight(1f))
            IconButton(onClick = {
                viewModel.markNotification(notification.id, "saved", !notification.saved)
                item = notification.copy(saved = !notification.saved)
            }) { Icon(if (notification.saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Guardar") }
        }
        notification.subtitle?.let { Text(it, color = AlertOrange, fontWeight = FontWeight.Bold) }
        Text(notification.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(notification.body, style = MaterialTheme.typography.bodyLarge)
        safeHttpsUrl(notification.ctaDestination)?.let { uri ->
            Button(
                onClick = {
                    viewModel.markNotification(notification.id, "clicked", true)
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
            ) {
                Text(notification.ctaLabel ?: "Abrir")
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
fun PromotionDetailScreen(id: String, nav: NavHostController, viewModel: AppViewModel) {
    var item by remember { mutableStateOf<Promotion?>(null) }
    var code by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(id) { item = viewModel.loadPromotion(id) }
    DetailScaffold("Promoción", nav, item) { promotion ->
        CoverImage(promotion.coverImage, promotion.title, 230)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrganizationAvatar(promotion.organizationName, promotion.organizationLogo, 54)
            Text(promotion.organizationName, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp).weight(1f))
            IconButton(onClick = {
                viewModel.setSaved("promotion", promotion.id, !promotion.saved)
                item = promotion.copy(saved = !promotion.saved)
            }) { Icon(if (promotion.saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Guardar") }
        }
        promotion.discountPercent?.let { Text("-${it.toInt()} %", color = AlertOrange, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) }
        Text(promotion.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(promotion.description)
        promotion.conditions?.let { AlertCard { Text("Condiciones", fontWeight = FontWeight.Bold); Text(it) } }
        promotion.redemptionInstructions?.let { AlertCard { Text("Cómo canjear", fontWeight = FontWeight.Bold); Text(it) } }
        Button(
            onClick = { viewModel.claimPromotion(promotion.id) { code = it } },
            enabled = !promotion.claimed,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text(if (promotion.claimed) "Ya reclamada" else "Reclamar promoción") }
    }
    code?.let { RedemptionDialog(it, onDismiss = { code = null }) }
}

@Composable
fun GiveawayDetailScreen(id: String, nav: NavHostController, state: AppUiState, viewModel: AppViewModel) {
    var item by remember(id, state.dashboard.giveaways) { mutableStateOf(state.dashboard.giveaways.firstOrNull { it.id == id }) }
    LaunchedEffect(id) {
        if (item == null) item = viewModel.loadGiveaway(id)
    }
    DetailScaffold("Sorteo", nav, item) { giveaway ->
        CoverImage(giveaway.coverImage, giveaway.title, 230)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OrganizationAvatar(giveaway.organizationName, giveaway.organizationLogo, 54)
            TwoLineText(giveaway.organizationName, giveaway.endDate?.let { "Finaliza $it" }, Modifier.padding(start = 12.dp))
        }
        Text(giveaway.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text(giveaway.description)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AlertCard(Modifier.weight(1f)) { Text("Premio", fontWeight = FontWeight.Bold); Text(giveaway.prize) }
            AlertCard(Modifier.weight(1f)) { Text("Participantes", fontWeight = FontWeight.Bold); Text("${giveaway.entries}") }
        }
        Text("${giveaway.numberOfWinners} ganador${if (giveaway.numberOfWinners == 1) "" else "es"}")
        giveaway.conditions?.let { AlertCard { Text("Condiciones", fontWeight = FontWeight.Bold); Text(it) } }
        giveaway.legalTerms?.let { AlertCard { Text("Bases legales", fontWeight = FontWeight.Bold); Text(it) } }
        Button(
            onClick = {
                viewModel.setGiveawayEntry(giveaway.id, !giveaway.entered)
                item = giveaway.copy(entered = !giveaway.entered)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text(if (giveaway.entered) "Retirar participación" else "Participar") }
    }
}

@Composable
fun SurveyDetailScreen(id: String, nav: NavHostController, viewModel: AppViewModel) {
    var survey by remember { mutableStateOf<Survey?>(null) }
    var completed by remember { mutableStateOf(false) }
    val answers = remember { mutableStateMapOf<String, Any>() }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(id) {
        survey = viewModel.loadSurvey(id)
        saved = viewModel.loadSaved().any { it.itemType == "survey" && it.itemId == id }
    }
    if (completed) {
        Scaffold(topBar = { SimpleTopBar("Encuesta", nav) }) { padding ->
            Box(Modifier.padding(padding)) { EmptyState("✅", "¡Gracias!", survey?.thankYouMessage ?: "Tus respuestas se han enviado.", "Volver") { nav.navigateUp() } }
        }
        return
    }
    DetailScaffold("Encuesta", nav, survey) { item ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            IconButton(onClick = { saved = !saved; viewModel.setSaved("survey", item.id, saved) }) {
                Icon(if (saved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Guardar")
            }
        }
        Text(item.introText ?: item.description.orEmpty())
        if (item.anonymous) AlertCard { Text("🔒 Esta encuesta es anónima") }
        item.questions.orEmpty().sortedBy(SurveyQuestion::order).forEach { question ->
            QuestionCard(question, answers)
        }
        val valid = item.questions.orEmpty().filter { it.required }.all { question ->
            when (val answer = answers[question.id]) {
                is String -> answer.isNotBlank()
                is List<*> -> answer.isNotEmpty()
                is Number -> true
                else -> false
            }
        }
        Button(
            onClick = { viewModel.submitSurvey(item.id, answers.toMap()) { completed = true } },
            enabled = valid,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text("Enviar respuestas") }
    }
}

@Composable
private fun QuestionCard(question: SurveyQuestion, answers: MutableMap<String, Any>) {
    AlertCard {
        Text(question.title + if (question.required) " *" else "", fontWeight = FontWeight.Bold)
        question.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        when (question.type) {
            "multiple_choice", "ranking" -> question.options.orEmpty().forEach { option ->
                val current = (answers[question.id] as? List<*>)?.filterIsInstance<String>().orEmpty()
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        answers[question.id] = if (option in current) current - option else current + option
                    },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = option in current, onCheckedChange = null)
                    Text(option)
                }
            }
            "rating", "numeric_scale", "nps", "emoji_sentiment" -> {
                val max = if (question.type == "nps") 10f else 5f
                var value by remember(question.id) { mutableFloatStateOf((answers[question.id] as? Number)?.toFloat() ?: 0f) }
                Text(if (question.type == "emoji_sentiment") listOf("😡", "😕", "😐", "😊", "🤩").getOrElse(value.toInt().coerceIn(0, 4)) { "😐" } else value.toInt().toString(), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                Slider(value = value, onValueChange = { value = it; answers[question.id] = it.toInt() }, valueRange = 0f..max, steps = max.toInt() - 1)
            }
            "yes_no" -> Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("Sí", "No").forEach { option -> FilterChip(selected = answers[question.id] == option, onClick = { answers[question.id] = option }, label = { Text(option) }) }
            }
            "single_choice", "image_choice" -> question.options.orEmpty().forEach { option ->
                FilterChip(selected = answers[question.id] == option, onClick = { answers[question.id] = option }, label = { Text(option) }, modifier = Modifier.fillMaxWidth())
            }
            else -> OutlinedTextField(
                value = answers[question.id] as? String ?: "",
                onValueChange = { answers[question.id] = it },
                label = { Text("Tu respuesta") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        }
    }
}

@Composable
fun OrganizationDetailScreen(id: String, nav: NavHostController, viewModel: AppViewModel) {
    var item by remember { mutableStateOf<Organization?>(null) }
    LaunchedEffect(id) { item = viewModel.loadOrganization(id) }
    DetailScaffold("Organización", nav, item) { organization ->
        CoverImage(organization.coverUrl, organization.name, 190)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            OrganizationAvatar(organization.name, organization.logoUrl, 82)
            Text(organization.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text(organization.category.orEmpty(), color = AlertOrange)
        }
        Button(
            onClick = {
                viewModel.setFollowing(organization.id, !organization.followed)
                item = organization.copy(followed = !organization.followed)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text(if (organization.followed) "Siguiendo" else "Seguir organización") }
        organization.description?.let { Text(it) }
        if (organization.followed) {
            SectionTitle("Canales", "Elige qué quieres recibir")
            organization.channels.orEmpty().forEach { channel ->
                var subscribed by remember(channel.id) { mutableStateOf(channel.subscribed) }
                AlertCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TwoLineText(channel.name, channel.description, Modifier.weight(1f))
                        Switch(
                            checked = subscribed,
                            enabled = !channel.requiresApproval || subscribed,
                            onCheckedChange = { subscribed = it; viewModel.setChannel(channel.id, it) },
                        )
                    }
                }
            }
        }
        listOfNotNull(organization.city, organization.phone, organization.website).forEach { Text(it) }
    }
}

@Composable
fun LoyaltyDetailScreen(id: String, nav: NavHostController, viewModel: AppViewModel) {
    var item by remember { mutableStateOf<LoyaltyProgram?>(null) }
    var code by remember { mutableStateOf<String?>(null) }
    var savedRewardIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(id) {
        item = viewModel.loadLoyalty(id)
        savedRewardIds = viewModel.loadSaved().filter { it.itemType == "loyalty_reward" }.mapTo(mutableSetOf()) { it.itemId }
    }
    DetailScaffold("Programa", nav, item) { program ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            OrganizationAvatar(program.organizationName, program.organizationLogo, 80)
            Text(program.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
            Text("${program.points} puntos", color = AlertOrange, style = MaterialTheme.typography.titleLarge)
        }
        program.description?.let { Text(it) }
        if (program.joined != true) Button(onClick = { viewModel.joinLoyalty(program.id); item = program.copy(joined = true) }, modifier = Modifier.fillMaxWidth()) { Text("Unirme al programa") }
        SectionTitle("Recompensas")
        program.rewards.orEmpty().forEach { reward ->
            AlertCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TwoLineText(reward.title, reward.description, Modifier.weight(1f))
                    IconButton(onClick = {
                        val willSave = reward.id !in savedRewardIds
                        savedRewardIds = if (willSave) savedRewardIds + reward.id else savedRewardIds - reward.id
                        viewModel.setSaved("loyalty_reward", reward.id, willSave)
                    }) {
                        Icon(
                            if (reward.id in savedRewardIds) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar recompensa",
                        )
                    }
                }
                Text("${reward.pointsRequired} puntos", color = AlertOrange, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = { viewModel.redeemReward(reward.id) { code = it.code } },
                    enabled = program.joined == true && program.points >= reward.pointsRequired && reward.active,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Canjear") }
            }
        }
        program.terms?.let { AlertCard { Text("Condiciones", fontWeight = FontWeight.Bold); Text(it) } }
        if (!program.history.isNullOrEmpty()) {
            SectionTitle("Actividad")
            program.history.orEmpty().forEach { transaction -> Text("${transaction.quantity} · ${transaction.note ?: transaction.type}") }
        }
    }
    code?.let { RedemptionDialog(it, onDismiss = { code = null }) }
}

@Composable
fun SavedScreen(nav: NavHostController, viewModel: AppViewModel) {
    var savedItems by remember { mutableStateOf<List<SavedItem>?>(null) }
    var filter by remember { mutableStateOf("all") }
    LaunchedEffect(Unit) { savedItems = viewModel.loadSaved() }
    Scaffold(topBar = { SimpleTopBar("Guardados", nav) }) { padding ->
        Column(Modifier.padding(padding)) {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("all" to "Todo", "notification" to "Alertas", "promotion" to "Ofertas", "survey" to "Encuestas", "reward" to "Recompensas").forEach { pair ->
                            FilterChip(selected = filter == pair.first, onClick = { filter = pair.first }, label = { Text(pair.second) })
                        }
                    }
                }
                val visible = savedItems.orEmpty().filter { filter == "all" || it.type == filter }
                if (savedItems == null) item { LoadingState() }
                else if (visible.isEmpty()) item { EmptyState("🔖", "Nada guardado", "Guarda alertas, ofertas, encuestas o recompensas para verlas aquí.") }
                else items(visible, key = { "${it.type}-${it.id}" }) { saved ->
                    AlertCard(modifier = Modifier.clickable { nav.navigate(routeFor(saved)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TwoLineText(saved.title, saved.subtitle, Modifier.weight(1f))
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = AlertOrange)
                        }
                    }
                }
            }
        }
    }
}

private fun routeFor(item: SavedItem): String = when (item.type) {
    "notification" -> Routes.notification(item.itemId)
    "promotion" -> Routes.promotion(item.itemId)
    "survey" -> Routes.survey(item.itemId)
    "reward" -> Routes.LOYALTY
    else -> Routes.HOME
}

@Composable
private fun <T> DetailScaffold(title: String, nav: NavHostController, item: T?, content: @Composable ColumnScope.(T) -> Unit) {
    Scaffold(topBar = { SimpleTopBar(title, nav) }) { padding ->
        if (item == null) LoadingState()
        else LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) { item { Column(verticalArrangement = Arrangement.spacedBy(18.dp)) { content(item) } } }
    }
}

@Composable
private fun RedemptionDialog(code: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Código de canje") },
        text = { Text(code, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black) },
        confirmButton = {
            TextButton(onClick = { clipboard.setText(AnnotatedString(code)); onDismiss() }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text("Copiar")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

private fun safeHttpsUrl(value: String?): Uri? = value?.let(Uri::parse)?.takeIf { it.scheme == "https" }
