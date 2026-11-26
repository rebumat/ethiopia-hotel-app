package et.hotelbooking.app.ui.screens.client

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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.BookingEntity
import et.hotelbooking.app.data.local.NotificationEntity
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.auth.errorText
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.MyBookingsViewModel
import et.hotelbooking.app.ui.vm.NotificationsViewModel
import et.hotelbooking.app.util.*
import kotlinx.coroutines.launch

@Composable
fun MyBookingsScreen(vm: AppViewModel, onOpen: (Long) -> Unit, onLogin: () -> Unit, onExplore: () -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(stringResource(R.string.my_bookings), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        val u = user ?: return EmptyState(Icons.Outlined.Luggage, stringResource(R.string.sign_in_to_see_bookings), stringResource(R.string.sign_in_to_see_bookings_sub), action = stringResource(R.string.sign_in), onAction = onLogin)
        val mvm: MyBookingsViewModel = viewModel(key = "mb_${u.id}") { MyBookingsViewModel(vm.c, u.id) }
        val list by mvm.bookings.collectAsStateWithLifecycle()
        val hotels by mvm.hotels.collectAsStateWithLifecycle()
        var tab by remember { mutableIntStateOf(0) }
        val l = list ?: return LoadingBox()
        val upcoming = l.filter { it.status.isActive }.sortedBy { it.checkIn }
        val past = l.filter { !it.status.isActive }
        TabRow(tab) {
            Tab(tab == 0, { tab = 0 }, text = { Text(stringResource(R.string.upcoming) + " (" + upcoming.size + ")") })
            Tab(tab == 1, { tab = 1 }, text = { Text(stringResource(R.string.past) + " (" + past.size + ")") })
        }
        val shown = if (tab == 0) upcoming else past
        // Next upcoming trip: the soonest active booking whose check-in has not passed yet.
        val nextTrip = upcoming.filter { it.checkIn >= Dates.today() }.minByOrNull { it.checkIn }
        if (shown.isEmpty()) EmptyState(Icons.Outlined.EventAvailable, stringResource(R.string.no_bookings), stringResource(R.string.no_bookings_sub), action = stringResource(R.string.explore_hotels), onAction = onExplore)
        else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (tab == 0 && nextTrip != null) {
                item {
                    val days = (nextTrip.checkIn - Dates.today()).toInt()
                    val nextHotel = hotels[nextTrip.hotelId]
                    ElevatedCard(Modifier.fillMaxWidth(), colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.next_trip), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    when (days) {
                                        0 -> stringResource(R.string.checkin_today)
                                        1 -> stringResource(R.string.checkin_tomorrow)
                                        else -> pluralStringResource(R.plurals.days_to_go, days, days)
                                    }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(nextHotel?.name ?: "", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(Dates.display(nextTrip.checkIn, settings.showEthiopianCalendar) + " – " + Dates.display(nextTrip.checkOut, settings.showEthiopianCalendar),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Button(onClick = { onOpen(nextTrip.id) }) { Text(stringResource(R.string.view_booking)) }
                        }
                    }
                }
            }
            items(shown, key = { it.id }) { b ->
                val h = hotels[b.hotelId]
                ElevatedCard(onClick = { onOpen(b.id) }, Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HotelImage(h?.imageList?.firstOrNull() ?: "hotel_building", Modifier.size(72.dp).clip(MaterialTheme.shapes.medium))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(h?.name ?: "", style = MaterialTheme.typography.titleSmall)
                            Text(Dates.dayMonth(b.checkIn) + " – " + Dates.dayMonth(b.checkOut) + " · " + pluralStringResource(R.plurals.nights_count, b.nights, b.nights), style = MaterialTheme.typography.bodySmall)
                            Text(b.reference, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                StatusChip(b.status)
                                if (b.status == BookingStatus.CHECKED_OUT && !b.reviewed) Pill(stringResource(R.string.review_now), MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(status: BookingStatus) {
    val steps = listOf(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT)
    val terminal = status in setOf(BookingStatus.CANCELLED, BookingStatus.REJECTED, BookingStatus.NO_SHOW)
    val idx = steps.indexOf(status)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        steps.forEachIndexed { i, s ->
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                val done = !terminal && i <= idx
                Box(Modifier.size(22.dp).clip(CircleShape).background(if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    if (done) Icon(Icons.Outlined.Check, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text(stringResource(Labels.status(s)), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, maxLines = 2)
            }
        }
    }
    if (terminal) Text(stringResource(Labels.status(status)), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
}

@Composable
fun BookingDetailScreen(vm: AppViewModel, bookingId: Long, onBack: () -> Unit, onHotel: (Long) -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val u = user ?: return
    val mvm: MyBookingsViewModel = viewModel(key = "mb_${u.id}") { MyBookingsViewModel(vm.c, u.id) }
    val b by remember(bookingId) { vm.c.bookings.booking(bookingId) }.collectAsStateWithLifecycle(null)
    val hotels by mvm.hotels.collectAsStateWithLifecycle()
    val types by mvm.types.collectAsStateWithLifecycle()
    val folio by remember(bookingId) { mvm.folio(bookingId) }.collectAsStateWithLifecycle(emptyList())
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var cancelPreview by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showPay by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    val bk = b ?: return LoadingBox()
    val h = hotels[bk.hotelId]
    val t = types[bk.roomTypeId]
    val cancelledMsg = stringResource(R.string.booking_cancelled_refund)
    Scaffold(topBar = { AppTopBar(stringResource(R.string.booking_details), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (msg != null) Text(msg!!, color = MaterialTheme.colorScheme.primary)
            ElevatedCard {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(h?.name ?: "", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    Text(bk.reference, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    StatusTimeline(bk.status)
                    if (bk.status == BookingStatus.CONFIRMED || bk.status == BookingStatus.PENDING || bk.status == BookingStatus.CHECKED_IN) {
                        val qr = remember(bk.reference) { QrCode.bitmap(QrCode.payloadFor(bk.reference), 480).asImageBitmap() }
                        Spacer(Modifier.height(12.dp))
                        Image(qr, stringResource(R.string.checkin_qr), Modifier.size(200.dp).background(Color.White).padding(6.dp))
                        Text(stringResource(if (bk.status == BookingStatus.PENDING) R.string.qr_pending_hint else R.string.qr_hint), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                }
            }
            SectionCard(title = stringResource(R.string.your_stay)) {
                KeyValue(stringResource(R.string.check_in), Dates.display(bk.checkIn, settings.showEthiopianCalendar) + "\n" + stringResource(R.string.from_time, h?.checkInTime ?: ""))
                KeyValue(stringResource(R.string.check_out), Dates.display(bk.checkOut, settings.showEthiopianCalendar) + "\n" + stringResource(R.string.until_time, h?.checkOutTime ?: ""))
                KeyValue(stringResource(R.string.room), (t?.name ?: "") + if (bk.status == BookingStatus.CHECKED_IN && bk.roomId != null) "" else "")
                KeyValue(stringResource(R.string.guests), stringResource(R.string.adults_children, bk.adults, bk.children))
                KeyValue(stringResource(R.string.guest), bk.guestName)
                if (bk.idNumber.isNotBlank() && bk.idType != null) KeyValue(stringResource(Labels.idType(bk.idType)), IdImageStore.mask(bk.idNumber))
                OutlinedButton(onClick = { h?.let { onHotel(it.id) } }) { Text(stringResource(R.string.view_hotel)) }
            }
            SectionCard(title = stringResource(R.string.payment)) {
                KeyValue(stringResource(R.string.room_charges), Money.etb(bk.roomTotalEtb))
                KeyValue(stringResource(R.string.service_charge), Money.etb(bk.serviceChargeEtb))
                KeyValue(stringResource(R.string.vat), Money.etb(bk.vatEtb))
                folio.forEach { KeyValue(stringResource(Labels.charge(it.category)) + " · " + it.description, Money.etb(it.amountEtb)) }
                KeyValue(stringResource(R.string.total), Money.etb(bk.totalEtb + folio.sumOf { it.amountEtb }), bold = true)
                KeyValue(stringResource(R.string.approx_in, if (settings.currency == Currency.ETB) "USD" else settings.currency.code), Money.inCurrency(bk.totalEtb, if (settings.currency == Currency.ETB) Currency.USD else settings.currency))
                KeyValue(stringResource(R.string.payment_method), stringResource(Labels.method(bk.paymentMethod)) + if (bk.cardLast4.isNotBlank()) " •••• " + bk.cardLast4 else "")
                KeyValue(stringResource(R.string.payment_status), stringResource(Labels.payStatus(bk.paymentStatus)))
                if (bk.refundEtb > 0) KeyValue(stringResource(R.string.refund), Money.etb(bk.refundEtb))
                if ((bk.paymentStatus == PaymentStatus.UNPAID) && (bk.status == BookingStatus.PENDING || bk.status == BookingStatus.CONFIRMED))
                    Button(onClick = { showPay = true }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.Payment, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.pay_now_demo)) }
            }
            if (bk.status == BookingStatus.CHECKED_OUT) {
                OutlinedButton(onClick = { scope.launch { mvm.invoice(bk.id)?.let { InvoicePdf.share(ctx, it) } } }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.PictureAsPdf, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.download_invoice)) }
                if (!bk.reviewed) Button(onClick = { h?.let { onHotel(it.id) } }, Modifier.fillMaxWidth()) { Icon(Icons.Outlined.RateReview, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.write_review)) }
            }
            if (bk.status == BookingStatus.PENDING || bk.status == BookingStatus.CONFIRMED) {
                SectionCard(title = stringResource(R.string.cancellation)) {
                    Text(stringResource(R.string.cancellation_policy_text, h?.freeCancelDays ?: 2), style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { scope.launch { cancelPreview = mvm.preview(bk.id) } }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.cancel_booking)) }
                }
            }
        }
    }
    cancelPreview?.let { (fee, refund) ->
        AlertDialog(onDismissRequest = { cancelPreview = null }, title = { Text(stringResource(R.string.cancel_booking_q)) },
            text = { Column {
                KeyValue(stringResource(R.string.cancellation_fee), Money.etb(fee))
                KeyValue(stringResource(R.string.refund), Money.etb(refund))
                Text(stringResource(R.string.refund_note), style = MaterialTheme.typography.bodySmall)
            } },
            confirmButton = { Button(onClick = { mvm.cancel(bk.id) { r -> msg = if (r.isSuccess) cancelledMsg else null }; cancelPreview = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.cancel_booking)) } },
            dismissButton = { TextButton(onClick = { cancelPreview = null }) { Text(stringResource(R.string.keep_booking)) } })
    }
    if (showPay) PayLaterDialog(settings.mode, bk, onDismiss = { showPay = false }) { p -> mvm.pay(bk.id, p) { showPay = false } }
}

@Composable
private fun PayLaterDialog(mode: UserMode, b: BookingEntity, onDismiss: () -> Unit, onPay: (et.hotelbooking.app.data.repo.DemoPayment) -> Unit) {
    val st = remember { PaymentFormState(methodsFor(mode, includePayAtHotel = false).first()) }
    var err by remember { mutableStateOf<Int?>(null) }
    var processing by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.pay_now_demo) + " · " + Money.etb(b.totalEtb)) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            PaymentMethodPicker(st, methodsFor(mode, includePayAtHotel = false), b.reference)
            if (err != null) Text(stringResource(err!!), color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { Button(onClick = { err = st.validate(); if (err == null) { if (st.method == PaymentMethod.BANK_TRANSFER) onPay(st.toDemoPayment(b.totalEtb)!!) else processing = true } }) { Text(stringResource(R.string.pay)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
    if (processing) DemoProcessingDialog(st.method) { processing = false; onPay(st.toDemoPayment(b.totalEtb)!!); st.clearSensitive() }
}

@Composable
fun notificationText(n: NotificationEntity): Pair<String, String> = when (n.type) {
    NotificationType.BOOKING_SUBMITTED -> stringResource(R.string.n_submitted_t) to stringResource(R.string.n_submitted_b, n.arg1, n.arg2)
    NotificationType.BOOKING_CONFIRMED -> stringResource(R.string.n_confirmed_t) to stringResource(R.string.n_confirmed_b, n.arg1, n.arg2)
    NotificationType.BOOKING_REJECTED -> stringResource(R.string.n_rejected_t) to stringResource(R.string.n_rejected_b, n.arg1, n.arg2)
    NotificationType.BOOKING_CANCELLED -> stringResource(R.string.n_cancelled_t) to stringResource(R.string.n_cancelled_b, n.arg1, n.arg2)
    NotificationType.CHECKED_IN -> stringResource(R.string.n_checked_in_t) to stringResource(R.string.n_checked_in_b, n.arg1, n.arg2)
    NotificationType.CHECKED_OUT -> stringResource(R.string.n_checked_out_t) to stringResource(R.string.n_checked_out_b, n.arg1, n.arg2)
    NotificationType.REVIEW_REMINDER -> stringResource(R.string.n_review_t) to stringResource(R.string.n_review_b, n.arg2)
    NotificationType.NEW_RESERVATION -> stringResource(R.string.n_new_res_t) to stringResource(R.string.n_new_res_b, n.arg1, n.arg2)
    NotificationType.PAYMENT_RECEIVED -> stringResource(R.string.n_payment_t) to stringResource(R.string.n_payment_b, n.arg1)
    NotificationType.HOTEL_APPROVED -> stringResource(R.string.n_hotel_approved_t) to stringResource(R.string.n_hotel_approved_b, n.arg1)
    NotificationType.HOTEL_SUSPENDED -> stringResource(R.string.n_hotel_suspended_t) to stringResource(R.string.n_hotel_suspended_b, n.arg1)
    NotificationType.WELCOME -> stringResource(R.string.n_welcome_t) to stringResource(R.string.n_welcome_b, n.arg1)
    NotificationType.NO_SHOW -> stringResource(R.string.n_no_show_t) to stringResource(R.string.n_no_show_b, n.arg1, n.arg2)
    NotificationType.HOTEL_REGISTERED -> stringResource(R.string.n_hotel_registered_t) to stringResource(R.string.n_hotel_registered_b, n.arg1, n.arg2)
    NotificationType.ID_VERIFIED -> stringResource(R.string.n_id_verified_t) to stringResource(R.string.n_id_verified_b)
    NotificationType.ID_REJECTED -> stringResource(R.string.n_id_rejected_t) to stringResource(R.string.n_id_rejected_b)
    NotificationType.ID_SUBMITTED -> stringResource(R.string.n_id_submitted_t) to stringResource(R.string.n_id_submitted_b, n.arg1)
}

@Composable
fun NotificationsScreen(vm: AppViewModel, onLogin: () -> Unit, onBooking: (Long) -> Unit) {
    val user by vm.user.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        val u = user
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.notifications), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
        }
        if (u == null) { EmptyState(Icons.Outlined.NotificationsNone, stringResource(R.string.sign_in_for_alerts), stringResource(R.string.sign_in_for_alerts_sub), action = stringResource(R.string.sign_in), onAction = onLogin); return }
        val nvm: NotificationsViewModel = viewModel(key = "n_${u.id}") { NotificationsViewModel(vm.c, u.id) }
        val items by nvm.items.collectAsStateWithLifecycle()
        DisposableEffect(Unit) { onDispose { nvm.markAll() } }
        val l = items ?: return LoadingBox()
        if (l.isEmpty()) EmptyState(Icons.Outlined.NotificationsNone, stringResource(R.string.no_notifications), stringResource(R.string.no_notifications_sub))
        else LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            items(l, key = { it.id }) { n ->
                val (title, body) = notificationText(n)
                ListItem(headlineContent = { Text(title, fontWeight = if (!n.read) FontWeight.Bold else FontWeight.Normal) },
                    supportingContent = { Column { Text(body); Text(Dates.dateTime(n.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
                    leadingContent = { Box(Modifier.size(40.dp).clip(CircleShape).background(if (!n.read) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { Icon(iconFor(n.type), null) } },
                    modifier = if (n.bookingId != null && u.role == Role.CLIENT) Modifier.clickable { onBooking(n.bookingId) } else Modifier)
                HorizontalDivider()
            }
        }
    }
}


private fun iconFor(t: NotificationType) = when (t) {
    NotificationType.BOOKING_CONFIRMED, NotificationType.HOTEL_APPROVED, NotificationType.ID_VERIFIED -> Icons.Outlined.CheckCircle
    NotificationType.BOOKING_REJECTED, NotificationType.BOOKING_CANCELLED, NotificationType.HOTEL_SUSPENDED, NotificationType.NO_SHOW, NotificationType.ID_REJECTED -> Icons.Outlined.Cancel
    NotificationType.CHECKED_IN, NotificationType.CHECKED_OUT -> Icons.Outlined.MeetingRoom
    NotificationType.REVIEW_REMINDER -> Icons.Outlined.RateReview
    NotificationType.PAYMENT_RECEIVED -> Icons.Outlined.Payments
    NotificationType.WELCOME -> Icons.Outlined.WavingHand
    else -> Icons.Outlined.Notifications
}
