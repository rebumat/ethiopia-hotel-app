package et.hotelbooking.app.ui.screens.admin

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.nav.R0
import et.hotelbooking.app.ui.vm.AdminViewModel
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.IdImageStore
import et.hotelbooking.app.util.Money
import java.io.File

@Composable
private fun adminVm(vm: AppViewModel): AdminViewModel = viewModel(key = "admin") { AdminViewModel(vm.c) }

@Composable
fun AdminDashboardScreen(vm: AppViewModel, go: (String) -> Unit) {
    val av = adminVm(vm)
    val stats by av.stats.collectAsStateWithLifecycle()
    val users by av.users.collectAsStateWithLifecycle()
    val hotels by av.hotels.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { av.refresh() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).statusBarsPadding().padding(20.dp)) {
            Text(stringResource(R.string.admin_panel), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onPrimary)
            Text(stringResource(R.string.admin_panel_sub), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = .85f))
        }
        TibebStrip()
        val s = stats ?: return@Column LoadingBox(Modifier.height(200.dp))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(R.string.stat_users), s.users.toString(), Icons.Outlined.Group, Modifier.weight(1f), onClick = { go(R0.ADMIN_USERS) })
                StatCard(stringResource(R.string.stat_hotels_live), s.hotelsApproved.toString(), Icons.Outlined.Hotel, Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary, onClick = { go(R0.ADMIN_HOTELS) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(R.string.stat_hotels_pending), s.hotelsPending.toString(), Icons.Outlined.HourglassTop, Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary, onClick = { go(R0.ADMIN_HOTELS) })
                StatCard(stringResource(R.string.stat_bookings), s.bookings.toString() + " (" + s.activeBookings + ")", Icons.Outlined.EventNote, Modifier.weight(1f), onClick = { go(R0.ADMIN_BOOKINGS) })
            }
            StatCard(stringResource(R.string.stat_gross), Money.etb(s.grossEtb), Icons.Outlined.Payments, Modifier.fillMaxWidth(), tint = MaterialTheme.colorScheme.secondary)
            val pendingIds = users.orEmpty().filter { it.verification == VerificationStatus.PENDING }
            SectionCard(title = stringResource(R.string.needs_attention)) {
                hotels.orEmpty().filter { it.status == HotelStatus.PENDING }.forEach { h ->
                    ListItem(headlineContent = { Text(h.name) }, supportingContent = { Text(stringResource(R.string.hotel_awaiting_approval)) }, leadingContent = { Icon(Icons.Outlined.Hotel, null) },
                        trailingContent = { TextButton(onClick = { go(R0.ADMIN_HOTELS) }) { Text(stringResource(R.string.review)) } })
                }
                pendingIds.forEach { u ->
                    ListItem(headlineContent = { Text(u.fullName) }, supportingContent = { Text(stringResource(R.string.id_awaiting_review) + " · " + (u.idType?.let { stringResource(Labels.idType(it)) } ?: "")) },
                        leadingContent = { Icon(Icons.Outlined.Badge, null) }, trailingContent = { TextButton(onClick = { go(R0.adminUser(u.id)) }) { Text(stringResource(R.string.review)) } })
                }
                if (pendingIds.isEmpty() && hotels.orEmpty().none { it.status == HotelStatus.PENDING }) Text(stringResource(R.string.all_clear))
            }
        }
    }
}

