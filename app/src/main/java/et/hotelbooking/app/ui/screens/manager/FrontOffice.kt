package et.hotelbooking.app.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.UserEntity
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.nav.R0
import et.hotelbooking.app.ui.theme.Brand
import et.hotelbooking.app.ui.vm.PmsViewModel
import et.hotelbooking.app.util.*
import kotlinx.coroutines.launch

data class QuickAction(val label: Int, val icon: ImageVector, val route: String, val perm: Permission)

@Composable
fun PmsDashboardScreen(pv: PmsViewModel, user: UserEntity, perms: Set<Permission>, go: (String) -> Unit) {
    val hotel by pv.hotel.collectAsStateWithLifecycle()
    val fd by pv.frontDesk.collectAsStateWithLifecycle()
    val board by pv.roomBoard.collectAsStateWithLifecycle()
    val types = rememberTypes(pv); val roomNo = rememberRoomNumbers(pv)
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    val occ = if (board.isEmpty()) 0 else board.count { it.second == RoomStatus.OCCUPIED } * 100 / board.size
    val actions = listOf(
        QuickAction(R.string.qa_checkin_lookup, Icons.Outlined.QrCodeScanner, R0.PMS_LOOKUP, Permission.CHECK_IN_OUT),
        QuickAction(R.string.qa_walk_in, Icons.Outlined.DirectionsWalk, R0.PMS_WALKIN, Permission.WALK_IN),
        QuickAction(R.string.nav_housekeeping, Icons.Outlined.CleaningServices, R0.PMS_HK, Permission.HOUSEKEEPING),
        QuickAction(R.string.qa_calendar, Icons.Outlined.CalendarViewMonth, R0.PMS_CAL, Permission.VIEW_CALENDAR),
        QuickAction(R.string.qa_guests, Icons.Outlined.Groups, R0.PMS_GUESTS, Permission.VIEW_GUESTS),
        QuickAction(R.string.qa_rates, Icons.Outlined.Sell, R0.PMS_RATES, Permission.MANAGE_RATES),
        QuickAction(R.string.qa_reports, Icons.Outlined.Insights, R0.PMS_REPORTS, Permission.VIEW_REPORTS),
        QuickAction(R.string.qa_staff, Icons.Outlined.Badge, R0.PMS_STAFF, Permission.MANAGE_STAFF),
        QuickAction(R.string.qa_hotel_profile, Icons.Outlined.Apartment, R0.PMS_HOTEL, Permission.EDIT_HOTEL_PROFILE),
    ).filter { it.perm in perms }
    Scaffold(snackbarHost = { SnackbarHost(snack) }, contentWindowInsets = WindowInsets(0)) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Brand.GreenDark, Brand.Green))).statusBarsPadding().padding(20.dp)) {
                    Column {
                        Text(stringResource(R.string.hello_name, user.fullName.substringBefore(' ')), color = Brand.GoldSoft, style = MaterialTheme.typography.titleMedium)
                        Text(hotel?.name ?: "", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(Labels.role(user.role)) + " · " + Dates.display(Dates.today(), true), color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                        if (hotel?.status == HotelStatus.PENDING) { Spacer(Modifier.height(8.dp)); Pill(stringResource(R.string.hotel_awaiting_approval), Brand.GoldSoft) }
                    }
                }
                TibebStrip(height = 6.dp)
            }
            item {
                val f = fd
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(stringResource(R.string.arrivals_today), (f?.arrivals?.size ?: 0).toString(), Icons.Outlined.Login, Modifier.weight(1f)) { go(R0.PMS_DESK) }
                    StatCard(stringResource(R.string.departures_today), (f?.departures?.size ?: 0).toString(), Icons.Outlined.Logout, Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary) { go(R0.PMS_DESK) }
                }
                Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(stringResource(R.string.in_house), (f?.inHouse?.size ?: 0).toString(), Icons.Outlined.Hotel, Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary)
                    StatCard(stringResource(R.string.occupancy_now), "$occ%", Icons.Outlined.PieChart, Modifier.weight(1f)) { go(R0.PMS_ROOMS) }
                }
            }
            item { SectionHeader(stringResource(R.string.quick_actions)) }
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    actions.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 5.dp)) {
                            row.forEach { a ->
                                Card(onClick = { go(a.route) }, modifier = Modifier.weight(1f).height(92.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                    Column(Modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                        Icon(a.icon, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(6.dp))
                                        Text(stringResource(a.label), style = MaterialTheme.typography.labelMedium, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }
            val pending = fd?.pending.orEmpty()
            item { SectionHeader(stringResource(R.string.pending_requests) + " (" + pending.size + ")", action = stringResource(R.string.see_all), onAction = { go(R0.PMS_RES) }) }
            if (pending.isEmpty()) item { Text(stringResource(R.string.no_pending), Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(pending.take(4), key = { it.id }) { b ->
                BookingRow(b, types, b.roomId?.let { roomNo[it] }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp), onClick = { go(R0.pmsBooking(b.id)) }, actions = {
                    if (Permission.MANAGE_RESERVATIONS in perms) {
                        TextButton(onClick = { pv.reject(b.id) }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.reject)) }
                        Button(onClick = { pv.confirm(b.id) }) { Text(stringResource(R.string.confirm)) }
                    }
                })
            }
        }
    }
}

@Composable
fun FrontDeskScreen(pv: PmsViewModel, perms: Set<Permission>, go: (String) -> Unit) {
    val fd by pv.frontDesk.collectAsStateWithLifecycle()
    val types = rememberTypes(pv); val roomNo = rememberRoomNumbers(pv)
    var tab by remember { mutableIntStateOf(0) }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    val f = fd
    Scaffold(snackbarHost = { SnackbarHost(snack) }, contentWindowInsets = WindowInsets(0), topBar = {
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.padding(16.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.nav_front_desk), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                if (Permission.CHECK_IN_OUT in perms) FilledTonalIconButton(onClick = { go(R0.PMS_LOOKUP) }) { Icon(Icons.Outlined.QrCodeScanner, stringResource(R.string.qa_checkin_lookup)) }
                if (Permission.WALK_IN in perms) FilledTonalButton(onClick = { go(R0.PMS_WALKIN) }) { Icon(Icons.Outlined.PersonAdd, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.qa_walk_in)) }
            }
            Text(Dates.display(Dates.today(), true), Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ScrollableTabRow(tab, edgePadding = 8.dp) {
                listOf(stringResource(R.string.arrivals) + " (" + (f?.arrivals?.size ?: 0) + ")", stringResource(R.string.in_house) + " (" + (f?.inHouse?.size ?: 0) + ")",
                    stringResource(R.string.departures) + " (" + (f?.departures?.size ?: 0) + ")", stringResource(R.string.overdue) + " (" + (f?.overdue?.size ?: 0) + ")").forEachIndexed { i, t ->
                    Tab(tab == i, { tab = i }, text = { Text(t) })
                }
            }
        }
    }) { pad ->
        if (f == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        val list = when (tab) { 0 -> f.arrivals; 1 -> f.inHouse; 2 -> f.departures; else -> f.overdue }
        if (list.isEmpty()) EmptyState(Icons.Outlined.EventAvailable, stringResource(R.string.nothing_here), stringResource(R.string.nothing_here_sub), Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { b ->
                BookingRow(b, types, b.roomId?.let { roomNo[it] }, onClick = { go(R0.pmsBooking(b.id)) }, actions = {
                    when {
                        b.status == BookingStatus.CONFIRMED && b.checkIn < Dates.today() && Permission.MANAGE_RESERVATIONS in perms -> TextButton(onClick = { pv.noShow(b.id) }) { Text(stringResource(R.string.mark_no_show)) }
                        b.status == BookingStatus.CONFIRMED && Permission.CHECK_IN_OUT in perms -> Button(onClick = { go(R0.checkin(b.id)) }) { Icon(Icons.Outlined.Login, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.check_in)) }
                        b.status == BookingStatus.CHECKED_IN && Permission.CHECK_IN_OUT in perms -> Button(onClick = { go(R0.checkout(b.id)) }) { Icon(Icons.Outlined.Logout, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.check_out)) }
                        else -> {}
                    }
                })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReservationsScreen(pv: PmsViewModel, perms: Set<Permission>, onOpen: (Long) -> Unit) {
    val all by pv.bookings.collectAsStateWithLifecycle()
    val types = rememberTypes(pv); val roomNo = rememberRoomNumbers(pv)
    var filter by remember { mutableStateOf<BookingStatus?>(null) }
    var q by remember { mutableStateOf("") }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, contentWindowInsets = WindowInsets(0), topBar = {
        Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.nav_reservations), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
            OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(stringResource(R.string.search_ref_name)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.extraLarge)
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(filter == null, { filter = null }, label = { Text(stringResource(R.string.all)) })
                BookingStatus.entries.forEach { s -> FilterChip(filter == s, { filter = s }, label = { Text(stringResource(Labels.status(s)) + " (" + all.orEmpty().count { it.status == s } + ")") }) }
            }
        }
    }) { pad ->
        val l = all ?: return@Scaffold LoadingBox(Modifier.padding(pad))
        val shown = l.filter { (filter == null || it.status == filter) && (q.isBlank() || it.reference.contains(q, true) || it.guestName.contains(q, true)) }
            .sortedWith(compareBy<et.hotelbooking.app.data.local.BookingEntity> { if (it.status.isActive) 0 else 1 }.thenByDescending { it.checkIn }).take(200)
        if (shown.isEmpty()) EmptyState(Icons.Outlined.SearchOff, stringResource(R.string.no_results), stringResource(R.string.no_results_sub), Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown, key = { it.id }) { b -> BookingRow(b, types, b.roomId?.let { roomNo[it] }, onClick = { onOpen(b.id) }) }
        }
    }
}

