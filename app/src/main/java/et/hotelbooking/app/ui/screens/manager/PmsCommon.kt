package et.hotelbooking.app.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.BookingEntity
import et.hotelbooking.app.data.local.RoomTypeEntity
import et.hotelbooking.app.domain.RoomStatus
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.theme.LocalExtraColors
import et.hotelbooking.app.ui.vm.PmsViewModel
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.Money
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun roomStatusColor(s: RoomStatus): Color {
    val ex = LocalExtraColors.current
    return when (s) {
        RoomStatus.AVAILABLE -> ex.success; RoomStatus.OCCUPIED -> MaterialTheme.colorScheme.primary; RoomStatus.RESERVED -> ex.info
        RoomStatus.CLEANING -> ex.warning; RoomStatus.MAINTENANCE -> MaterialTheme.colorScheme.error
    }
}

/** Collects PMS messages (success / error codes) and shows them as snackbars. */
@Composable
fun PmsMessages(pv: PmsViewModel, host: SnackbarHostState) {
    val ok = stringResource(R.string.action_done)
    val err = stringResource(R.string.error_generic)
    LaunchedEffect(Unit) { pv.messages.collect { host.showSnackbar(if (it.startsWith("err:")) err + " (" + it.removePrefix("err:") + ")" else ok) } }
}

@Composable
fun BookingRow(b: BookingEntity, types: Map<Long, RoomTypeEntity>, roomNo: String?, modifier: Modifier = Modifier, actions: @Composable RowScope.() -> Unit = {}, onClick: (() -> Unit)? = null) {
    val content: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Text(roomNo ?: "—", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(b.guestName, style = MaterialTheme.typography.titleSmall)
                    Text(b.reference + " · " + (types[b.roomTypeId]?.name ?: ""), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(Dates.dayMonth(b.checkIn) + " → " + Dates.dayMonth(b.checkOut) + " · " + pluralStringResource(R.plurals.nights_count, b.nights, b.nights) + " · " + Money.etb(b.totalEtb), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(b.status)
                    Text(stringResource(Labels.source(b.source)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End, content = actions)
        }
    }
    if (onClick != null) OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth(), content = content)
    else OutlinedCard(modifier = modifier.fillMaxWidth(), content = content)
}

@Composable
fun rememberRoomNumbers(pv: PmsViewModel): Map<Long, String> {
    val rooms by pv.rooms.collectAsStateWithLifecycle()
    return remember(rooms) { rooms.associate { it.id to it.number } }
}

@Composable
fun rememberTypes(pv: PmsViewModel): Map<Long, RoomTypeEntity> {
    val types by pv.types.collectAsStateWithLifecycle()
    return remember(types) { types.associateBy { it.id } }
}
