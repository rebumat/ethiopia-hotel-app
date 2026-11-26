package et.hotelbooking.app.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.data.repo.SearchCriteria
import et.hotelbooking.app.data.repo.SortOrder
import et.hotelbooking.app.data.seed.CityInfo
import et.hotelbooking.app.domain.Facility
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.screens.auth.CityDropdown
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.SearchViewModel
import et.hotelbooking.app.ui.vm.UiState
import et.hotelbooking.app.util.Money

const val PRICE_MAX = 60000f

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(vm: AppViewModel, onHotel: (Long) -> Unit, onCompare: () -> Unit) {
    val svm: SearchViewModel = viewModel { SearchViewModel(vm.c) }
    val crit by vm.criteria.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val state by svm.state.collectAsStateWithLifecycle()
    val compare by vm.compare.collectAsStateWithLifecycle()
    val savedIds by vm.savedIds.collectAsStateWithLifecycle()
    val recentSearches by vm.recentSearches.collectAsStateWithLifecycle()
    var showFilters by remember { mutableStateOf(false) }
    var showDates by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    val fullMsg = stringResource(R.string.compare_full)
    var toast by remember { mutableStateOf(false) }
    LaunchedEffect(toast) { if (toast) { snack.showSnackbar(fullMsg); toast = false } }
    LaunchedEffect(crit, settings.mode) { svm.run(crit.copy(resident = settings.mode == UserMode.LOCAL)) }

    Scaffold(snackbarHost = { SnackbarHost(snack) }, topBar = {
        Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(stringResource(R.string.explore_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(crit.query, { vm.criteria.value = crit.copy(query = it) }, Modifier.weight(1f), singleLine = true,
                    placeholder = { Text(stringResource(R.string.search_placeholder)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    shape = MaterialTheme.shapes.extraLarge)
                Spacer(Modifier.width(8.dp))
                FilledTonalIconButton(onClick = { showFilters = true }, Modifier.size(52.dp)) { Icon(Icons.Outlined.Tune, stringResource(R.string.filters)) }
            }
            Row(Modifier.padding(top = 8.dp).fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { showDates = true }, label = { Text(if (crit.checkIn != null && crit.checkOut != null) et.hotelbooking.app.util.Dates.dayMonth(crit.checkIn!!) + " – " + et.hotelbooking.app.util.Dates.dayMonth(crit.checkOut!!) else stringResource(R.string.any_dates)) }, leadingIcon = { Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp)) })
                AssistChip(onClick = { showFilters = true }, label = { Text(pluralStringResource(R.plurals.guests_count, crit.guests, crit.guests)) }, leadingIcon = { Icon(Icons.Outlined.Person, null, Modifier.size(18.dp)) })
                if (crit.city != null) InputChip(true, { vm.criteria.value = crit.copy(city = null) }, label = { Text(stringResource(CityInfo.nameRes(crit.city!!))) }, trailingIcon = { Icon(Icons.Outlined.Close, null, Modifier.size(16.dp)) })
                Box {
                    AssistChip(onClick = { sortOpen = true }, label = { Text(stringResource(sortLabel(crit.sort))) }, leadingIcon = { Icon(Icons.Outlined.Sort, null, Modifier.size(18.dp)) })
                    DropdownMenu(sortOpen, { sortOpen = false }) {
                        SortOrder.entries.forEach { s -> DropdownMenuItem({ Text(stringResource(sortLabel(s))) }, { vm.criteria.value = crit.copy(sort = s); sortOpen = false }) }
                    }
                }
            }
        }
    }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            when (val s = state) {
                UiState.Loading -> LoadingBox()
                is UiState.Error -> ErrorBox(stringResource(R.string.error_generic)) { svm.run(crit) }
                is UiState.Ready -> if (s.data.isEmpty()) EmptyState(Icons.Outlined.SearchOff, stringResource(R.string.no_results), stringResource(R.string.no_results_sub),
                    action = stringResource(R.string.reset_filters), onAction = { vm.criteria.value = SearchCriteria(guests = crit.guests) })
                else LazyColumn(contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (recentSearches.isNotEmpty()) {
                        item {
                            Column {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(R.string.recent_searches), style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { vm.clearRecentSearches() }) { Text(stringResource(R.string.clear_recent_searches)) }
                                }
                                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    recentSearches.forEach { rs ->
                                        val cityName = rs.city?.let { stringResource(CityInfo.nameRes(it)) } ?: stringResource(R.string.any_city)
                                        val dates = if (rs.checkIn != null && rs.checkOut != null)
                                            et.hotelbooking.app.util.Dates.dayMonth(rs.checkIn) + " – " + et.hotelbooking.app.util.Dates.dayMonth(rs.checkOut) else null
                                        val label = listOfNotNull(cityName, dates, pluralStringResource(R.plurals.guests_count, rs.guests, rs.guests))
                                            .joinToString(" · ")
                                        AssistChip(onClick = { vm.applyRecentSearch(rs) }, label = { Text(label) },
                                            leadingIcon = { Icon(Icons.Outlined.History, null, Modifier.size(18.dp)) })
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                    item { Text(stringResource(R.string.results_count, s.data.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    items(s.data, key = { it.hotel.id }) { r ->
                        HotelCard(r.hotel, r.fromPriceEtb, { onHotel(r.hotel.id) }, roomsLeft = r.roomsLeft, compareSelected = r.hotel.id in compare,
                            onToggleCompare = { if (!vm.toggleCompare(r.hotel.id)) toast = true },
                            saved = r.hotel.id in savedIds, onToggleSaved = { vm.toggleSaved(r.hotel.id) })
                    }
                }
            }
            if (compare.isNotEmpty()) CompareBar(compare.size, onCompare, { vm.clearCompare() }, Modifier.align(Alignment.BottomCenter))
        }
    }
    if (showDates) DateRangeDialog(crit.checkIn, crit.checkOut, { showDates = false }, { a, b -> vm.criteria.value = crit.copy(checkIn = a, checkOut = b); showDates = false })
    if (showFilters) ModalBottomSheet(onDismissRequest = { showFilters = false }) {
        FiltersSheet(crit, onApply = { vm.criteria.value = it; vm.recordRecentSearch(); showFilters = false })
    }
}

fun sortLabel(s: SortOrder) = when (s) {
    SortOrder.RECOMMENDED -> R.string.sort_recommended; SortOrder.PRICE_LOW -> R.string.sort_price_low; SortOrder.PRICE_HIGH -> R.string.sort_price_high
    SortOrder.RATING -> R.string.sort_rating; SortOrder.STARS -> R.string.sort_stars
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FiltersSheet(initial: SearchCriteria, onApply: (SearchCriteria) -> Unit) {
    var c by remember { mutableStateOf(initial) }
    var showDates by remember { mutableStateOf(false) }
    val cur = LocalSettings.current.currency
    Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.filters), style = MaterialTheme.typography.titleLarge)
        CityDropdown(c.city, { c = c.copy(city = it, region = null) })
        Text(stringResource(R.string.region), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CityInfo.regions.forEach { r -> FilterChip(c.region == r, { c = c.copy(region = if (c.region == r) null else r, city = null) }, label = { Text(stringResource(CityInfo.regionName(r))) }) }
        }
        DateRangeButton(c.checkIn, c.checkOut, { showDates = true })
        Stepper(stringResource(R.string.guests), c.guests, { c = c.copy(guests = it) }, 1, 8)
        val maxP = if (c.maxPrice == Double.MAX_VALUE) PRICE_MAX else c.maxPrice.toFloat()
        Text(stringResource(R.string.price_per_night) + ": " + Money.inCurrency(c.minPrice, cur) + " – " + (if (maxP >= PRICE_MAX) Money.inCurrency(PRICE_MAX.toDouble(), cur) + "+" else Money.inCurrency(maxP.toDouble(), cur)), style = MaterialTheme.typography.labelLarge)
        RangeSlider(value = c.minPrice.toFloat()..maxP, onValueChange = { r -> c = c.copy(minPrice = r.start.toDouble(), maxPrice = if (r.endInclusive >= PRICE_MAX) Double.MAX_VALUE else r.endInclusive.toDouble()) }, valueRange = 0f..PRICE_MAX, steps = 23)
        Text(stringResource(R.string.star_rating), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { s -> FilterChip(s in c.stars, { c = c.copy(stars = if (s in c.stars) c.stars - s else c.stars + s) }, label = { Text("$s★") }) }
        }
        Text(stringResource(R.string.guest_rating), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0.0 to R.string.any, 3.5 to R.string.rating_35, 4.0 to R.string.rating_40, 4.5 to R.string.rating_45).forEach { (v, l) ->
                FilterChip(c.minRating == v, { c = c.copy(minRating = v) }, label = { Text(stringResource(l)) })
            }
        }
        Text(stringResource(R.string.facilities), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Facility.entries.forEach { f -> FilterChip(f in c.facilities, { c = c.copy(facilities = if (f in c.facilities) c.facilities - f else c.facilities + f) },
                label = { Text(stringResource(Labels.facility(f))) }, leadingIcon = { Icon(Labels.facilityIcon(f), null, Modifier.size(16.dp)) }) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { c = SearchCriteria(guests = c.guests, sort = c.sort) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.reset_filters)) }
            Button(onClick = { onApply(c) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.apply)) }
        }
    }
    if (showDates) DateRangeDialog(c.checkIn, c.checkOut, { showDates = false }, { a, b -> c = c.copy(checkIn = a, checkOut = b); showDates = false })
}