@Composable
fun AdminHotelsScreen(vm: AppViewModel) {
    val av = adminVm(vm)
    val hotels by av.hotels.collectAsStateWithLifecycle()
    var filter by remember { mutableStateOf<HotelStatus?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(stringResource(R.string.manage_hotels), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(filter == null, { filter = null }, label = { Text(stringResource(R.string.all)) })
            HotelStatus.entries.forEach { s -> FilterChip(filter == s, { filter = s }, label = { Text(stringResource(Labels.hotelStatus(s)) + " (" + hotels.orEmpty().count { it.status == s } + ")") }) }
        }
        val l = hotels ?: return@Column LoadingBox()
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(l.filter { filter == null || it.status == filter }.sortedBy { it.status.ordinal }, key = { it.id }) { h ->
                OutlinedCard {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HotelImage(h.imageList.firstOrNull() ?: "hotel_building", Modifier.size(64.dp).clip(MaterialTheme.shapes.small))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(h.name, style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(CityInfo.nameRes(h.city)), style = MaterialTheme.typography.bodySmall)
                            Row(verticalAlignment = Alignment.CenterVertically) { Stars(h.stars, 12.dp); Spacer(Modifier.width(6.dp)); Pill(stringResource(Labels.hotelStatus(h.status)), statusColor(h.status)) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(end = 8.dp, bottom = 6.dp), horizontalArrangement = Arrangement.End) {
                        when (h.status) {
                            HotelStatus.PENDING -> { TextButton(onClick = { av.setHotelStatus(h.id, HotelStatus.REJECTED) }) { Text(stringResource(R.string.reject)) }; Button(onClick = { av.setHotelStatus(h.id, HotelStatus.APPROVED) }) { Text(stringResource(R.string.approve)) } }
                            HotelStatus.APPROVED -> OutlinedButton(onClick = { av.setHotelStatus(h.id, HotelStatus.SUSPENDED) }) { Text(stringResource(R.string.suspend)) }
                            HotelStatus.SUSPENDED, HotelStatus.REJECTED -> Button(onClick = { av.setHotelStatus(h.id, HotelStatus.APPROVED) }) { Text(stringResource(R.string.reinstate)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun statusColor(s: HotelStatus) = when (s) {
    HotelStatus.APPROVED -> MaterialTheme.colorScheme.primary; HotelStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.error
}

@Composable
fun verificationColor(v: VerificationStatus) = when (v) {
    VerificationStatus.VERIFIED -> MaterialTheme.colorScheme.primary; VerificationStatus.PENDING -> MaterialTheme.colorScheme.tertiary
    VerificationStatus.REJECTED -> MaterialTheme.colorScheme.error; VerificationStatus.NONE -> MaterialTheme.colorScheme.outline
}

@Composable
fun AdminUsersScreen(vm: AppViewModel, onOpen: (Long) -> Unit) {
    val av = adminVm(vm)
    val users by av.users.collectAsStateWithLifecycle()
    var q by remember { mutableStateOf("") }
    var pendingOnly by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(stringResource(R.string.manage_users), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth().padding(horizontal = 16.dp), singleLine = true, leadingIcon = { Icon(Icons.Outlined.Search, null) }, placeholder = { Text(stringResource(R.string.search_users)) })
        FilterChip(pendingOnly, { pendingOnly = !pendingOnly }, label = { Text(stringResource(R.string.id_awaiting_review)) }, modifier = Modifier.padding(horizontal = 16.dp))
        val l = users ?: return@Column LoadingBox()
        LazyColumn(contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 16.dp)) {
            items(l.filter { (q.isBlank() || it.fullName.contains(q, true) || it.email.contains(q, true)) && (!pendingOnly || it.verification == VerificationStatus.PENDING) }, key = { it.id }) { u ->
                ListItem(modifier = Modifier.clickable { onOpen(u.id) }, headlineContent = { Text(u.fullName) },
                    supportingContent = { Text(stringResource(Labels.role(u.role)) + " · " + u.email, maxLines = 1) },
                    leadingContent = { Icon(if (u.role == Role.CLIENT) Icons.Outlined.Person else Icons.Outlined.Badge, null) },
                    trailingContent = { Column(horizontalAlignment = Alignment.End) {
                        if (u.verification != VerificationStatus.NONE) Pill(stringResource(Labels.verification(u.verification)), verificationColor(u.verification))
                        if (!u.active) Text(stringResource(R.string.deactivated), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    } })
                HorizontalDivider()
            }
        }
    }
}

@Composable
fun AdminBookingsScreen(vm: AppViewModel) {
    val av = adminVm(vm)
    val bookings by av.bookings.collectAsStateWithLifecycle()
    val hotels by av.hotels.collectAsStateWithLifecycle()
    val names = remember(hotels) { hotels.orEmpty().associate { it.id to it.name } }
    var filter by remember { mutableStateOf<BookingStatus?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text(stringResource(R.string.all_reservations), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            FilterChip(filter == null, { filter = null }, label = { Text(stringResource(R.string.all)) })
            BookingStatus.entries.forEach { s -> FilterChip(filter == s, { filter = s }, label = { Text(stringResource(Labels.status(s))) }) }
        }
        val l = bookings ?: return@Column LoadingBox()
        val shown = l.filter { filter == null || it.status == filter }
        if (shown.isEmpty()) EmptyState(Icons.Outlined.EventBusy, stringResource(R.string.no_bookings), "")
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(shown.take(300), key = { it.id }) { b ->
                OutlinedCard { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(b.guestName + " · " + b.reference, style = MaterialTheme.typography.titleSmall)
                        Text(names[b.hotelId] ?: "", style = MaterialTheme.typography.bodySmall)
                        Text(Dates.dayMonth(b.checkIn) + " → " + Dates.dayMonth(b.checkOut) + " · " + Money.etb(b.totalEtb), style = MaterialTheme.typography.bodySmall)
                    }
                    StatusChip(b.status)
                } }
            }
        }
    }
}

@Composable
fun AdminUserScreen(vm: AppViewModel, id: Long, onBack: () -> Unit) {
    val av = adminVm(vm)
    val users by av.users.collectAsStateWithLifecycle()
    val u = users?.firstOrNull { it.id == id }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.user_profile), onBack) }) { pad ->
        if (u == null) return@Scaffold LoadingBox(Modifier.padding(pad))
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(u.fullName, style = MaterialTheme.typography.headlineSmall)
            SectionCard {
                KeyValue(stringResource(R.string.role), stringResource(Labels.role(u.role)))
                KeyValue(stringResource(R.string.email), u.email)
                KeyValue(stringResource(R.string.phone), u.phone)
                if (u.nationality.isNotBlank()) KeyValue(stringResource(R.string.nationality), u.nationality)
                KeyValue(stringResource(R.string.member_since), Dates.dateTime(u.createdAt))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.account_active), Modifier.weight(1f))
                    Switch(u.active, { av.setUserActive(u.id, it) }, enabled = u.role != Role.ADMIN)
                }
            }
            SectionCard(title = stringResource(R.string.identity_document)) {
                if (u.idType == null) Text(stringResource(R.string.no_id_on_file))
                else {
                    KeyValue(stringResource(R.string.id_type), stringResource(Labels.idType(u.idType)))
                    KeyValue(stringResource(R.string.id_number), IdImageStore.mask(u.idNumber))
                    u.idExpiry?.let { KeyValue(stringResource(R.string.expiry), Dates.dayMonth(it) + " " + Dates.of(it).year) }
                    KeyValue(stringResource(R.string.verification), stringResource(Labels.verification(u.verification)))
                    u.consentAt?.let { KeyValue(stringResource(R.string.consent_given), Dates.dateTime(it)) }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IdThumb(u.idFrontPath, stringResource(R.string.front_side), Modifier.weight(1f))
                        if (u.idType != IdType.PASSPORT) IdThumb(u.idBackPath, stringResource(R.string.back_side), Modifier.weight(1f))
                    }
                    Text(stringResource(R.string.admin_id_note), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { av.setVerification(u.id, VerificationStatus.REJECTED) }, enabled = u.verification != VerificationStatus.REJECTED,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.reject)) }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { av.setVerification(u.id, VerificationStatus.VERIFIED) }, enabled = u.verification != VerificationStatus.VERIFIED) { Icon(Icons.Outlined.Verified, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.verify)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun IdThumb(path: String, label: String, modifier: Modifier) {
    val bmp = remember(path) { if (path.isNotBlank() && File(path).exists()) runCatching { BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = 4 }) }.getOrNull() else null }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().aspectRatio(1.58f).clip(MaterialTheme.shapes.medium).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp.asImageBitmap(), label, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            else Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Outlined.Badge, null, Modifier.size(36.dp)); Text(stringResource(R.string.sample_id_placeholder), style = MaterialTheme.typography.labelSmall) }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}
