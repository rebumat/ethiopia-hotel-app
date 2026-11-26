package et.hotelbooking.app.ui.screens.booking

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.auth.errorText
import et.hotelbooking.app.ui.screens.auth.validPhone
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.BookingDraft
import et.hotelbooking.app.ui.vm.BookingViewModel
import et.hotelbooking.app.util.*

@Composable
fun BookingFlowScreen(vm: AppViewModel, hotelId: Long, roomTypeId: Long?, onBack: () -> Unit, onDone: (Long) -> Unit) {
    val bvm: BookingViewModel = viewModel(key = "book_${hotelId}_$roomTypeId") { BookingViewModel(vm.c, hotelId, roomTypeId) }
    val d by bvm.draft.collectAsStateWithLifecycle()
    val result by bvm.result.collectAsStateWithLifecycle()
    val submitting by bvm.submitting.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val crit by vm.criteria.collectAsStateWithLifecycle()
    var step by rememberSaveable { mutableIntStateOf(0) }
    var err by remember { mutableStateOf<Int?>(null) }
    var processing by remember { mutableStateOf(false) }
    val pay = remember(settings.mode) { PaymentFormState(methodsFor(settings.mode).first()) }
    LaunchedEffect(user?.id, d.hotel?.id) { if (d.hotel != null) bvm.prefill(user, crit, settings.mode == UserMode.LOCAL) }
    LaunchedEffect(user?.phone) { if (settings.mode == UserMode.LOCAL && user?.phone?.startsWith("+251") == true) pay.phone = user!!.phone }
    val booked = result?.getOrNull()
    if (booked != null) { ConfirmationView(booked.reference, booked.status, d, booked.paymentStatus, onView = { onDone(booked.id) }); return }

    val titles = listOf(R.string.step_room, R.string.step_guest, R.string.step_review, R.string.step_payment)
    Scaffold(topBar = { AppTopBar(d.hotel?.name ?: "", onBack = { if (step > 0) step-- else onBack() }) }, bottomBar = {
        Surface(shadowElevation = 10.dp) {
            Row(Modifier.navigationBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.total), style = MaterialTheme.typography.labelSmall)
                    d.quote?.let { Price(it.total, Modifier.wrapContentWidth(Alignment.Start)) }
                }
                Button(enabled = !submitting && d.quote != null, modifier = Modifier.height(50.dp), onClick = {
                    err = when (step) {
                        0 -> if (d.checkOut <= d.checkIn) R.string.err_dates else if ((d.available ?: 1) <= 0) R.string.err_sold_out else null
                        1 -> when {
                            d.name.isBlank() -> R.string.err_name
                            !validPhone(d.phone, settings.mode) && !validPhone(d.phone, UserMode.TOURIST) -> R.string.err_phone
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(d.email).matches() -> R.string.err_email
                            settings.mode == UserMode.TOURIST && d.nationality.isBlank() -> R.string.err_nationality
                            else -> null
                        }
                        3 -> pay.validate()
                        else -> null
                    }
                    if (err == null) { if (step < 3) step++ else if (pay.method == PaymentMethod.PAY_AT_HOTEL || pay.method == PaymentMethod.BANK_TRANSFER) bvm.submit(user?.id, pay.toDemoPayment(d.quote!!.total)) else processing = true }
                }) { Text(stringResource(if (step < 3) R.string.next else R.string.confirm_booking)) }
            }
        }
    }) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                titles.forEachIndexed { i, t ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(26.dp).clip(CircleShape).background(if (i <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                            if (i < step) Icon(Icons.Outlined.Check, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimary)
                            else Text((i + 1).toString(), color = if (i <= step) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                        }
                        Text(stringResource(t), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
            }
            LinearProgressIndicator(progress = { (step + 1) / 4f }, Modifier.fillMaxWidth())
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (err != null) Text(stringResource(err!!), color = MaterialTheme.colorScheme.error)
                result?.exceptionOrNull()?.let { Text(errorText(it), color = MaterialTheme.colorScheme.error) }
                when (step) {
                    0 -> RoomStep(d, bvm)
                    1 -> GuestStep(d, bvm, settings.mode, user?.verification)
                    2 -> ReviewStep(d, settings)
                    else -> PaymentMethodPicker(pay, methodsFor(settings.mode))
                }
            }
        }
    }
    if (processing) DemoProcessingDialog(pay.method) { processing = false; bvm.submit(user?.id, pay.toDemoPayment(d.quote!!.total)); pay.clearSensitive() }
}

