package et.hotelbooking.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import et.hotelbooking.app.R
import et.hotelbooking.app.data.repo.IdDocument
import et.hotelbooking.app.domain.IdType
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.util.IdImageStore
import java.io.File
import java.time.LocalDate

/** Holds the identity-document form state (shared by registration and the "My ID" screen). */
class IdFormState(mode: UserMode) {
    var type by mutableStateOf(if (mode == UserMode.TOURIST) IdType.PASSPORT else IdType.NATIONAL_ID)
    var number by mutableStateOf("")
    var nationality by mutableStateOf(if (mode == UserMode.LOCAL) "Ethiopian" else "")
    var expiry by mutableStateOf("")   // YYYY-MM-DD
    var front by mutableStateOf("")
    var back by mutableStateOf("")
    var consent by mutableStateOf(false)
    val needsBack get() = type != IdType.PASSPORT
    fun expiryDay(): Long? = runCatching { LocalDate.parse(expiry.trim()).toEpochDay() }.getOrNull()
    /** Returns an error string resource, or null when valid. */
    fun validate(): Int? = when {
        number.trim().length < 5 -> R.string.err_id_number
        type == IdType.PASSPORT && nationality.isBlank() -> R.string.err_nationality
        type == IdType.PASSPORT && (expiryDay() ?: 0) <= LocalDate.now().toEpochDay() -> R.string.err_passport_expiry
        front.isBlank() -> R.string.err_id_photo
        needsBack && back.isBlank() -> R.string.err_id_back
        !consent -> R.string.err_consent
        else -> null
    }
    fun toDocument() = IdDocument(type, number.trim(), if (type == IdType.PASSPORT) expiryDay() else null, front, if (needsBack) back else "", nationality.trim())
}

@Composable
fun IdCaptureSection(state: IdFormState, mode: UserMode, onPrivacy: () -> Unit) {
    val types = if (mode == UserMode.TOURIST) listOf(IdType.PASSPORT) else listOf(IdType.NATIONAL_ID, IdType.KEBELE_ID)
    Text(stringResource(R.string.id_section_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(if (mode == UserMode.TOURIST) R.string.id_section_sub_tourist else R.string.id_section_sub_local),
        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Spacer(Modifier.height(8.dp))
    if (types.size > 1) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            types.forEachIndexed { i, t ->
                SegmentedButton(selected = state.type == t, onClick = { state.type = t }, shape = SegmentedButtonDefaults.itemShape(i, types.size)) { Text(stringResource(Labels.idType(t)), maxLines = 1) }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
    OutlinedTextField(state.number, { state.number = it.take(24) }, Modifier.fillMaxWidth(), singleLine = true,
        label = { Text(stringResource(if (state.type == IdType.PASSPORT) R.string.passport_number else R.string.id_number)) },
        supportingText = { if (state.type == IdType.NATIONAL_ID) Text(stringResource(R.string.fayda_hint)) },
        keyboardOptions = KeyboardOptions(keyboardType = if (state.type == IdType.NATIONAL_ID) KeyboardType.Number else KeyboardType.Text))
    if (state.type == IdType.PASSPORT) {
        OutlinedTextField(state.nationality, { state.nationality = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.nationality)) })
        OutlinedTextField(state.expiry, { state.expiry = it.take(10) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.passport_expiry)) },
            placeholder = { Text("2030-12-31") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        IdPhotoSlot(stringResource(if (state.type == IdType.PASSPORT) R.string.passport_photo_page else R.string.id_front), state.front, "front", Modifier.weight(1f)) { state.front = it }
        if (state.needsBack) IdPhotoSlot(stringResource(R.string.id_back), state.back, "back", Modifier.weight(1f)) { state.back = it }
    }
    Spacer(Modifier.height(8.dp))
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
            Checkbox(state.consent, { state.consent = it })
            Column(Modifier.weight(1f).padding(top = 10.dp)) {
                Text(stringResource(R.string.id_consent), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onPrivacy, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.privacy_policy)) }
            }
        }
    }
}

@Composable
fun IdPhotoSlot(label: String, path: String, side: String, modifier: Modifier = Modifier, onChange: (String) -> Unit) {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<File?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val f = pending
        if (ok && f != null && f.length() > 0) { IdImageStore.delete(path); onChange(f.absolutePath) } else f?.delete()
        pending = null
    }
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) IdImageStore.importFromUri(ctx, uri, side)?.let { IdImageStore.delete(path); onChange(it) }
    }
    val bmp = remember(path) {
        if (path.isBlank()) null else runCatching {
            val o = BitmapFactory.Options().apply { inSampleSize = 4 }
            BitmapFactory.decodeFile(path, o)?.asImageBitmap()
        }.getOrNull()
    }
    Column(modifier) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.58f).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp, label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Badge, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row {
            TextButton(onClick = { val (f, u) = IdImageStore.newCaptureTarget(ctx, side); pending = f; camera.launch(u) }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Icon(Icons.Outlined.PhotoCamera, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                Text(stringResource(if (bmp == null) R.string.camera else R.string.retake), style = MaterialTheme.typography.labelMedium)
            }
            TextButton(onClick = { gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, contentPadding = PaddingValues(horizontal = 6.dp)) {
                Icon(Icons.Outlined.PhotoLibrary, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.gallery), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
