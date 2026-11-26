package et.hotelbooking.app.ui.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.data.seed.Landmark
import et.hotelbooking.app.data.seed.Landmarks
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.auth.CityDropdown
import et.hotelbooking.app.ui.theme.Brand
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.HomeViewModel
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.EthiopianCalendar
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(vm: AppViewModel, onSearch: () -> Unit, onHotel: (Long) -> Unit, onLandmark: (String) -> Unit, onCity: (String) -> Unit, onCompare: () -> Unit, onLogin: () -> Unit) {
    val home: HomeViewModel = viewModel { HomeViewModel(vm.c) }
    val hotels by home.hotels.collectAsStateWithLifecycle()
    val prices by home.minPrices.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val compare by vm.compare.collectAsStateWithLifecycle()
    val savedIds by vm.savedIds.collectAsStateWithLifecycle()
    val recentlyViewed by vm.recentlyViewed.collectAsStateWithLifecycle()
    val tourist = settings.mode == UserMode.TOURIST
    fun priceOf(id: Long) = prices[id]?.let { if (tourist) it.second else it.first }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 96.dp)) {
            item { Hero(vm, user?.fullName?.substringBefore(' '), tourist, onSearch, onLogin, user == null) }
            item { Spacer(Modifier.height(4.dp)) }
            if (tourist) {
                item { LandmarksSection(onLandmark, large = true) }
                item { TravelEssentials() }
            } else {
                item { LandmarksSection(onLandmark, large = false) }
                item { LocalPerks() }
            }
            item { SectionHeader(stringResource(R.string.popular_cities), subtitle = stringResource(R.string.popular_cities_sub)) }
            item {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(CityInfo.popular) { key ->
                        val c = CityInfo.get(key)!!
                        val count = hotels?.count { it.city == key } ?: 0
                        Card(onClick = { onCity(key) }, shape = MaterialTheme.shapes.medium, modifier = Modifier.width(132.dp)) {
                            Box(Modifier.height(150.dp)) {
                                Image(painterResource(c.image), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f)))))
                                Column(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                                    Text(stringResource(c.name), color = Color.White, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(pluralStringResource(R.plurals.hotels_count, count, count), color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            item { SectionHeader(stringResource(R.string.featured_hotels), subtitle = stringResource(R.string.featured_hotels_sub), action = stringResource(R.string.see_all), onAction = onSearch) }
            item {
                val list = hotels
                if (list == null) LoadingBox()
                else LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(list.filter { it.featured }, key = { it.id }) { h ->
                        HotelCard(h, priceOf(h.id), { onHotel(h.id) }, width = 270.dp, compareSelected = h.id in compare, onToggleCompare = { vm.toggleCompare(h.id) },
                            saved = h.id in savedIds, onToggleSaved = { vm.toggleSaved(h.id) })
                    }
                }
            }
            if (recentlyViewed.isNotEmpty() && hotels != null) {
                val viewed = recentlyViewed.mapNotNull { id -> hotels!!.firstOrNull { it.id == id } }
                if (viewed.isNotEmpty()) {
                    item { SectionHeader(stringResource(R.string.recently_viewed), subtitle = stringResource(R.string.recently_viewed_sub)) }
                    item {
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(viewed, key = { it.id }) { h ->
                                HotelCard(h, priceOf(h.id), { onHotel(h.id) }, width = 270.dp, compareSelected = h.id in compare, onToggleCompare = { vm.toggleCompare(h.id) },
                                    saved = h.id in savedIds, onToggleSaved = { vm.toggleSaved(h.id) })
                            }
                        }
                    }
                }
            }
            item { SectionHeader(stringResource(if (tourist) R.string.top_rated else R.string.weekend_getaways), subtitle = stringResource(if (tourist) R.string.top_rated_sub else R.string.weekend_getaways_sub)) }
            val more = hotels.orEmpty().let { l -> if (tourist) l.sortedByDescending { it.ratingAvg }.take(4) else l.filter { it.city in setOf("bishoftu", "adama", "addis_ababa") && !it.featured }.take(4) }
            items(more, key = { "m" + it.id }) { h ->
                HotelCard(h, priceOf(h.id), { onHotel(h.id) }, Modifier.padding(horizontal = 16.dp, vertical = 6.dp), compareSelected = h.id in compare, onToggleCompare = { vm.toggleCompare(h.id) },
                    saved = h.id in savedIds, onToggleSaved = { vm.toggleSaved(h.id) })
            }
            item { DemoBanner(stringResource(R.string.demo_note_short), Modifier.padding(16.dp)) }
        }
        if (compare.isNotEmpty()) CompareBar(compare.size, onCompare, { vm.clearCompare() }, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun CompareBar(count: Int, onCompare: () -> Unit, onClear: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier.padding(16.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.inverseSurface, shape = RoundedCornerShape(50), shadowElevation = 6.dp) {
        Row(Modifier.padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CompareArrows, null, tint = MaterialTheme.colorScheme.inverseOnSurface)
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.compare_selected, count), color = MaterialTheme.colorScheme.inverseOnSurface, modifier = Modifier.weight(1f))
            TextButton(onClick = onClear) { Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.inverseOnSurface) }
            Button(onClick = onCompare, enabled = count >= 2) { Text(stringResource(R.string.compare)) }
        }
    }
}