@Composable
fun PmsBookingScreen(pv: PmsViewModel, id: Long, perms: Set<Permission>, onBack: () -> Unit, go: (String) -> Unit) {
    val all by pv.bookings.collectAsStateWithLifecycle()
    val b = all?.firstOrNull { it.id == id }
    val types = rememberTypes(pv); val roomNo = rememberRoomNumbers(pv)
    val folio by remember(id) { pv.folio(id) }.collectAsStateWithLifecycle(emptyList())
    val payments by remember(id) { pv.payments(id) }.collectAsStateWithLifecycle(emptyList())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, topBar = { AppTopBar(b?.reference ?: "", onBack) }) { pad ->
        if (b == null) { LoadingBox(Modifier.padding(pad)); return@Scaffold }
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { StatusChip(b.status); Pill(stringResource(Labels.payStatus(b.paymentStatus)), MaterialTheme.colorScheme.secondary); Pill(stringResource(Labels.source(b.source)), MaterialTheme.colorScheme.outline) }
            SectionCard(title = stringResource(R.string.guest)) {
                KeyValue(stringResource(R.string.full_name), b.guestName)
                KeyValue(stringResource(R.string.phone), b.guestPhone)
                KeyValue(stringResource(R.string.email), b.guestEmail)
                if (b.nationality.isNotBlank()) KeyValue(stringResource(R.string.nationality), b.nationality)
                if (b.idType != null) KeyValue(stringResource(Labels.idType(b.idType)), IdImageStore.mask(b.idNumber))
                if (b.specialRequests.isNotBlank()) KeyValue(stringResource(R.string.special_requests), b.specialRequests)
            }
            SectionCard(title = stringResource(R.string.your_stay)) {
                KeyValue(stringResource(R.string.check_in), Dates.display(b.checkIn, true))
                KeyValue(stringResource(R.string.check_out), Dates.display(b.checkOut, true))
                KeyValue(stringResource(R.string.room), (types[b.roomTypeId]?.name ?: "") + (b.roomId?.let { " · #" + roomNo[it] } ?: ""))
                KeyValue(stringResource(R.string.guests), stringResource(R.string.adults_children, b.adults, b.children))
                KeyValue(stringResource(R.string.rate_type), stringResource(if (b.residentRate) R.string.resident_rate else R.string.foreigner_rate))
            }
            SectionCard(title = stringResource(R.string.folio)) {
                KeyValue(stringResource(R.string.room_charges), Money.etb(b.roomTotalEtb))
                KeyValue(stringResource(R.string.service_charge) + " + " + stringResource(R.string.vat), Money.etb(b.serviceChargeEtb + b.vatEtb))
                folio.forEach { KeyValue(stringResource(Labels.charge(it.category)) + " · " + it.description, Money.etb(it.amountEtb)) }
                KeyValue(stringResource(R.string.grand_total), Money.etb(b.totalEtb + folio.sumOf { it.amountEtb }), bold = true)
                payments.forEach { KeyValue((if (it.type == PaymentType.REFUND) "− " else "") + stringResource(Labels.method(it.method)) + " · " + it.type.name.lowercase(), Money.etb(it.amountEtb)) }
            }
            val canRes = Permission.MANAGE_RESERVATIONS in perms; val canDesk = Permission.CHECK_IN_OUT in perms
            if (b.status == BookingStatus.PENDING && canRes) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { pv.reject(b.id) }, Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.reject)) }
                Button(onClick = { pv.confirm(b.id) }, Modifier.weight(1f)) { Text(stringResource(R.string.confirm)) }
            }
            if (b.paymentStatus == PaymentStatus.PENDING_VERIFICATION && canRes) OutlinedButton(onClick = { pv.verifyPayment(b.id) }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Verified, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.verify_bank_payment)) }
            if (b.status == BookingStatus.CONFIRMED && canDesk && b.checkIn <= Dates.today()) Button(onClick = { go(R0.checkin(b.id)) }, Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.check_in)) }
            if (b.status == BookingStatus.CONFIRMED && canRes && b.checkIn < Dates.today()) OutlinedButton(onClick = { pv.noShow(b.id) }, Modifier.fillMaxWidth()) { Text(stringResource(R.string.mark_no_show)) }
            if (b.status == BookingStatus.CHECKED_IN && canDesk) Button(onClick = { go(R0.checkout(b.id)) }, Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.check_out)) }
            if (b.status == BookingStatus.CHECKED_IN || b.status == BookingStatus.CHECKED_OUT) OutlinedButton(onClick = { scope.launch { pv.invoice(b.id)?.let { InvoicePdf.share(ctx, it) } } }, Modifier.fillMaxWidth()) {
                Icon(Icons.Outlined.PictureAsPdf, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.share_invoice))
            }
            if (b.guestFeedback.isNotBlank()) SectionCard(title = stringResource(R.string.guest_feedback)) { Text(b.guestFeedback) }
        }
    }
}
