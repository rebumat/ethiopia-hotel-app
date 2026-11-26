package et.hotelbooking.app.ui.screens.common

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.hotelbooking.app.BuildConfig
import et.hotelbooking.app.R
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.data.seed.Credits
import et.hotelbooking.app.data.seed.Landmarks
import et.hotelbooking.app.domain.Role
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.domain.VerificationStatus
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.onboarding.LanguageList
import et.hotelbooking.app.ui.screens.onboarding.ModeCards
import et.hotelbooking.app.ui.theme.Brand
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: AppViewModel, onLogin: () -> Unit, onCredits: () -> Unit, onAbout: () -> Unit, onPrivacy: () -> Unit, onIdUpload: () -> Unit, onSaved: () -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val savedIds by vm.savedIds.collectAsStateWithLifecycle()
    var langDialog by remember { mutableStateOf(false) }
    var modeDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Brand.GreenDark, Brand.Green))).statusBarsPadding().padding(20.dp)) {
            val u = user
            if (u == null) Column {
                Text(stringResource(R.string.profile_guest_title), style = MaterialTheme.typography.headlineSmall, color = Color.White)
                Text(stringResource(R.string.profile_guest_sub), color = Color.White.copy(alpha = 0.85f))
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogin, colors = ButtonDefaults.buttonColors(containerColor = Brand.Gold, contentColor = Brand.Ink)) { Text(stringResource(R.string.sign_in)) }
            } else Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).clip(CircleShape).background(Brand.Gold), contentAlignment = Alignment.Center) {
                    Text(u.fullName.split(' ').mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""), style = MaterialTheme.typography.titleLarge, color = Brand.Ink)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(u.fullName, style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text(u.email, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(stringResource(Labels.role(u.role)), Color.White)
                        if (u.role == Role.CLIENT || u.verification != VerificationStatus.NONE) Pill(stringResource(Labels.verification(u.verification)), if (u.verification == VerificationStatus.VERIFIED) Brand.GoldSoft else Color.White)
                    }
                }
            }
        }
        TibebStrip(height = 6.dp)
        if (user != null) {
            SettingRow(Icons.Outlined.Badge, stringResource(R.string.my_id_document), user!!.idType?.let { stringResource(Labels.idType(it)) + " · " + IdImageStore.mask(user!!.idNumber) } ?: stringResource(R.string.not_provided), onIdUpload)
        }
        SettingRow(Icons.Outlined.Favorite, stringResource(R.string.saved_hotels),
            if (savedIds.isEmpty()) null else stringResource(R.string.saved_count, savedIds.size), onSaved)
        SettingsHeader(stringResource(R.string.preferences))
        SettingRow(Icons.Outlined.Translate, stringResource(R.string.language), Languages.all.firstOrNull { Languages.isCurrent(it.tag) }?.nativeName ?: "English") { langDialog = true }
        SettingRow(if (s.mode == UserMode.LOCAL) Icons.Outlined.Home else Icons.Outlined.FlightLand, stringResource(R.string.user_mode), stringResource(if (s.mode == UserMode.LOCAL) R.string.mode_local else R.string.mode_tourist)) { modeDialog = true }
        Column(Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
            Text(stringResource(R.string.display_currency), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                Currency.entries.forEachIndexed { i, c -> SegmentedButton(s.currency == c, { vm.setCurrency(c) }, SegmentedButtonDefaults.itemShape(i, Currency.entries.size)) { Text(c.code) } }
            }
            Text(stringResource(R.string.sample_rate_note, Rates.ETB_PER_USD.toString(), Rates.AS_OF), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(10.dp))
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.labelLarge)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                listOf(ThemeMode.SYSTEM to R.string.theme_system, ThemeMode.LIGHT to R.string.theme_light, ThemeMode.DARK to R.string.theme_dark).forEachIndexed { i, (t, l) ->
                    SegmentedButton(s.theme == t, { vm.setTheme(t) }, SegmentedButtonDefaults.itemShape(i, 3)) { Text(stringResource(l), maxLines = 1) }
                }
            }
        }
        ListItem(headlineContent = { Text(stringResource(R.string.ethiopian_calendar)) }, supportingContent = { Text(stringResource(R.string.ethiopian_calendar_sub)) },
            leadingContent = { Icon(Icons.Outlined.CalendarMonth, null) }, trailingContent = { Switch(s.showEthiopianCalendar, { vm.setEthCalendar(it) }) })
        SettingsHeader(stringResource(R.string.about_section))
        SettingRow(Icons.Outlined.Info, stringResource(R.string.about_app), "v" + BuildConfig.VERSION_NAME, onAbout)
        SettingRow(Icons.Outlined.PrivacyTip, stringResource(R.string.privacy_policy), null, onPrivacy)
        SettingRow(Icons.Outlined.PhotoLibrary, stringResource(R.string.credits), null, onCredits)
        if (user != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = { vm.logout() }, Modifier.padding(20.dp).fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.AutoMirrored.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.sign_out))
            }
        }
        DemoBanner(stringResource(R.string.demo_note_short), Modifier.padding(20.dp))
    }
    if (langDialog) AlertDialog(onDismissRequest = { langDialog = false }, confirmButton = { TextButton(onClick = { langDialog = false }) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.language)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { LanguageList(s.mode) } })
    if (modeDialog) AlertDialog(onDismissRequest = { modeDialog = false }, confirmButton = { TextButton(onClick = { modeDialog = false }) { Text(stringResource(R.string.close)) } },
        title = { Text(stringResource(R.string.user_mode)) }, text = { Column(Modifier.verticalScroll(rememberScrollState())) { ModeCards(s.mode) { vm.setMode(it) } } })
}