@Composable
private fun Hero(vm: AppViewModel, firstName: String?, tourist: Boolean, onSearch: () -> Unit, onLogin: () -> Unit, guest: Boolean) {
    val crit by vm.criteria.collectAsStateWithLifecycle()
    var showDates by remember { mutableStateOf(false) }
    val lang = Locale.getDefault().language
    Box {
        Box(Modifier.fillMaxWidth().height(300.dp)) {
            Image(painterResource(if (tourist) R.drawable.lm_simien else R.drawable.lm_lalibela), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.55f), Color.Black.copy(alpha = 0.15f), Color.Black.copy(alpha = 0.65f)))))
            Column(Modifier.statusBarsPadding().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(Brand.Gold), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.Hotel, null, tint = Brand.GreenDark, modifier = Modifier.size(20.dp)) }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.app_name), color = Color.White, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (guest) TextButton(onClick = onLogin) { Text(stringResource(R.string.sign_in), color = Color.White) }
                }
                Spacer(Modifier.height(18.dp))
                Text(if (firstName != null) stringResource(R.string.hello_name, firstName) else stringResource(R.string.hello_guest), color = Brand.GoldSoft, style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.hero_title), color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text(stringResource(R.string.today_is) + " " + EthiopianCalendar.format(LocalDate.now(), lang), color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
            }
        }
        TibebStrip(Modifier.align(Alignment.BottomCenter).padding(bottom = 0.dp), height = 6.dp)
    }
    ElevatedCard(Modifier.padding(horizontal = 16.dp).layout { m, c -> val p = m.measure(c); val o = 36.dp.roundToPx(); layout(p.width, p.height - o) { p.place(0, -o) } }, shape = MaterialTheme.shapes.large, elevation = CardDefaults.elevatedCardElevation(6.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CityDropdown(crit.city, { vm.criteria.value = crit.copy(city = it) })
            DateRangeButton(crit.checkIn, crit.checkOut, { showDates = true })
            Stepper(stringResource(R.string.guests), crit.guests, { vm.criteria.value = crit.copy(guests = it) }, 1, 8)
            Button(onClick = { vm.recordRecentSearch(); onSearch() }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(Icons.Outlined.Search, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.search_hotels))
            }
        }
    }
    if (showDates) DateRangeDialog(crit.checkIn, crit.checkOut, { showDates = false }, { a, b -> vm.criteria.value = crit.copy(checkIn = a, checkOut = b); showDates = false })
}

@Composable
private fun LandmarksSection(onLandmark: (String) -> Unit, large: Boolean) {
    SectionHeader(stringResource(R.string.discover_ethiopia), subtitle = stringResource(R.string.discover_ethiopia_sub))
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(Landmarks.all, key = { it.key }) { l -> LandmarkCard(l, large) { onLandmark(l.key) } }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun LandmarkCard(l: Landmark, large: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = MaterialTheme.shapes.large, modifier = Modifier.width(if (large) 240.dp else 200.dp)) {
        Box(Modifier.height(if (large) 300.dp else 240.dp)) {
            Image(painterResource(l.image), stringResource(l.name), Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.85f)))))
            if (l.unesco) Surface(Modifier.padding(10.dp).align(Alignment.TopStart), color = Brand.Gold, shape = RoundedCornerShape(50)) {
                Text(stringResource(R.string.unesco), Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Brand.Ink, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(stringResource(l.name), color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Text(stringResource(l.desc), color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, null, Modifier.size(12.dp), tint = Brand.GoldSoft)
                    Text(stringResource(CityInfo.nameRes(l.nearCity)), color = Brand.GoldSoft, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun TravelEssentials() {
    SectionCard(Modifier.padding(16.dp), title = stringResource(R.string.travel_essentials)) {
        listOf(Icons.Outlined.Badge to R.string.tip_visa, Icons.Outlined.Payments to R.string.tip_currency, Icons.Outlined.Power to R.string.tip_power,
            Icons.Outlined.Schedule to R.string.tip_time, Icons.Outlined.CalendarMonth to R.string.tip_calendar, Icons.Outlined.HealthAndSafety to R.string.tip_health).forEach { (ic, t) -> TipRow(ic, stringResource(t)) }
        Text(stringResource(R.string.tip_disclaimer), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LocalPerks() {
    SectionCard(Modifier.padding(16.dp), title = stringResource(R.string.local_perks)) {
        TipRow(Icons.Outlined.PhoneAndroid, stringResource(R.string.perk_telebirr))
        TipRow(Icons.Outlined.Sell, stringResource(R.string.perk_resident))
        TipRow(Icons.Outlined.CalendarMonth, stringResource(R.string.perk_calendar))
    }
}

@Composable
private fun TipRow(icon: ImageVector, text: String) {
    Row(Modifier.padding(vertical = 5.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
