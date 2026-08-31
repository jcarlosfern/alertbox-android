package app.alertbox.io.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavHostController
import app.alertbox.io.core.model.PreferencesUpdate
import app.alertbox.io.core.model.ProfileUpdate
import app.alertbox.io.core.model.QuietHours
import app.alertbox.io.core.model.TravelMode
import app.alertbox.io.ui.AppUiState
import app.alertbox.io.ui.AppViewModel
import app.alertbox.io.ui.components.AlertCard
import app.alertbox.io.ui.components.AvatarCropDialog
import app.alertbox.io.ui.components.BrandMark
import app.alertbox.io.ui.components.OrganizationAvatar
import app.alertbox.io.ui.components.loadAvatarBitmap
import app.alertbox.io.ui.theme.AlertOrange
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    state: AppUiState,
    padding: PaddingValues,
    nav: NavHostController,
    viewModel: AppViewModel,
) {
    val profile = state.profile
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        AlertCard {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                OrganizationAvatar(profile?.greetingName ?: "AB", profile?.avatarUrl, 58, crop = true)
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile?.displayName ?: profile?.greetingName ?: "Mi cuenta", fontWeight = FontWeight.Bold)
                    Text(profile?.email.orEmpty(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (profile?.emailVerified == true) Text("Correo verificado", color = Color(0xFF16864B), style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        SettingsGroup {
            SettingsItem(Icons.Default.Notifications, "Notificaciones", "Push, correo y horas de descanso") { nav.navigate(Routes.NOTIFICATION_SETTINGS) }
            SettingsItem(Icons.Default.AccountCircle, "Datos personales", "Nombre, ubicación, fecha de nacimiento y género") { nav.navigate(Routes.PROFILE_SETTINGS) }
            SettingsItem(Icons.Default.Bookmark, "Guardados", "Avisos y promociones que conservas") { nav.navigate(Routes.SAVED) }
        }
        SettingsGroup {
            SettingsItem(Icons.Default.Language, "Idioma", "Idioma preferido para tus comunicaciones") { nav.navigate(Routes.LANGUAGE_SETTINGS) }
            SettingsItem(Icons.Default.Shield, "Privacidad y seguridad", "Control de cuenta y eliminación") { nav.navigate(Routes.PRIVACY_SETTINGS) }
            PreferenceSwitch(
                "Modo oscuro",
                "Apariencia con menor luminosidad",
                state.darkMode ?: isSystemInDarkTheme(),
                leadingIcon = Icons.Default.DarkMode,
                onCheckedChange = viewModel::setDarkMode,
            )
            SettingsItem(Icons.Default.Help, "Ayuda", "Preguntas frecuentes y soporte") { nav.navigate(Routes.HELP) }
            SettingsItem(Icons.Default.Gavel, "Información legal", "Privacidad, condiciones y licencias") { nav.navigate(Routes.LEGAL) }
        }
        OutlinedButton(onClick = viewModel::signOut, modifier = Modifier.fillMaxWidth(), enabled = !state.working) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Text("Cerrar sesión", modifier = Modifier.padding(start = 8.dp))
        }
        Text(
            "AlertBox para Android · 0.1.0",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column { content() }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null, tint = AlertOrange) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = "Abrir $title") },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationPreferencesScreen(
    state: AppUiState,
    nav: NavHostController,
    viewModel: AppViewModel,
    notificationsGranted: Boolean,
    onRequestNotifications: () -> Unit,
) {
    val preferences = state.profile?.preferences
    var push by rememberSaveable(preferences) { mutableStateOf(preferences?.pushEnabled ?: false) }
    var email by rememberSaveable(preferences) { mutableStateOf(preferences?.emailEnabled ?: true) }
    var privateMessages by rememberSaveable(preferences) { mutableStateOf(preferences?.privateMessagesEnabled ?: true) }
    var marketing by rememberSaveable(preferences) { mutableStateOf(preferences?.marketingEnabled ?: false) }
    var quietEnabled by rememberSaveable(preferences) { mutableStateOf(preferences?.quietHours?.enabled ?: false) }
    var quietStart by rememberSaveable(preferences) { mutableStateOf(preferences?.quietHours?.start ?: "22:00") }
    var quietEnd by rememberSaveable(preferences) { mutableStateOf(preferences?.quietHours?.end ?: "08:00") }
    var allowCritical by rememberSaveable(preferences) { mutableStateOf(preferences?.quietHours?.allowCritical ?: true) }
    var travelMode by rememberSaveable(preferences) { mutableStateOf(preferences?.travelMode?.enabled ?: false) }
    var quietDays by rememberSaveable(preferences) {
        mutableStateOf(preferences?.quietHours?.days?.joinToString(",") ?: "Mon,Tue,Wed,Thu,Fri")
    }
    var destination by rememberSaveable(preferences) { mutableStateOf(preferences?.travelMode?.destination.orEmpty()) }
    var returnDate by rememberSaveable(preferences) { mutableStateOf(preferences?.travelMode?.returnDate.orEmpty()) }

    Scaffold(topBar = { SimpleTopBar("Notificaciones", nav) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PreferenceSwitch("Notificaciones push", "Avisos importantes en este dispositivo", push) { enabled ->
                if (enabled && !notificationsGranted) onRequestNotifications()
                push = enabled
            }
            PreferenceSwitch("Correo electrónico", "Resumen y comunicaciones de tus suscripciones", email) { email = it }
            PreferenceSwitch("Mensajes privados", "Mensajes directos de organizaciones", privateMessages) { privateMessages = it }
            PreferenceSwitch("Novedades comerciales", "Ofertas opcionales; desactivadas por defecto", marketing) { marketing = it }
            Divider()
            PreferenceSwitch("Horas de descanso", "Silencia avisos durante una franja horaria", quietEnabled) { quietEnabled = it }
            if (quietEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(quietStart, { quietStart = it.take(5) }, label = { Text("Desde") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(quietEnd, { quietEnd = it.take(5) }, label = { Text("Hasta") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                PreferenceSwitch("Permitir avisos críticos", "Emergencias y comunicaciones imprescindibles", allowCritical) { allowCritical = it }
                Text("Días activos", fontWeight = FontWeight.SemiBold)
                listOf(
                    listOf("Mon" to "L", "Tue" to "M", "Wed" to "X", "Thu" to "J"),
                    listOf("Fri" to "V", "Sat" to "S", "Sun" to "D"),
                ).forEach { weekRow ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        weekRow.forEach { (code, label) ->
                            val active = code in quietDays.split(",").filter(String::isNotBlank)
                            androidx.compose.material3.FilterChip(
                                selected = active,
                                onClick = {
                                    val selected = quietDays.split(",").filter(String::isNotBlank).toMutableSet()
                                    if (active) selected.remove(code) else selected.add(code)
                                    quietDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                        .filter(selected::contains).joinToString(",")
                                },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
            PreferenceSwitch("Modo viaje", "Usa la zona horaria actual del dispositivo", travelMode) { travelMode = it }
            if (travelMode) {
                OutlinedTextField(destination, { destination = it }, label = { Text("Destino") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(
                    returnDate,
                    { returnDate = it.take(10) },
                    label = { Text("Fecha de regreso") },
                    supportingText = { Text("Formato AAAA-MM-DD") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text("Los avisos no urgentes se agruparán cada 6 horas; las encuestas se pausarán y los avisos críticos seguirán llegando.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(
                onClick = {
                    val timePattern = Regex("^(?:[01]\\d|2[0-3]):[0-5]\\d$")
                    val datePattern = Regex("^\\d{4}-\\d{2}-\\d{2}$")
                    if (quietEnabled && (!timePattern.matches(quietStart) || !timePattern.matches(quietEnd))) {
                        viewModel.showError("Introduce las horas con formato HH:mm.")
                    } else if (travelMode && returnDate.isNotBlank() && !datePattern.matches(returnDate)) {
                        viewModel.showError("La fecha de regreso debe tener formato AAAA-MM-DD.")
                    } else {
                        viewModel.updatePreferences(
                            PreferencesUpdate(
                                pushEnabled = push,
                                emailEnabled = email,
                                privateMessagesEnabled = privateMessages,
                                marketingEnabled = marketing,
                                quietHours = QuietHours(
                                    enabled = quietEnabled,
                                    start = quietStart,
                                    end = quietEnd,
                                    days = quietDays.split(",").filter(String::isNotBlank),
                                    criticalOnly = preferences?.quietHours?.criticalOnly,
                                    allowCritical = allowCritical,
                                ),
                                travelMode = TravelMode(
                                    enabled = travelMode,
                                    timezone = TimeZone.getDefault().id,
                                    destination = destination.takeIf(String::isNotBlank),
                                    returnDate = returnDate.takeIf(String::isNotBlank),
                                    batchHours = 6,
                                    pauseSurveys = true,
                                    criticalOnly = true,
                                ),
                                interests = preferences?.interests,
                            ),
                            notificationsGranted,
                        )
                    }
                },
                enabled = !state.working,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
            ) { Text("Guardar preferencias") }
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    leadingIcon: ImageVector? = null,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        leadingIcon?.let { Icon(it, contentDescription = null, tint = AlertOrange, modifier = Modifier.padding(horizontal = 16.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(state: AppUiState, nav: NavHostController, viewModel: AppViewModel) {
    val languages = listOf(
        "es" to "Español", "en" to "English", "fr" to "Français", "de" to "Deutsch",
        "pt" to "Português", "it" to "Italiano", "ja" to "日本語", "zh" to "中文",
        "ko" to "한국어", "ar" to "العربية",
    )
    var selected by rememberSaveable(state.profile?.locale) {
        mutableStateOf(state.profile?.locale?.substringBefore("-") ?: "es")
    }
    Scaffold(topBar = { SimpleTopBar("Idioma", nav) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("El idioma se guarda en tu perfil y se utiliza para las comunicaciones de AlertBox.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                languages.forEach { (code, name) ->
                    ListItem(
                        headlineContent = { Text(name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(code.uppercase(Locale.ROOT)) },
                        trailingContent = {
                            if (selected == code) Icon(Icons.Default.Check, contentDescription = "Seleccionado", tint = AlertOrange)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            selected = code
                            viewModel.updateLocale(code)
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDataScreen(state: AppUiState, nav: NavHostController, viewModel: AppViewModel) {
    val profile = state.profile
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable(profile) { mutableStateOf(profile?.firstName.orEmpty()) }
    var lastName by rememberSaveable(profile) { mutableStateOf(profile?.lastName.orEmpty()) }
    var city by rememberSaveable(profile) { mutableStateOf(profile?.city.orEmpty()) }
    var countryCode by rememberSaveable(profile) { mutableStateOf(profile?.countryCode.orEmpty()) }
    var birthDate by rememberSaveable(profile) { mutableStateOf(profile?.birthDate.orEmpty()) }
    var gender by rememberSaveable(profile) { mutableStateOf(profile?.gender ?: "prefer_not_to_say") }
    var cropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var avatarPreview by remember { mutableStateOf<Bitmap?>(null) }
    var avatarBytes by remember { mutableStateOf<ByteArray?>(null) }
    var countryMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            val bitmap = loadAvatarBitmap(context, uri)
            if (bitmap == null) viewModel.showError("No se pudo leer la imagen seleccionada.") else cropBitmap = bitmap
        }
    }
    val countries = remember {
        Locale.getISOCountries().map { code ->
            code to Locale.Builder().setRegion(code).build().getDisplayCountry(Locale.getDefault())
        }.sortedBy { it.second.lowercase(Locale.getDefault()) }
    }

    Scaffold(topBar = { SimpleTopBar("Datos personales", nav) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AlertCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (avatarPreview != null) {
                        Image(
                            bitmap = requireNotNull(avatarPreview).asImageBitmap(),
                            contentDescription = "Foto de perfil seleccionada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(82.dp).clip(RoundedCornerShape(28.dp)),
                        )
                    } else {
                        OrganizationAvatar(profile?.greetingName ?: "AB", profile?.avatarUrl, 82, crop = true)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(onClick = { photoPicker.launch("image/*") }) {
                            Text("Elegir y recortar foto")
                        }
                        Text(
                            "JPG, PNG o WebP. Recomendado: 600×600 px (mínimo 256×256 px). Se guardará en Cloudflare R2.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text("Tu correo se gestiona desde Firebase Authentication y no puede cambiarse desde esta pantalla.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(firstName, { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            OutlinedTextField(lastName, { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next))
            CityAutocompleteField(
                city = city,
                countryCode = countryCode,
                onQueryChange = { city = it; countryCode = "" },
                onSelect = { suggestion -> city = suggestion.name; countryCode = suggestion.countryCode },
                viewModel = viewModel,
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { countryMenuExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    val selectedCountry = countries.firstOrNull { it.first == countryCode }?.second
                    Text(selectedCountry ?: "Selecciona un país")
                }
                DropdownMenu(
                    expanded = countryMenuExpanded,
                    onDismissRequest = { countryMenuExpanded = false },
                ) {
                    countries.forEach { (code, name) ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                countryCode = code
                                countryMenuExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                birthDate,
                { birthDate = it.take(10) },
                label = { Text("Fecha de nacimiento") },
                supportingText = { Text("Formato AAAA-MM-DD") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            )
            Text("Género", fontWeight = FontWeight.Bold)
            listOf(
                "prefer_not_to_say" to "Prefiero no decirlo",
                "female" to "Mujer",
                "male" to "Hombre",
                "non_binary" to "No binario",
                "other" to "Otro",
            ).forEach { (value, label) ->
                androidx.compose.material3.FilterChip(
                    selected = gender == value,
                    onClick = { gender = value },
                    label = { Text(label) },
                    leadingIcon = if (gender == value) ({ Icon(Icons.Default.Check, contentDescription = null) }) else null,
                )
            }
            Text(
                "La fecha de nacimiento y el género no se muestran públicamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = {
                    val validDate = birthDate.isBlank() || Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(birthDate)
                    if (firstName.isBlank() || lastName.isBlank()) viewModel.showError("Nombre y apellidos son obligatorios.")
                    else if (city.isBlank() || countryCode.length != 2) viewModel.showError("Selecciona una ciudad de la lista.")
                    else if (!validDate) viewModel.showError("La fecha debe tener formato AAAA-MM-DD.")
                    else viewModel.updateProfile(
                        ProfileUpdate(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            displayName = "$firstName $lastName".trim(),
                            city = city.trim(),
                            countryCode = countryCode,
                            gender = gender,
                            birthDate = birthDate,
                            locale = profile?.locale ?: Locale.getDefault().toLanguageTag(),
                            timezone = TimeZone.getDefault().id,
                            onboardingComplete = true,
                        ),
                        avatarBytes,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.working,
                colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
            ) { Text("Guardar datos") }
        }
    }
    cropBitmap?.let { bitmap ->
        AvatarCropDialog(
            bitmap = bitmap,
            onDismiss = { cropBitmap = null },
            onApply = { data, preview ->
                avatarBytes = data
                avatarPreview = preview
                cropBitmap = null
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(nav: NavHostController, viewModel: AppViewModel) {
    var showDelete by rememberSaveable { mutableStateOf(false) }
    var confirmation by rememberSaveable { mutableStateOf("") }
    Scaffold(topBar = { SimpleTopBar("Privacidad y seguridad", nav) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandMark(modifier = Modifier.align(Alignment.CenterHorizontally), size = 56)
            Text("Tus credenciales se procesan mediante Firebase Authentication. AlertBox no guarda tu contraseña y la comunicación con la API exige HTTPS.")
            AlertCard {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = AlertOrange)
                    Column {
                        Text("Protección de la cuenta", fontWeight = FontWeight.Bold)
                        Text("Usa una contraseña única y mantén verificado tu correo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Text("Eliminar la cuenta borra el acceso y solicita la eliminación del perfil. Firebase puede pedirte que vuelvas a iniciar sesión si la autenticación no es reciente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("Eliminar mi cuenta") }
        }
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false; confirmation = "" },
            title = { Text("Eliminar cuenta definitivamente") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Esta acción no se puede deshacer. Escribe ELIMINAR para continuar.")
                    OutlinedTextField(confirmation, { confirmation = it }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDelete = false; viewModel.deleteAccount() }, enabled = confirmation == "ELIMINAR") {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDelete = false; confirmation = "" }) { Text("Cancelar") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(nav: NavHostController) {
    InfoScreen("Ayuda", nav) {
        InfoSection("¿Cómo recibo avisos?", "Sigue una organización y suscríbete a sus canales. Activa las notificaciones push desde Ajustes para recibirlas en tiempo real.")
        InfoSection("No recibo notificaciones", "Comprueba el permiso de Android, que el canal esté suscrito y que no coincida con tus horas de descanso.")
        InfoSection("¿Cómo canjeo una promoción?", "Abre la promoción, revisa sus condiciones y pulsa Canjear. No compartas el código antes de usarlo.")
        InfoSection("Soporte", "Escríbenos a soporte@alertbox.app indicando la versión de Android y una descripción del problema. Nunca envíes tu contraseña.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(nav: NavHostController) {
    InfoScreen("Información legal", nav) {
        InfoSection("Privacidad", "AlertBox usa los datos necesarios para identificarte, gestionar tus suscripciones y entregar comunicaciones. Puedes retirar consentimientos opcionales desde Notificaciones y eliminar tu cuenta desde Privacidad.")
        InfoSection("Condiciones de uso", "Las organizaciones son responsables del contenido, promociones, sorteos y programas que publican. Revisa siempre sus condiciones específicas antes de participar o canjear una ventaja.")
        InfoSection("Seguridad", "El acceso usa Firebase Authentication y las comunicaciones se realizan mediante conexiones cifradas. No compartas códigos de acceso, verificación o canje.")
        InfoSection("Licencias", "La aplicación incorpora componentes de código abierto sujetos a sus respectivas licencias. © 2026 AlertBox.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoScreen(title: String, nav: NavHostController, content: @Composable () -> Unit) {
    Scaffold(topBar = { SimpleTopBar(title, nav) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) { content() }
    }
}

@Composable
private fun InfoSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
