package et.hotelbooking.app.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.hotelbooking.app.R
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.DemoBanner
import et.hotelbooking.app.ui.components.TibebStrip
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.util.Languages

@Composable
fun OnboardingScreen(vm: AppViewModel, onDone: (login: Boolean) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val settings by vm.settings.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            Image(painterResource(listOf(R.drawable.lm_lalibela, R.drawable.lm_simien, R.drawable.lm_lake_tana)[step]), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.7f)))))
            Column(Modifier.align(Alignment.BottomStart).padding(20.dp).statusBarsPadding()) {
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(stringResource(R.string.tagline), style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f))
            }
        }
        TibebStrip()
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
            repeat(3) { i -> Box(Modifier.padding(4.dp).size(if (i == step) 22.dp else 8.dp, 8.dp).clip(CircleShape).background(if (i == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) }
        }
        AnimatedContent(step, Modifier.weight(1f), label = "onb") { s ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                when (s) {
                    0 -> LanguageStep(settings.mode)
                    1 -> ModeStep(settings.mode) { vm.setMode(it) }
                    else -> FinishStep()
                }
            }
        }
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (step > 0) TextButton(onClick = { step-- }) { Text(stringResource(R.string.back)) }
            Spacer(Modifier.weight(1f))
            if (step < 2) Button(onClick = { step++ }, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.next)) }
            else {
                OutlinedButton(onClick = { onDone(false) }, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.continue_guest)) }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { onDone(true) }, modifier = Modifier.height(48.dp)) { Text(stringResource(R.string.sign_in)) }
            }
        }
    }
}

@Composable
fun LanguageList(mode: UserMode) {
    val current = Languages.current()
    val groups = Languages.all.partition { it.local }
    val ordered = if (mode == UserMode.LOCAL) listOf(R.string.lang_local to groups.first, R.string.lang_world to groups.second)
        else listOf(R.string.lang_world to groups.second, R.string.lang_local to groups.first)
    ordered.forEach { (title, langs) ->
        Text(stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 12.dp, bottom = 6.dp))
        langs.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { l ->
                    val sel = Languages.isCurrent(l.tag) || (current.isEmpty() && l.tag == "en")
                    OutlinedCard(onClick = { Languages.apply(l.tag) }, modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                        border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        colors = CardDefaults.outlinedCardColors(containerColor = if (sel) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface)) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(l.nativeName, style = MaterialTheme.typography.titleMedium)
                                Text(l.englishName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (sel) Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LanguageStep(mode: UserMode) {
    Text(stringResource(R.string.choose_language), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.choose_language_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    LanguageList(mode)
}

@Composable
fun ModeCards(mode: UserMode, onSelect: (UserMode) -> Unit) {
    ModeCard(UserMode.LOCAL, mode == UserMode.LOCAL, Icons.Outlined.Home, R.string.mode_local, R.string.mode_local_desc,
        listOf(R.string.mode_local_b1, R.string.mode_local_b2, R.string.mode_local_b3)) { onSelect(UserMode.LOCAL) }
    Spacer(Modifier.height(12.dp))
    ModeCard(UserMode.TOURIST, mode == UserMode.TOURIST, Icons.Outlined.FlightLand, R.string.mode_tourist, R.string.mode_tourist_desc,
        listOf(R.string.mode_tourist_b1, R.string.mode_tourist_b2, R.string.mode_tourist_b3)) { onSelect(UserMode.TOURIST) }
}

@Composable
private fun ModeStep(mode: UserMode, onSelect: (UserMode) -> Unit) {
    Text(stringResource(R.string.choose_mode), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.choose_mode_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(16.dp))
    ModeCards(mode, onSelect)
}

@Composable
private fun ModeCard(m: UserMode, selected: Boolean, icon: ImageVector, title: Int, desc: Int, bullets: List<Int>, onClick: () -> Unit) {
    OutlinedCard(onClick = onClick, modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.outlinedCardColors(containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (m == UserMode.LOCAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RadioButton(selected = selected, onClick = onClick)
            }
            Spacer(Modifier.height(8.dp))
            bullets.forEach { b ->
                Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(b), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun FinishStep() {
    Text(stringResource(R.string.onb_ready_title), style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(8.dp))
    listOf(Icons.Outlined.TravelExplore to R.string.onb_f1, Icons.Outlined.CompareArrows to R.string.onb_f2, Icons.Outlined.QrCode2 to R.string.onb_f3, Icons.Outlined.Translate to R.string.onb_f4).forEach { (ic, t) ->
        Row(Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Icon(ic, null, tint = MaterialTheme.colorScheme.onSecondaryContainer) }
            Spacer(Modifier.width(12.dp))
            Text(stringResource(t), style = MaterialTheme.typography.bodyLarge)
        }
    }
    Spacer(Modifier.height(12.dp))
    DemoBanner(stringResource(R.string.demo_note_short))
}