@Composable
private fun RoomStep(d: BookingDraft, bvm: BookingViewModel) {
    var showDates by remember { mutableStateOf(false) }
    Text(stringResource(R.string.choose_room), style = MaterialTheme.typography.titleMedium)
    d.types.forEach { t ->
        val sel = d.typeId == t.id
        OutlinedCard(onClick = { bvm.updateAndQuote { it.copy(typeId = t.id) } }, border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                RadioButton(sel, { bvm.updateAndQuote { it.copy(typeId = t.id) } })
                HotelImage(t.image, Modifier.size(56.dp).clip(MaterialTheme.shapes.small))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(t.name, style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.room_specs, t.capacity, t.bedType, t.sizeSqm), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Price(Pricing.baseRate(t, d.resident || d.hotel?.dualPricing != true), perNight = true)
            }
        }
    }
    DateRangeButton(d.checkIn, d.checkOut, { showDates = true })
    if (d.available != null) Text(if (d.available!! > 0) stringResource(R.string.rooms_available_n, d.available!!) else stringResource(R.string.sold_out), color = if (d.available!! > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
    Stepper(stringResource(R.string.adults), d.adults, { v -> bvm.update { it.copy(adults = v) } }, 1, 6)
    Stepper(stringResource(R.string.children), d.children, { v -> bvm.update { it.copy(children = v) } }, 0, 4)
    if (d.hotel?.dualPricing == true) Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.resident_rate_toggle), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.resident_rate_note), style = MaterialTheme.typography.bodySmall)
            }
            Switch(d.resident, { v -> bvm.updateAndQuote { it.copy(resident = v) } })
        }
    }
    if (showDates) DateRangeDialog(d.checkIn, d.checkOut, { showDates = false }, { a, b -> bvm.updateAndQuote { it.copy(checkIn = a, checkOut = b) }; showDates = false })
}

@Composable
private fun GuestStep(d: BookingDraft, bvm: BookingViewModel, mode: UserMode, ver: VerificationStatus?) {
    Text(stringResource(R.string.guest_details), style = MaterialTheme.typography.titleMedium)
    OutlinedTextField(d.name, { v -> bvm.update { it.copy(name = v) } }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.full_name)) }, singleLine = true)
    OutlinedTextField(d.phone, { v -> bvm.update { it.copy(phone = v) } }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.phone)) }, singleLine = true,
        supportingText = { Text(stringResource(if (mode == UserMode.LOCAL) R.string.phone_hint_local else R.string.phone_hint_intl)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
    OutlinedTextField(d.email, { v -> bvm.update { it.copy(email = v.trim()) } }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.email)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
    if (mode == UserMode.TOURIST || d.nationality.isNotBlank()) OutlinedTextField(d.nationality, { v -> bvm.update { it.copy(nationality = v) } }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.nationality)) }, singleLine = true)
    if (d.idType != null && d.idNumber.isNotBlank()) Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Badge, null, tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(Labels.idType(d.idType!!)) + " · " + IdImageStore.mask(d.idNumber), style = MaterialTheme.typography.titleSmall)
                Text(stringResource(R.string.id_on_file_note), style = MaterialTheme.typography.bodySmall)
            }
            if (ver != null) Pill(stringResource(Labels.verification(ver)), if (ver == VerificationStatus.VERIFIED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
        }
    }
    OutlinedTextField(d.requests, { v -> bvm.update { it.copy(requests = v) } }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.special_requests)) }, minLines = 2)
}

