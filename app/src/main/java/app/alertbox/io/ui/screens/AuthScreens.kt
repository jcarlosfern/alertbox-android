package app.alertbox.io.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.alertbox.io.core.auth.PasswordPolicy
import app.alertbox.io.core.model.CitySuggestion
import app.alertbox.io.core.model.ProfileUpdate
import app.alertbox.io.ui.AppViewModel
import app.alertbox.io.ui.components.AlertCard
import app.alertbox.io.ui.components.BrandMark
import app.alertbox.io.ui.components.LoadingState
import app.alertbox.io.ui.theme.AlertOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.delay

@Composable
fun FirebaseConfigurationScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandMark()
        Spacer(Modifier.height(22.dp))
        Text("Falta conectar Firebase", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text(
            "Añade el google-services.json de esta variante desde el proyecto alertbox-42dad. El archivo real permanece fuera de Git.",
            modifier = Modifier.padding(top = 12.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val pages = listOf(
        Triple("🔔", "Solo lo que importa", "Alertas directas de organizaciones que eliges seguir."),
        Triple("🛡️", "Privacidad primero", "Tus datos no se convierten en un perfil público."),
        Triple("🎁", "Más que avisos", "Promociones, encuestas, sorteos y fidelización en un solo lugar."),
    )
    val item = pages[page]
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LinearProgressIndicator(progress = { (page + 1f) / pages.size }, modifier = Modifier.fillMaxWidth(), color = AlertOrange)
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text(item.first, style = MaterialTheme.typography.displayLarge)
            Text(item.second, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(item.third, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        Button(
            onClick = { if (page < pages.lastIndex) page++ else onFinished() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) {
            Text(if (page == pages.lastIndex) "Empezar" else "Continuar")
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun AuthenticationScreen(
    contentPadding: PaddingValues,
    working: Boolean,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String) -> Unit,
    onReset: (String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
) {
    var signUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(contentPadding).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        BrandMark()
        Text(if (signUp) "Crea tu cuenta" else "Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Tus alertas, bajo tu control", color = MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(PasswordPolicy.MAX_LENGTH) },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            supportingText = if (signUp) ({ Text("Mínimo ${PasswordPolicy.MIN_LENGTH} caracteres") }) else null,
            modifier = Modifier.fillMaxWidth(),
        )
        if (!signUp) {
            TextButton(onClick = { onReset(email) }, enabled = email.contains('@') && !working, modifier = Modifier.align(Alignment.End)) {
                Text("He olvidado mi contraseña")
            }
        }
        Button(
            onClick = { if (signUp) onSignUp(email, password) else onSignIn(email, password) },
            enabled = email.contains('@') && password.length >= (if (signUp) PasswordPolicy.MIN_LENGTH else 1) && !working,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text(if (working) "Espera…" else if (signUp) "Crear cuenta" else "Entrar") }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("o", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OutlinedButton(onClick = onGoogleSignIn, enabled = !working, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Continuar con Google") }
        OutlinedButton(onClick = onAppleSignIn, enabled = !working, modifier = Modifier.fillMaxWidth().height(50.dp)) { Text("Continuar con Apple") }
        TextButton(onClick = { signUp = !signUp; password = "" }) {
            Text(if (signUp) "¿Ya tienes cuenta? Entra" else "¿Aún no tienes cuenta? Regístrate")
        }
        Text(
            "Al continuar aceptas las condiciones y la política de privacidad.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun VerifyEmailScreen(email: String, working: Boolean, onRefresh: () -> Unit, onResend: () -> Unit, onSignOut: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.Email, contentDescription = null, tint = AlertOrange)
        Spacer(Modifier.height(18.dp))
        Text("Verifica tu correo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Hemos enviado un enlace a $email", modifier = Modifier.padding(vertical = 12.dp), textAlign = TextAlign.Center)
        Button(onClick = onRefresh, enabled = !working, modifier = Modifier.fillMaxWidth()) { Text("Ya lo he verificado") }
        TextButton(onClick = onResend, enabled = !working) { Text("Enviar de nuevo") }
        TextButton(onClick = onSignOut, enabled = !working) { Text("Usar otra cuenta") }
    }
}

@Composable
fun ProfileSetupScreen(viewModel: AppViewModel, working: Boolean, onComplete: (ProfileUpdate, List<String>) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("1990-01-01") }
    var gender by remember { mutableStateOf("prefer_not_to_say") }
    val interests = remember { mutableStateListOf<String>() }
    val options = listOf("food" to "Comida", "technology" to "Tecnología", "fitness" to "Deporte", "shopping" to "Compras", "travel" to "Viajes", "culture" to "Cultura", "health" to "Salud", "education" to "Educación")
    val valid = when (step) {
        0 -> firstName.isNotBlank() && lastName.isNotBlank() && city.isNotBlank() && country.length == 2
        1 -> country.length == 2 && Regex("\\d{4}-\\d{2}-\\d{2}").matches(birthDate)
        else -> true
    }
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        LinearProgressIndicator(progress = { (step + 1f) / 3f }, modifier = Modifier.fillMaxWidth(), color = AlertOrange)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) IconButton(onClick = { step-- }) { Icon(Icons.Default.ArrowBack, contentDescription = "Atrás") }
            Text("Paso ${step + 1} de 3", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            when (step) {
                0 -> {
                    Text("¿Cómo te llamas?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    OutlinedTextField(firstName, { firstName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(lastName, { lastName = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth())
                    CityAutocompleteField(
                        city = city,
                        countryCode = country,
                        onQueryChange = { city = it; country = "" },
                        onSelect = { suggestion -> city = suggestion.name; country = suggestion.countryCode },
                        viewModel = viewModel,
                    )
                }
                1 -> {
                    Text("Un poco más sobre ti", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    OutlinedTextField(birthDate, { birthDate = it }, label = { Text("Fecha (AAAA-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    Text("País seleccionado: $country", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Género", fontWeight = FontWeight.Bold)
                    listOf("prefer_not_to_say" to "Prefiero no decirlo", "female" to "Mujer", "male" to "Hombre", "non_binary" to "No binario", "other" to "Otro").forEach { item ->
                        FilterChip(selected = gender == item.first, onClick = { gender = item.first }, label = { Text(item.second) }, leadingIcon = if (gender == item.first) ({ Icon(Icons.Default.Check, null) }) else null)
                    }
                }
                else -> {
                    Text("¿Qué te interesa?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Es opcional y puedes cambiarlo más adelante.")
                    options.forEach { item ->
                        FilterChip(
                            selected = item.first in interests,
                            onClick = { if (item.first in interests) interests.remove(item.first) else interests.add(item.first) },
                            label = { Text(item.second) },
                        )
                    }
                    AlertCard { Text("Tus datos se usan para personalizar AlertBox y no crean un perfil público.") }
                }
            }
        }
        Button(
            onClick = {
                if (step < 2) step++ else onComplete(
                    ProfileUpdate(
                        firstName.trim(), lastName.trim(), "$firstName $lastName".trim(), city.trim(), country,
                        gender, birthDate, Locale.getDefault().toLanguageTag().take(10), TimeZone.getDefault().id, true,
                    ),
                    interests.toList(),
                )
            },
            enabled = valid && !working,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AlertOrange),
        ) { Text(if (step == 2) "Terminar" else "Continuar") }
    }
}

@Composable
fun CityAutocompleteField(
    city: String,
    countryCode: String,
    onQueryChange: (String) -> Unit,
    onSelect: (CitySuggestion) -> Unit,
    viewModel: AppViewModel,
) {
    var selectedCity by remember(city) { mutableStateOf(if (countryCode.isNotBlank()) city else "") }
    var suggestions by remember { mutableStateOf(emptyList<CitySuggestion>()) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }

    LaunchedEffect(city) {
        val query = city.trim()
        if (query.length < 2 || query == selectedCity) {
            suggestions = emptyList()
            loading = false
            failed = false
            return@LaunchedEffect
        }
        delay(300)
        loading = true
        failed = false
        runCatching { viewModel.searchCities(query) }
            .onSuccess { suggestions = it }
            .onFailure { suggestions = emptyList(); failed = true }
        loading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        OutlinedTextField(
            value = city,
            onValueChange = { selectedCity = ""; onQueryChange(it) },
            label = { Text("Ciudad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = { if (loading) CircularProgressIndicator(modifier = Modifier.width(20.dp).height(20.dp), strokeWidth = 2.dp) },
            supportingText = {
                Text(if (countryCode.isNotBlank()) "País seleccionado: $countryCode" else "Escribe al menos 2 caracteres y selecciona una ciudad.")
            },
        )
        if (failed) {
            Text("La búsqueda de ciudades no está disponible temporalmente.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        } else {
            suggestions.forEach { suggestion ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        selectedCity = suggestion.name
                        suggestions = emptyList()
                        onSelect(suggestion)
                    }.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("📍")
                    Column {
                        Text(suggestion.name, fontWeight = FontWeight.SemiBold)
                        Text(listOf(suggestion.region, suggestion.country).filter(String::isNotBlank).joinToString(", "), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
