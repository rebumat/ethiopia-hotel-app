package et.hotelbooking.app.ui.screens.manager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.BookingEntity
import et.hotelbooking.app.data.local.RoomEntity
import et.hotelbooking.app.data.local.UserEntity
import et.hotelbooking.app.data.repo.BookingRequest
import et.hotelbooking.app.data.repo.DemoPayment
import et.hotelbooking.app.data.repo.Invoice
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.auth.errorText
import et.hotelbooking.app.ui.vm.PmsViewModel
import et.hotelbooking.app.util.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckInScreen(pv: PmsViewModel, id: Long, onBack: () -> Unit, onDone: () -> Unit) {
    var b by remember { mutableStateOf<BookingEntity?>(null) }
    var guest by remember { mutableStateOf<UserEntity?>(null) }
    var rooms by remember { mutableStateOf<List<RoomEntity>>(emptyList()) }
    var roomId by remember { mutableStateOf<Long?>(null) }
    var idType by remember { mutableStateOf(IdType.NATIONAL_ID) }
    var idNo by remember { mutableStateOf("") }
    var nat by remember { mutableStateOf("") }
    var deposit by remember { mutableStateOf("") }
    var depMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var prefilled by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<Throwable?>(null) }
    var done by remember { mutableStateOf(false) }
    val types = rememberTypes(pv)
    val scope = rememberCoroutineScope()
    LaunchedEffect(id) {
        val bk = pv.booking(id); b = bk
        rooms = pv.freeRooms(id); roomId = bk?.roomId?.takeIf { r -> rooms.any { it.id == r } } ?: rooms.firstOrNull()?.id
        val g = pv.guestUser(bk?.userId); guest = g
        // Pre-fill from the ID on the guest's profile (uploaded at registration) or from the booking.
        val t = g?.idType ?: bk?.idType
        if (t != null) { idType = t; idNo = g?.idNumber?.ifBlank { null } ?: bk?.idNumber ?: ""; prefilled = idNo.isNotBlank() }
        else if (bk?.nationality?.isNotBlank() == true && bk.nationality != "Ethiopian") idType = IdType.PASSPORT
        nat = g?.nationality?.ifBlank { null } ?: bk?.nationality ?: ""
        deposit = if (bk?.paymentStatus == PaymentStatus.PAID) "0" else "2000"
    }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.check_in), onBack) }) { pad ->
        val bk = b ?: return@Scaffold LoadingBox(Modifier.padding(pad))
        if (done) { Column(Modifier.padding(pad).fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.CheckCircle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.checked_in_ok, bk.guestName, rooms.firstOrNull { it.id == roomId }?.number ?: ""), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp)); Button(onClick = onDone) { Text(stringResource(R.string.done)) }
        }; return@Scaffold }
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionCard(title = bk.guestName) {
                KeyValue(stringResource(R.string.booking_reference), bk.reference)
                KeyValue(stringResource(R.string.room), types[bk.roomTypeId]?.name ?: "")
                KeyValue(stringResource(R.string.stay), Dates.dayMonth(bk.checkIn) + " → " + Dates.dayMonth(bk.checkOut) + " (" + pluralStringResource(R.plurals.nights_count, bk.nights, bk.nights) + ")")
                KeyValue(stringResource(R.string.payment_status), stringResource(Labels.payStatus(bk.paymentStatus)) + if (bk.cardLast4.isNotBlank()) " · " + bk.cardBrand + " •••• " + bk.cardLast4 else "")
                KeyValue(stringResource(R.string.total), Money.etb(bk.totalEtb), bold = true)
            }
            Text(stringResource(R.string.verify_guest_id), style = MaterialTheme.typography.titleMedium)
            if (prefilled) Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Badge, null); Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.id_prefilled, guest?.verification?.let { stringResource(Labels.verification(it)) } ?: "—"), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IdType.entries.forEach { t -> FilterChip(idType == t, { idType = t }, label = { Text(stringResource(Labels.idType(t))) }) }
            }
            OutlinedTextField(idNo, { idNo = it; prefilled = false }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(if (idType == IdType.PASSPORT) R.string.passport_number else R.string.id_number)) },
                visualTransformation = if (prefilled) MaskTransformation else androidx.compose.ui.text.input.VisualTransformation.None)
            OutlinedTextField(nat, { nat = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.nationality)) })
            Text(stringResource(R.string.assign_room), style = MaterialTheme.typography.titleMedium)
            if (rooms.isEmpty()) Text(stringResource(R.string.no_free_rooms), color = MaterialTheme.colorScheme.error)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rooms.forEach { r -> FilterChip(roomId == r.id, { roomId = r.id }, label = { Text("#" + r.number) }, leadingIcon = { Icon(Icons.Outlined.MeetingRoom, null, Modifier.size(16.dp)) }) }
            }
            Text(stringResource(R.string.deposit), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(deposit, { deposit = it.filter { c -> c.isDigit() } }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.deposit_amount_etb)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(PaymentMethod.CASH, PaymentMethod.TELEBIRR, PaymentMethod.CBE_BIRR, PaymentMethod.VISA, PaymentMethod.MASTERCARD).forEach { m ->
                    FilterChip(depMethod == m, { depMethod = m }, label = { Text(stringResource(Labels.method(m))) }, leadingIcon = { MethodBadge(m, Modifier.height(16.dp)) })
                }
            }
            if (err != null) Text(errorText(err), color = MaterialTheme.colorScheme.error)
            Button(enabled = roomId != null, onClick = { scope.launch { pv.checkIn(bk.id, roomId!!, idType, idNo, nat, deposit.toDoubleOrNull() ?: 0.0, depMethod).onSuccess { done = true }.onFailure { err = it } } },
                modifier = Modifier.fillMaxWidth().height(52.dp)) { Icon(Icons.Outlined.Login, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.complete_check_in)) }
        }
    }
}