@Composable
private fun SettingsHeader(t: String) = Text(t, Modifier.padding(start = 20.dp, top = 16.dp, bottom = 4.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

@Composable
private fun SettingRow(icon: ImageVector, title: String, value: String?, onClick: () -> Unit) {
    ListItem(headlineContent = { Text(title) }, supportingContent = value?.let { { Text(it) } }, leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.Outlined.ChevronRight, null) }, modifier = Modifier.clickableRow(onClick))
}

fun Modifier.clickableRow(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)

@Composable
fun LandmarkScreen(key: String, onBack: () -> Unit, onFindHotels: (String) -> Unit) {
    val l = Landmarks.all.firstOrNull { it.key == key } ?: return
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(380.dp)) {
            Image(painterResource(l.image), stringResource(l.name), Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color.Black.copy(alpha = 0.8f)))))
            FilledTonalIconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(8.dp)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                if (l.unesco) Pill(stringResource(R.string.unesco_full), Brand.GoldSoft)
                Text(stringResource(l.name), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(stringResource(CityInfo.nameRes(l.nearCity)) + " · " + stringResource(CityInfo.regionName(CityInfo.regionOf(l.nearCity))), color = Brand.GoldSoft)
            }
        }
        TibebStrip(height = 6.dp)
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(l.desc), style = MaterialTheme.typography.bodyLarge)
            Button(onClick = { onFindHotels(l.nearCity) }, Modifier.fillMaxWidth().height(50.dp)) { Icon(Icons.Outlined.Hotel, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.hotels_near, stringResource(CityInfo.nameRes(l.nearCity)))) }
            OutlinedButton(onClick = { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${l.lat},${l.lng}?q=${l.lat},${l.lng}"))) }
                .onFailure { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${l.lat}&mlon=${l.lng}#map=12/${l.lat}/${l.lng}"))) } } }, Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.Map, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.view_on_map))
            }
            val credit = Credits.images.firstOrNull { it.drawable == ctx.resources.getResourceEntryName(l.image) }
            if (credit != null) Text(stringResource(R.string.photo_by, credit.author, credit.license), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    Scaffold(topBar = { AppTopBar(stringResource(R.string.credits), onBack) }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp)) {
            item { Text(stringResource(R.string.credits_intro), style = MaterialTheme.typography.bodyMedium); Spacer(Modifier.height(8.dp)) }
            items(Credits.images) { c ->
                ListItem(leadingContent = { HotelImage(c.drawable, Modifier.size(56.dp).clip(MaterialTheme.shapes.small)) },
                    headlineContent = { Text(c.subject) }, supportingContent = { Text(c.fileTitle + "\n" + c.author + " · " + c.license, style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.clickableRow { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(c.url))) } })
            }
            item { Text(stringResource(R.string.fonts), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) }
            items(Credits.fonts) { Text("• $it", style = MaterialTheme.typography.bodySmall) }
            item { Text(stringResource(R.string.open_source), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp)) }
            items(Credits.libraries) { Text("• $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = { AppTopBar(stringResource(R.string.about_app), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Image(painterResource(R.drawable.lm_axum), null, Modifier.fillMaxWidth().height(180.dp).clip(MaterialTheme.shapes.large), contentScale = ContentScale.Crop)
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.version_x, BuildConfig.VERSION_NAME), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.about_body))
            SectionCard(title = stringResource(R.string.sample_data_title)) { Text(stringResource(R.string.sample_data_body)) }
            SectionCard(title = stringResource(R.string.demo_payments_title)) { Text(stringResource(R.string.demo_payments_body)) }
        }
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    Scaffold(topBar = { AppTopBar(stringResource(R.string.privacy_policy), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(R.string.privacy_p1, R.string.privacy_p2, R.string.privacy_p3, R.string.privacy_p4, R.string.privacy_p5).forEach { Text(stringResource(it), style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
fun IdUploadScreen(vm: AppViewModel, onBack: () -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val u = user ?: return
    val mode = if (u.idType == et.hotelbooking.app.domain.IdType.PASSPORT || (u.idType == null && s.mode == UserMode.TOURIST)) UserMode.TOURIST else UserMode.LOCAL
    var editMode by remember { mutableStateOf(mode) }
    val form = remember(editMode) { IdFormState(editMode).apply { nationality = u.nationality.ifBlank { nationality } } }
    var err by remember { mutableStateOf<Int?>(null) }
    var saved by remember { mutableStateOf(false) }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.my_id_document), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionCard(title = stringResource(R.string.id_on_file)) {
                if (u.idType == null) Text(stringResource(R.string.not_provided))
                else {
                    KeyValue(stringResource(R.string.document), stringResource(Labels.idType(u.idType)))
                    KeyValue(stringResource(R.string.id_number), IdImageStore.mask(u.idNumber))
                    u.idExpiry?.let { KeyValue(stringResource(R.string.passport_expiry), Dates.medium(it)) }
                    KeyValue(stringResource(R.string.verification_status), stringResource(Labels.verification(u.verification)))
                }
            }
            Text(stringResource(R.string.update_id), style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(editMode == UserMode.LOCAL, { editMode = UserMode.LOCAL }, SegmentedButtonDefaults.itemShape(0, 2)) { Text(stringResource(R.string.mode_local_short)) }
                SegmentedButton(editMode == UserMode.TOURIST, { editMode = UserMode.TOURIST }, SegmentedButtonDefaults.itemShape(1, 2)) { Text(stringResource(R.string.mode_tourist_short)) }
            }
            IdCaptureSection(form, editMode, onPrivacy = {})
            if (err != null) Text(stringResource(err!!), color = MaterialTheme.colorScheme.error)
            if (saved) Text(stringResource(R.string.id_submitted_ok), color = MaterialTheme.colorScheme.primary)
            Button(onClick = { err = form.validate(); if (err == null) vm.updateId(form.toDocument()) { saved = it } }, Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.submit_for_verification)) }
        }
    }
}
