package et.hotelbooking.app.ui.screens.client

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.domain.Facility
import et.hotelbooking.app.domain.Pricing
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.theme.LocalExtraColors
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.CompareViewModel
import et.hotelbooking.app.util.Money
import kotlin.math.*

fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0; val dLat = Math.toRadians(lat2 - lat1); val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return 2 * r * asin(sqrt(a))
}

@Composable
fun CompareScreen(vm: AppViewModel, onBack: () -> Unit, onHotel: (Long) -> Unit) {
    val cvm: CompareViewModel = viewModel { CompareViewModel(vm.c) }
    val ids by vm.compare.collectAsStateWithLifecycle()
    val data by cvm.data.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val resident = settings.mode == UserMode.LOCAL
    LaunchedEffect(ids) { cvm.load(ids) }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.compare_hotels), onBack, actions = { if (ids.isNotEmpty()) TextButton(onClick = { vm.clearCompare() }) { Text(stringResource(R.string.clear)) } }) }) { pad ->
        if (ids.size < 2) { EmptyState(Icons.Outlined.CompareArrows, stringResource(R.string.compare_empty), stringResource(R.string.compare_empty_sub), Modifier.padding(pad)); return@Scaffold }
        val colW = 128.dp; val labelW = 116.dp
        val cheapest = data.minOfOrNull { (_, t) -> t.filter { it.active }.minOfOrNull { Pricing.baseRate(it, resident) } ?: Double.MAX_VALUE }
        val bestRating = data.maxOfOrNull { it.first.ratingAvg }
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState()).padding(12.dp)) {
            Row {
                Spacer(Modifier.width(labelW))
                data.forEach { (h, _) ->
                    Column(Modifier.width(colW).padding(4.dp)) {
                        HotelImage(h.imageList.first(), Modifier.fillMaxWidth().height(90.dp).clip(MaterialTheme.shapes.medium))
                        Text(h.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, modifier = Modifier.padding(top = 4.dp))
                        IconButton(onClick = { vm.toggleCompare(h.id) }, Modifier.size(28.dp)) { Icon(Icons.Outlined.Close, stringResource(R.string.remove), Modifier.size(16.dp)) }
                    }
                }
            }
            @Composable fun row(label: String, shaded: Boolean, cell: @Composable (Int) -> Unit) {
                Row(Modifier.background(if (shaded) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.background).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.width(labelW).padding(horizontal = 6.dp), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    data.indices.forEach { i -> Box(Modifier.width(colW).padding(horizontal = 4.dp)) { cell(i) } }
                }
            }
            row(stringResource(R.string.location), false) { i -> Text(stringResource(CityInfo.nameRes(data[i].first.city)), style = MaterialTheme.typography.bodyMedium) }
            row(stringResource(R.string.distance_centre), true) { i -> val h = data[i].first
                Text(stringResource(R.string.km_value, distanceKm(h.latitude, h.longitude, CityInfo.lat(h.city), CityInfo.lng(h.city))), style = MaterialTheme.typography.bodyMedium) }
            row(stringResource(R.string.star_rating), false) { i -> Stars(data[i].first.stars) }
            row(stringResource(R.string.guest_rating), true) { i -> val h = data[i].first
                Row(verticalAlignment = Alignment.CenterVertically) { RatingBadge(h.ratingAvg); if (h.ratingAvg == bestRating) Icon(Icons.Outlined.EmojiEvents, null, Modifier.padding(start = 4.dp).size(16.dp), tint = LocalExtraColors.current.gold) } }
            row(stringResource(R.string.from_price), false) { i ->
                val p = data[i].second.filter { it.active }.minOfOrNull { Pricing.baseRate(it, resident) }
                if (p != null) Column {
                    Text(Money.primary(p, settings.currency), fontWeight = FontWeight.Bold, color = if (p == cheapest) LocalExtraColors.current.success else MaterialTheme.colorScheme.onSurface)
                    if (p == cheapest) Text(stringResource(R.string.best_price), style = MaterialTheme.typography.labelSmall, color = LocalExtraColors.current.success)
                }
            }
            row(stringResource(R.string.room_types), true) { i -> Text(data[i].second.filter { it.active }.joinToString("\n") { it.name }, style = MaterialTheme.typography.bodySmall) }
            row(stringResource(R.string.free_cancellation), false) { i -> Text(pluralStringResource(R.plurals.days_before, (data[i].first.freeCancelDays), (data[i].first.freeCancelDays)), style = MaterialTheme.typography.bodySmall) }
            row(stringResource(R.string.check_in), true) { i -> Text(data[i].first.checkInTime + " / " + data[i].first.checkOutTime, style = MaterialTheme.typography.bodySmall) }
            row(stringResource(R.string.dual_pricing), false) { i -> Text(stringResource(if (data[i].first.dualPricing) R.string.yes else R.string.no), style = MaterialTheme.typography.bodySmall) }
            Facility.entries.filter { f -> data.any { f in it.first.facilityList } }.forEachIndexed { k, f ->
                row(stringResource(Labels.facility(f)), k % 2 == 1) { i ->
                    if (f in data[i].first.facilityList) Icon(Icons.Filled.CheckCircle, stringResource(R.string.yes), tint = LocalExtraColors.current.success)
                    else Text("—", color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.padding(top = 12.dp)) {
                Spacer(Modifier.width(labelW))
                data.forEach { (h, _) -> Button(onClick = { onHotel(h.id) }, Modifier.width(colW).padding(horizontal = 4.dp)) { Text(stringResource(R.string.view)) } }
            }
        }
    }
}