@Composable
private fun ReviewStep(d: BookingDraft, s: AppSettings) {
    val q = d.quote ?: return
    val t = d.types.firstOrNull { it.id == d.typeId }
    SectionCard(title = stringResource(R.string.your_stay)) {
        KeyValue(stringResource(R.string.hotel), d.hotel?.name ?: "")
        KeyValue(stringResource(R.string.room), t?.name ?: "")
        KeyValue(stringResource(R.string.check_in), Dates.display(d.checkIn, s.showEthiopianCalendar))
        KeyValue(stringResource(R.string.check_out), Dates.display(d.checkOut, s.showEthiopianCalendar))
        KeyValue(stringResource(R.string.guests), stringResource(R.string.adults_children, d.adults, d.children))
        if (d.hotel?.dualPricing == true) KeyValue(stringResource(R.string.rate_type), stringResource(if (d.resident) R.string.resident_rate else R.string.foreigner_rate))
    }
    SectionCard(title = stringResource(R.string.price_details)) {
        q.nightly.forEach { (day, rate) -> KeyValue(Dates.short(day), Money.etb(rate)) }
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        KeyValue(stringResource(R.string.room_charges), Money.etb(q.roomTotal))
        KeyValue(stringResource(R.string.service_charge), Money.etb(q.serviceCharge))
        KeyValue(stringResource(R.string.vat), Money.etb(q.vat))
        HorizontalDivider(Modifier.padding(vertical = 4.dp))
        KeyValue(stringResource(R.string.total), Money.etb(q.total), bold = true)
        if (s.currency != Currency.ETB) KeyValue(stringResource(R.string.approx_in, s.currency.code), Money.inCurrency(q.total, s.currency))
        else KeyValue(stringResource(R.string.approx_in, "USD"), Money.inCurrency(q.total, Currency.USD))
        Text(stringResource(R.string.sample_rate_note, Rates.ETB_PER_USD.toString(), Rates.AS_OF), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    SectionCard(title = stringResource(R.string.cancellation)) {
        Text(stringResource(R.string.cancellation_policy_text, d.hotel?.freeCancelDays ?: 2), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.booking_pending_explainer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun ConfirmationView(reference: String, status: BookingStatus, d: BookingDraft, ps: PaymentStatus, onView: () -> Unit) {
    val qr = remember(reference) { QrCode.bitmap(QrCode.payloadFor(reference), 480).asImageBitmap() }
    val s = LocalSettings.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Filled.CheckCircle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.booking_submitted), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(stringResource(R.string.booking_submitted_sub), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        TibebStrip(Modifier.clip(MaterialTheme.shapes.small))
        ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.booking_reference), style = MaterialTheme.typography.labelLarge)
                Text(reference, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatusChip(status); Pill(stringResource(Labels.payStatus(ps)), MaterialTheme.colorScheme.secondary) }
                Spacer(Modifier.height(12.dp))
                Image(qr, stringResource(R.string.checkin_qr), Modifier.size(180.dp).background(androidx.compose.ui.graphics.Color.White).padding(6.dp))
                Text(stringResource(R.string.qr_hint), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                KeyValue(stringResource(R.string.hotel), d.hotel?.name ?: "")
                KeyValue(stringResource(R.string.check_in), Dates.display(d.checkIn, s.showEthiopianCalendar))
                KeyValue(stringResource(R.string.check_out), Dates.display(d.checkOut, s.showEthiopianCalendar))
                d.quote?.let { KeyValue(stringResource(R.string.total), Money.etb(it.total) + "  (" + Money.inCurrency(it.total, if (s.currency == Currency.ETB) Currency.USD else s.currency) + ")", bold = true) }
            }
        }
        Button(onClick = onView, Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.view_booking)) }
    }
}
