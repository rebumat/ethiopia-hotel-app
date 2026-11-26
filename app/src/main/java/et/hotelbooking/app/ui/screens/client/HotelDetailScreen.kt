package et.hotelbooking.app.ui.screens.client

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.HotelEntity
import et.hotelbooking.app.data.local.RoomTypeEntity
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.domain.Pricing
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.theme.LocalExtraColors
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.HotelDetailViewModel
import et.hotelbooking.app.util.Dates

fun openMap(ctx: android.content.Context, h: HotelEntity) {
    val geo = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${h.latitude},${h.longitude}?q=${h.latitude},${h.longitude}(${Uri.encode(h.name)})"))
    val osm = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${h.latitude}&mlon=${h.longitude}#map=16/${h.latitude}/${h.longitude}"))
    runCatching { ctx.startActivity(geo) }.onFailure { runCatching { ctx.startActivity(osm) } }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotelDetailScreen(vm: AppViewModel, hotelId: Long, onBack: () -> Unit, onBook: (Long, Long?) -> Unit, onCompare: () -> Unit) {
    val dvm: HotelDetailViewModel = viewModel(key = "hotel_$hotelId") { HotelDetailViewModel(vm.c, hotelId) }
    val hotel by dvm.hotel.collectAsStateWithLifecycle()
    val types by dvm.types.collectAsStateWithLifecycle()
    val reviews by dvm.reviews.collectAsStateWithLifecycle()
    val seasons by dvm.seasons.collectAsStateWithLifecycle()
    val avail by dvm.availability.collectAsStateWithLifecycle()
    val reviewable by dvm.reviewable.collectAsStateWithLifecycle()
    val crit by vm.criteria.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val compare by vm.compare.collectAsStateWithLifecycle()
    val savedIds by vm.savedIds.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    LaunchedEffect(hotelId) { vm.recordViewed(hotelId) }
    var showDates by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    LaunchedEffect(crit.checkIn, crit.checkOut) { dvm.refreshAvailability(crit.checkIn, crit.checkOut) }
    LaunchedEffect(user?.id) { dvm.refreshReviewable(user?.id) }
    val h = hotel ?: return LoadingBox()
    val resident = settings.mode == UserMode.LOCAL

    Box(Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                val pager = rememberPagerState { h.imageList.size }
                Box(Modifier.fillMaxWidth().height(300.dp)) {
                    HorizontalPager(pager) { p -> HotelImage(h.imageList[p], Modifier.fillMaxSize(), h.name) }
                    Box(Modifier.fillMaxWidth().height(110.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent))))
                    Row(Modifier.statusBarsPadding().padding(8.dp).fillMaxWidth()) {
                        FilledTonalIconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
                        Spacer(Modifier.weight(1f))
                        FilledTonalIconToggleButton(h.id in compare, { vm.toggleCompare(h.id) }) { Icon(if (h.id in compare) Icons.Filled.CompareArrows else Icons.Outlined.CompareArrows, stringResource(R.string.compare)) }
                        FilledTonalIconToggleButton(h.id in savedIds, { vm.toggleSaved(h.id) }) {
                            Icon(if (h.id in savedIds) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                stringResource(if (h.id in savedIds) R.string.saved_hotel else R.string.save_hotel))
                        }
                        FilledTonalIconButton(onClick = {
                            ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, h.name + " – " + h.address), null))
                        }) { Icon(Icons.Outlined.Share, stringResource(R.string.share)) }
                    }
                    Row(Modifier.align(Alignment.BottomCenter).padding(10.dp)) {
                        repeat(h.imageList.size) { i -> Box(Modifier.padding(3.dp).size(if (i == pager.currentPage) 18.dp else 7.dp, 7.dp).clip(CircleShape).background(Color.White.copy(alpha = if (i == pager.currentPage) 1f else 0.6f))) }
                    }
                }
                TibebStrip(height = 6.dp)
            }
            item {
                Column(Modifier.padding(20.dp)) {
                    Stars(h.stars, 16.dp)
                    Text(h.name, style = MaterialTheme.typography.headlineSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Place, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(h.address + " · " + stringResource(CityInfo.nameRes(h.city)), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    if (h.reviewCount > 0) RatingBadge(h.ratingAvg, h.reviewCount)
                    Spacer(Modifier.height(12.dp))
                    Text(h.description, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { openMap(ctx, h) }) { Icon(Icons.Outlined.Map, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.view_on_map)) }
                        OutlinedButton(onClick = { runCatching { ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + h.phone.replace(" ", "")))) } }) { Icon(Icons.Outlined.Call, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.call)) }
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.facilities))
                FlowRow(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    h.facilityList.forEach { f ->
                        Surface(color = MaterialTheme.colorScheme.surfaceContainer, shape = MaterialTheme.shapes.small) {
                            Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Labels.facilityIcon(f), null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp)); Text(stringResource(Labels.facility(f)), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.rooms_and_rates), subtitle = if (h.dualPricing) stringResource(R.string.dual_pricing_note) else null)
                Column(Modifier.padding(horizontal = 16.dp)) {
                    DateRangeButton(crit.checkIn, crit.checkOut, { showDates = true })
                    if (crit.checkIn == null) Text(stringResource(R.string.pick_dates_for_availability), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
            items(types.filter { it.active }, key = { it.id }) { t -> RoomTypeCard(h, t, resident, avail[t.id], crit.checkIn != null) { onBook(h.id, t.id) } }
            if (seasons.isNotEmpty()) item {
                SectionCard(Modifier.padding(16.dp), title = stringResource(R.string.seasonal_rates)) {
                    seasons.forEach { s -> KeyValue(s.name + " (" + "%02d/%02d–%02d/%02d".format(s.startDay, s.startMonth, s.endDay, s.endMonth) + ")", (if (s.adjustmentPct > 0) "+" else "") + s.adjustmentPct + "%") }
                }
            }
            item {
                SectionCard(Modifier.padding(16.dp), title = stringResource(R.string.policies)) {
                    KeyValue(stringResource(R.string.check_in), stringResource(R.string.from_time, h.checkInTime))
                    KeyValue(stringResource(R.string.check_out), stringResource(R.string.until_time, h.checkOutTime))
                    Spacer(Modifier.height(6.dp))
                    PolicyLine(Icons.Outlined.EventBusy, stringResource(R.string.cancellation_policy_text, h.freeCancelDays))
                    PolicyLine(Icons.Outlined.ChildCare, h.childrenPolicy)
                    PolicyLine(Icons.Outlined.Rule, h.extraPolicies)
                    PolicyLine(Icons.Outlined.Receipt, stringResource(R.string.tax_note))
                }
            }
            item {
                SectionHeader(stringResource(R.string.guest_reviews), subtitle = stringResource(R.string.verified_reviews_note))
                if (reviewable != null && user != null) Button(onClick = { showReview = true }, modifier = Modifier.padding(horizontal = 20.dp)) { Icon(Icons.Outlined.RateReview, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.write_review)) }
                if (reviews.isEmpty()) Text(stringResource(R.string.no_reviews), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(reviews, key = { "r" + it.id }) { r ->
                Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) { Text(r.authorName.take(1), fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.authorName, style = MaterialTheme.typography.titleSmall)
                            Row { repeat(5) { i -> Icon(Icons.Filled.Star, null, Modifier.size(14.dp), tint = if (i < r.rating) LocalExtraColors.current.gold else MaterialTheme.colorScheme.outlineVariant) } }
                        }
                        if (r.verifiedStay) Pill(stringResource(R.string.verified_stay), LocalExtraColors.current.success)
                    }
                    Text(r.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 6.dp))
                    Text(r.comment, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider(Modifier.padding(top = 10.dp))
                }
            }
        }
        Surface(Modifier.align(Alignment.BottomCenter).fillMaxWidth(), shadowElevation = 12.dp, color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.navigationBarsPadding().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                val from = types.filter { it.active }.minOfOrNull { Pricing.baseRate(it, resident) }
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.from), style = MaterialTheme.typography.labelSmall)
                    if (from != null) Price(from, perNight = true, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.Start))
                }
                Button(onClick = { onBook(h.id, null) }, modifier = Modifier.height(50.dp)) { Text(stringResource(R.string.book_now)) }
            }
        }
    }
    if (showDates) DateRangeDialog(crit.checkIn, crit.checkOut, { showDates = false }, { a, b -> vm.criteria.value = crit.copy(checkIn = a, checkOut = b); showDates = false })
    if (showReview && user != null) ReviewDialog(onDismiss = { showReview = false }) { rating, title, comment -> dvm.submitReview(user!!.id, rating, title, comment) { showReview = false } }
}

