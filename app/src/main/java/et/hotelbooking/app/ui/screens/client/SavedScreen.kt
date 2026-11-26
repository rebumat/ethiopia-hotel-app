package et.hotelbooking.app.ui.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import et.hotelbooking.app.R
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.SavedHotelsViewModel

/** Saved hotels (favourites). Works without signing in — saved on this device. */
@Composable
fun SavedHotelsScreen(vm: AppViewModel, onHotel: (Long) -> Unit, onBack: () -> Unit) {
    val savedIds by vm.savedIds.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val svm: SavedHotelsViewModel = viewModel { SavedHotelsViewModel(vm.c) }
    val hotels by svm.hotels.collectAsStateWithLifecycle()
    val prices by svm.minPrices.collectAsStateWithLifecycle()
    val tourist = settings.mode == UserMode.TOURIST
    fun priceOf(id: Long) = prices[id]?.let { if (tourist) it.second else it.first }

    LaunchedEffect(savedIds) { svm.load(savedIds.toList()) }

    Scaffold(topBar = { AppTopBar(stringResource(R.string.saved_hotels), onBack) }) { pad ->
        if (savedIds.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize()) {
                EmptyState(Icons.Outlined.FavoriteBorder, stringResource(R.string.no_saved_hotels),
                    stringResource(R.string.no_saved_hotels_sub))
            }
        } else {
            LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text(stringResource(R.string.saved_count, savedIds.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                items(hotels, key = { it.id }) { h ->
                    HotelCard(h, priceOf(h.id), { onHotel(h.id) }, saved = true, onToggleSaved = { vm.toggleSaved(h.id) })
                }
            }
        }
    }
}