/** Shows only the last 4 characters of a pre-filled ID number. */
object MaskTransformation : androidx.compose.ui.text.input.VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString) = androidx.compose.ui.text.input.TransformedText(
        androidx.compose.ui.text.AnnotatedString(text.text.mapIndexed { i, c -> if (i < text.length - 4 && c != ' ') '•' else c }.joinToString("")),
        androidx.compose.ui.text.input.OffsetMapping.Identity)
}

@Composable
fun CheckOutScreen(pv: PmsViewModel, id: Long, onBack: () -> Unit) {
    val all by pv.bookings.collectAsStateWithLifecycle()
    val b = all?.firstOrNull { it.id == id }
    val folio by remember(id) { pv.folio(id) }.collectAsStateWithLifecycle(emptyList())
    val payments by remember(id) { pv.payments(id) }.collectAsStateWithLifecycle(emptyList())
    val roomNo = rememberRoomNumbers(pv)
    var showAdd by remember { mutableStateOf(false) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var feedback by remember { mutableStateOf("") }
    var finalInvoice by remember { mutableStateOf<Invoice?>(null) }
    var err by remember { mutableStateOf<Throwable?>(null) }
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings = LocalSettings.current
    Scaffold(topBar = { AppTopBar(stringResource(R.string.check_out), onBack) }) { pad ->
        val bk = b ?: return@Scaffold LoadingBox(Modifier.padding(pad))
        val inv = finalInvoice
        if (inv != null) {
            Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.CheckCircle, null, Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.checked_out_ok, bk.guestName), style = MaterialTheme.typography.titleLarge)
                SectionCard(title = stringResource(R.string.invoice_title)) {
                    KeyValue(stringResource(R.string.grand_total), Money.etb(inv.grandTotal), bold = true)
                    KeyValue("USD", Money.inCurrency(inv.grandTotal, Currency.USD))
                    KeyValue(stringResource(R.string.paid), Money.etb(inv.paid))
                }
                Text(stringResource(R.string.feedback_prompt_sent), style = MaterialTheme.typography.bodySmall)
                Button(onClick = { InvoicePdf.share(ctx, inv) }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.PictureAsPdf, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.share_invoice)) }
                OutlinedButton(onClick = onBack, Modifier.fillMaxWidth()) { Text(stringResource(R.string.done)) }
            }
            return@Scaffold
        }
        val extras = folio.sumOf { it.amountEtb }
        val paid = payments.filter { it.type != PaymentType.REFUND }.sumOf { it.amountEtb } - payments.filter { it.type == PaymentType.REFUND }.sumOf { it.amountEtb }
        val grand = bk.totalEtb + extras
        val balance = grand - paid
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(bk.guestName + " · #" + (bk.roomId?.let { roomNo[it] } ?: "—"), style = MaterialTheme.typography.titleLarge)
            Text(bk.reference + " · " + Dates.dayMonth(bk.checkIn) + " → " + Dates.dayMonth(bk.checkOut), color = MaterialTheme.colorScheme.onSurfaceVariant)
            SectionCard(title = stringResource(R.string.folio)) {
                KeyValue(stringResource(R.string.room_charges) + " (" + pluralStringResource(R.plurals.nights_count, bk.nights, bk.nights) + ")", Money.etb(bk.roomTotalEtb))
                KeyValue(stringResource(R.string.service_charge), Money.etb(bk.serviceChargeEtb))
                KeyValue(stringResource(R.string.vat), Money.etb(bk.vatEtb))
                folio.forEach { c ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KeyValue(stringResource(Labels.charge(c.category)) + " · " + c.description, Money.etb(c.amountEtb), Modifier.weight(1f))
                        IconButton(onClick = { pv.removeCharge(c) }, Modifier.size(32.dp)) { Icon(Icons.Outlined.Delete, stringResource(R.string.remove), Modifier.size(18.dp)) }
                    }
                }
                TextButton(onClick = { showAdd = true }) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.add_charge)) }
                HorizontalDivider()
                KeyValue(stringResource(R.string.grand_total), Money.etb(grand), bold = true)
                payments.forEach { KeyValue((if (it.type == PaymentType.REFUND) "− " else "") + stringResource(Labels.method(it.method)) + " (" + it.type.name.lowercase() + ")", Money.etb(it.amountEtb)) }
                KeyValue(if (balance >= 0) stringResource(R.string.balance_due) else stringResource(R.string.refund_due), Money.etb(kotlin.math.abs(balance)), bold = true)
                KeyValue(stringResource(R.string.approx_in, "USD"), Money.inCurrency(kotlin.math.abs(balance), Currency.USD))
            }
            if (balance > 0.009 || balance < -0.009) {
                Text(stringResource(R.string.settle_with), style = MaterialTheme.typography.titleMedium)
                methodsFor(settings.mode, includePayAtHotel = false, includeCash = true).forEach { m ->
                    Row(verticalAlignment = Alignment.CenterVertically) { RadioButton(method == m, { method = m }); MethodBadge(m, Modifier.height(18.dp)); Spacer(Modifier.width(8.dp)); Text(stringResource(Labels.method(m))) }
                }
                Text(stringResource(R.string.demo_no_charge), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            }
            OutlinedTextField(feedback, { feedback = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.guest_feedback_optional)) }, minLines = 2)
            if (err != null) Text(errorText(err), color = MaterialTheme.colorScheme.error)
            Button(onClick = { scope.launch { pv.checkOut(bk.id, method, feedback).onSuccess { finalInvoice = it }.onFailure { err = it } } }, Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.complete_check_out))
            }
        }
    }
    if (showAdd) AddChargeDialog(onDismiss = { showAdd = false }) { cat, desc, amt -> pv.addCharge(id, cat, desc, amt); showAdd = false }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddChargeDialog(onDismiss: () -> Unit, onAdd: (ChargeCategory, String, Double) -> Unit) {
    var cat by remember { mutableStateOf(ChargeCategory.MINIBAR) }
    var desc by remember { mutableStateOf("") }
    var amt by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.add_charge)) }, text = {
        Column {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChargeCategory.entries.filter { it != ChargeCategory.ROOM }.forEach { c -> FilterChip(cat == c, { cat = c }, label = { Text(stringResource(Labels.charge(c))) }) }
            }
            OutlinedTextField(desc, { desc = it }, label = { Text(stringResource(R.string.description)) }, singleLine = true)
            OutlinedTextField(amt, { amt = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text(stringResource(R.string.amount_etb)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
    }, confirmButton = { Button(onClick = { amt.toDoubleOrNull()?.let { onAdd(cat, desc, it) } }, enabled = (amt.toDoubleOrNull() ?: 0.0) > 0) { Text(stringResource(R.string.add)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WalkInScreen(pv: PmsViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val types by pv.types.collectAsStateWithLifecycle()
    val board by pv.roomBoard.collectAsStateWithLifecycle()
    val hotel by pv.hotel.collectAsStateWithLifecycle()
    val settings = LocalSettings.current
    var typeId by remember { mutableStateOf<Long?>(null) }
    var nights by remember { mutableIntStateOf(1) }
    var roomId by remember { mutableStateOf<Long?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+251 ") }
    var nat by remember { mutableStateOf("Ethiopian") }
    var idType by remember { mutableStateOf(IdType.NATIONAL_ID) }
    var idNo by remember { mutableStateOf("") }
    var adults by remember { mutableIntStateOf(1) }
    var resident by remember { mutableStateOf(true) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    var quote by remember { mutableStateOf<Quote?>(null) }
    var err by remember { mutableStateOf<Throwable?>(null) }
    var errRes by remember { mutableStateOf<Int?>(null) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(types) { if (typeId == null) typeId = types.firstOrNull()?.id }
    val today = Dates.today()
    LaunchedEffect(typeId, nights, resident) { typeId?.let { quote = pv.quote(it, today, today + nights, resident) } }
    val free = board.filter { it.first.roomTypeId == typeId && it.second == RoomStatus.AVAILABLE }.map { it.first }
    LaunchedEffect(free.map { it.id }) { if (roomId == null || free.none { it.id == roomId }) roomId = free.firstOrNull()?.id }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.qa_walk_in), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.choose_room), style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { types.filter { it.active }.forEach { t -> FilterChip(typeId == t.id, { typeId = t.id }, label = { Text(t.name) }) } }
            Stepper(stringResource(R.string.nights), nights, { nights = it }, 1, 30)
            Stepper(stringResource(R.string.adults), adults, { adults = it }, 1, 6)
            Text(stringResource(R.string.assign_room), style = MaterialTheme.typography.titleSmall)
            if (free.isEmpty()) Text(stringResource(R.string.no_free_rooms), color = MaterialTheme.colorScheme.error)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { free.forEach { r -> FilterChip(roomId == r.id, { roomId = r.id }, label = { Text("#" + r.number) }) } }
            HorizontalDivider()
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.full_name)) })
            OutlinedTextField(phone, { phone = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.phone)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(nat, { nat = it; resident = it.equals("Ethiopian", true) }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.nationality)) })
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { IdType.entries.forEach { t -> FilterChip(idType == t, { idType = t }, label = { Text(stringResource(Labels.idType(t))) }) } }
            OutlinedTextField(idNo, { idNo = it }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.id_number)) })
            if (hotel?.dualPricing == true) Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.resident_rate_toggle), Modifier.weight(1f)); Switch(resident, { resident = it }) }
            quote?.let { q ->
                SectionCard(title = stringResource(R.string.price_details)) {
                    KeyValue(stringResource(R.string.room_charges), Money.etb(q.roomTotal)); KeyValue(stringResource(R.string.service_charge) + " + " + stringResource(R.string.vat), Money.etb(q.serviceCharge + q.vat))
                    KeyValue(stringResource(R.string.total), Money.etb(q.total), bold = true)
                }
            }
            Text(stringResource(R.string.payment_method), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                methodsFor(settings.mode, includePayAtHotel = false, includeCash = true).forEach { m -> FilterChip(method == m, { method = m }, label = { Text(stringResource(Labels.method(m))) }, leadingIcon = { MethodBadge(m, Modifier.height(16.dp)) }) }
            }
            if (errRes != null) Text(stringResource(errRes!!), color = MaterialTheme.colorScheme.error)
            if (err != null) Text(errorText(err), color = MaterialTheme.colorScheme.error)
            Button(enabled = roomId != null && quote != null, modifier = Modifier.fillMaxWidth().height(52.dp), onClick = {
                errRes = when { name.isBlank() -> R.string.err_name; idNo.trim().length < 4 -> R.string.err_id_number; else -> null }
                if (errRes == null) scope.launch {
                    pv.walkIn(BookingRequest(null, pv.hotelId, typeId!!, today, today + nights, adults, 0, name, phone.replace(" ", ""), "", nat, idType, idNo, resident, ""), roomId!!,
                        DemoPayment(method, quote!!.total)).onSuccess { onDone() }.onFailure { err = it }
                }
            }) { Text(stringResource(R.string.create_and_check_in)) }
        }
    }
}

