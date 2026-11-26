package et.hotelbooking.app.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.hotelbooking.app.R
import et.hotelbooking.app.data.repo.DomainException
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.data.seed.Seed
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.vm.AppViewModel
import kotlinx.coroutines.launch

@Composable
fun errorText(e: Throwable?): String = when ((e as? DomainException)?.code ?: e?.message) {
    "invalid_credentials" -> stringResource(R.string.err_invalid_credentials)
    "account_disabled" -> stringResource(R.string.err_account_disabled)
    "email_taken" -> stringResource(R.string.err_email_taken)
    "sold_out" -> stringResource(R.string.err_sold_out)
    "invalid_dates" -> stringResource(R.string.err_dates)
    "capacity" -> stringResource(R.string.err_capacity)
    "cannot_cancel" -> stringResource(R.string.err_cannot_cancel)
    "too_early" -> stringResource(R.string.err_too_early)
    "id_required" -> stringResource(R.string.err_id_number)
    "room_unavailable" -> stringResource(R.string.err_room_unavailable)
    "room_exists" -> stringResource(R.string.err_room_exists)
    "room_in_use" -> stringResource(R.string.err_room_in_use)
    else -> stringResource(R.string.error_generic)
}

@Composable
fun LoginScreen(vm: AppViewModel, onBack: (() -> Unit)?, onRegister: () -> Unit, onDone: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var pw by rememberSaveable { mutableStateOf("") }
    var show by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<Throwable?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(200.dp)) {
            Image(painterResource(R.drawable.lm_gondar), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.7f)))))
            if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding()) { Icon(Icons.Outlined.Close, stringResource(R.string.back), tint = Color.White) }
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                Text(stringResource(R.string.welcome_back), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(stringResource(R.string.login_sub), color = Color.White.copy(alpha = 0.9f))
            }
        }
        TibebStrip()
        Column(Modifier.padding(20.dp)) {
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.email)) }, singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Email, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.password)) }, singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                trailingIcon = { IconButton(onClick = { show = !show }) { Icon(if (show) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null) } },
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            if (err != null) Text(errorText(err), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(16.dp))
            Button(onClick = { scope.launch { busy = true; err = null; vm.login(email, pw).onSuccess { onDone() }.onFailure { err = it }; busy = false } },
                enabled = !busy && email.isNotBlank() && pw.isNotBlank(), modifier = Modifier.fillMaxWidth().height(52.dp)) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp) else Text(stringResource(R.string.sign_in))
            }
            TextButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.no_account_register)) }
            Spacer(Modifier.height(12.dp))
            SectionCard(title = stringResource(R.string.demo_accounts)) {
                Text(stringResource(R.string.demo_accounts_sub), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Seed.demoAccounts.forEach { a ->
                    ListItem(headlineContent = { Text(stringResource(Labels.role(a.role)) + if (a.email.startsWith("tourist")) " · " + stringResource(R.string.mode_tourist) else "") },
                        supportingContent = { Text(a.email + "  /  " + a.password) },
                        leadingContent = { Icon(Icons.Outlined.AccountCircle, null) },
                        modifier = Modifier.padding(vertical = 0.dp), colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        trailingContent = { TextButton(onClick = { email = a.email; pw = a.password }) { Text(stringResource(R.string.use)) } })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(vm: AppViewModel, onBack: () -> Unit, onDone: () -> Unit, onPrivacy: () -> Unit) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var asManager by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf(if (settings.mode == UserMode.LOCAL) "+251 " else "") }
    var pw by rememberSaveable { mutableStateOf("") }
    var pw2 by rememberSaveable { mutableStateOf("") }
    var hotelName by rememberSaveable { mutableStateOf("") }
    var hotelCity by rememberSaveable { mutableStateOf("addis_ababa") }
    var stars by rememberSaveable { mutableIntStateOf(3) }
    val id = remember(settings.mode) { IdFormState(settings.mode) }
    var errRes by remember { mutableStateOf<Int?>(null) }
    var err by remember { mutableStateOf<Throwable?>(null) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { AppTopBar(stringResource(R.string.create_account), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp).imePadding(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(!asManager, { asManager = false }, SegmentedButtonDefaults.itemShape(0, 2), icon = {}) { Text(stringResource(R.string.acct_traveller)) }
                SegmentedButton(asManager, { asManager = true }, SegmentedButtonDefaults.itemShape(1, 2), icon = {}) { Text(stringResource(R.string.acct_hotel_owner)) }
            }
            Text(stringResource(if (settings.mode == UserMode.LOCAL) R.string.registering_as_local else R.string.registering_as_tourist), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.full_name)) }, singleLine = true)
            OutlinedTextField(email, { email = it.trim() }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.email)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.phone)) }, singleLine = true,
                supportingText = { Text(stringResource(if (settings.mode == UserMode.LOCAL) R.string.phone_hint_local else R.string.phone_hint_intl)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(pw, { pw = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation(),
                supportingText = { Text(stringResource(R.string.password_rule)) })
            OutlinedTextField(pw2, { pw2 = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.confirm_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
            if (asManager) {
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Text(stringResource(R.string.your_hotel), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(hotelName, { hotelName = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.hotel_name)) }, singleLine = true)
                CityDropdown(hotelCity, { hotelCity = it ?: "addis_ababa" }, allowAll = false)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.star_rating), Modifier.weight(1f))
                    (1..5).forEach { s -> FilterChip(stars == s, { stars = s }, label = { Text("$s★") }, modifier = Modifier.padding(horizontal = 2.dp)) }
                }
                Text(stringResource(R.string.hotel_pending_note), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            IdCaptureSection(id, settings.mode, onPrivacy)
            if (errRes != null) Text(stringResource(errRes!!), color = MaterialTheme.colorScheme.error)
            if (err != null) Text(errorText(err), color = MaterialTheme.colorScheme.error)
            Button(onClick = {
                err = null
                errRes = when {
                    name.isBlank() -> R.string.err_name
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> R.string.err_email
                    !validPhone(phone, settings.mode) -> R.string.err_phone
                    pw.length < 8 || pw.none { it.isDigit() } || pw.none { it.isLetter() } -> R.string.password_rule
                    pw != pw2 -> R.string.err_password_match
                    asManager && hotelName.isBlank() -> R.string.err_hotel_name
                    else -> id.validate()
                }
                if (errRes == null) scope.launch {
                    busy = true
                    vm.register(name, email, phone.replace(" ", ""), pw, id.nationality, asManager, hotelName, hotelCity, stars, id.toDocument()).onSuccess { onDone() }.onFailure { err = it }
                    busy = false
                }
            }, enabled = !busy, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text(stringResource(R.string.create_account)) }
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun validPhone(p: String, mode: UserMode): Boolean {
    val d = p.replace(" ", "").replace("-", "")
    return if (mode == UserMode.LOCAL) Regex("^(\\+251|0)?[79]\\d{8}$").matches(d) else Regex("^\\+?\\d{7,15}$").matches(d)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdown(value: String?, onChange: (String?) -> Unit, allowAll: Boolean = true, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(open, { open = it }, modifier) {
        OutlinedTextField(value?.let { stringResource(CityInfo.nameRes(it)) } ?: stringResource(R.string.all_cities), {}, readOnly = true,
            label = { Text(stringResource(R.string.city)) }, leadingIcon = { Icon(Icons.Outlined.Place, null) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(open) },
            modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))
        ExposedDropdownMenu(open, { open = false }) {
            if (allowAll) DropdownMenuItem({ Text(stringResource(R.string.all_cities)) }, { onChange(null); open = false })
            CityInfo.cities.forEach { c ->
                DropdownMenuItem({ Text(stringResource(c.name) + " · " + stringResource(CityInfo.regionName(c.region))) }, { onChange(c.key); open = false })
            }
        }
    }
}