@Composable
private fun PolicyLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    if (text.isBlank()) return
    Row(Modifier.padding(vertical = 4.dp)) { Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text(text, style = MaterialTheme.typography.bodyMedium) }
}

@Composable
private fun RoomTypeCard(h: HotelEntity, t: RoomTypeEntity, resident: Boolean, available: Int?, datesChosen: Boolean, onSelect: () -> Unit) {
    OutlinedCard(Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth(), shape = MaterialTheme.shapes.large) {
        Row {
            HotelImage(t.image, Modifier.width(110.dp).height(150.dp))
            Column(Modifier.padding(12.dp).weight(1f)) {
                Text(t.name, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.room_specs, t.capacity, t.bedType, t.sizeSqm), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(t.description, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                if (h.dualPricing && t.foreignerPriceEtb != null) {
                    Text(stringResource(R.string.resident_rate) + ": " + et.hotelbooking.app.util.Money.etb(t.priceEtb) + "  ·  " + stringResource(R.string.foreigner_rate) + ": " + et.hotelbooking.app.util.Money.etb(t.foreignerPriceEtb),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        when {
                            !datesChosen -> {}
                            available == null -> {}
                            available <= 0 -> Text(stringResource(R.string.sold_out), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                            available <= 3 -> Text(pluralStringResource(R.plurals.rooms_left, available, available), color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.labelMedium)
                            else -> Text(stringResource(R.string.available), color = LocalExtraColors.current.success, style = MaterialTheme.typography.labelMedium)
                        }
                        Price(Pricing.baseRate(t, resident), perNight = true, modifier = Modifier.wrapContentWidth(Alignment.Start))
                    }
                    FilledTonalButton(onClick = onSelect, enabled = !(datesChosen && available != null && available <= 0)) { Text(stringResource(R.string.select)) }
                }
            }
        }
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String, String) -> Unit) {
    var rating by remember { mutableIntStateOf(5) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.write_review)) }, text = {
        Column {
            Row { (1..5).forEach { i -> IconButton(onClick = { rating = i }) { Icon(Icons.Filled.Star, null, tint = if (i <= rating) LocalExtraColors.current.gold else MaterialTheme.colorScheme.outlineVariant) } } }
            OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.review_title)) }, singleLine = true)
            OutlinedTextField(comment, { comment = it }, label = { Text(stringResource(R.string.review_comment)) }, minLines = 3)
            Text(stringResource(R.string.verified_reviews_note), style = MaterialTheme.typography.labelSmall)
        }
    }, confirmButton = { Button(onClick = { onSubmit(rating, title, comment) }, enabled = comment.length >= 5) { Text(stringResource(R.string.submit)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