@Composable
fun LookupScreen(pv: PmsViewModel, onBack: () -> Unit, onFound: (Long) -> Unit) {
    var ref by remember { mutableStateOf("EHB-") }
    var notFound by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    fun find(r: String) = scope.launch { val b = pv.findByRef(QrCode.referenceFrom(r)); if (b != null) onFound(b.id) else notFound = true }
    val scanner = rememberLauncherForActivityResult(ScanContract()) { res -> res.contents?.let { ref = QrCode.referenceFrom(it); find(it) } }
    val prompt = stringResource(R.string.scan_prompt)
    Scaffold(topBar = { AppTopBar(stringResource(R.string.qa_checkin_lookup), onBack) }) { pad ->
        Column(Modifier.padding(pad).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scanner.launch(ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt(prompt).setBeepEnabled(false).setOrientationLocked(false)) }, Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Outlined.QrCodeScanner, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.scan_qr))
            }
            Text(stringResource(R.string.or_enter_ref), style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(ref, { ref = it.uppercase(); notFound = false }, Modifier.fillMaxWidth(), singleLine = true, label = { Text(stringResource(R.string.booking_reference)) })
            if (notFound) Text(stringResource(R.string.booking_not_found), color = MaterialTheme.colorScheme.error)
            Button(onClick = { find(ref) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.find_booking)) }
        }
    }
}
