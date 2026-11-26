package et.hotelbooking.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import et.hotelbooking.app.R
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.EthiopianCalendar
import java.util.Locale

private const val DAY_MS = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeDialog(from: Long?, to: Long?, onDismiss: () -> Unit, onConfirm: (Long, Long) -> Unit, allowPast: Boolean = false) {
    val today = Dates.today()
    val state = rememberDateRangePickerState(initialSelectedStartDateMillis = from?.times(DAY_MS), initialSelectedEndDateMillis = to?.times(DAY_MS),
        selectableDates = object : SelectableDates { override fun isSelectableDate(utcTimeMillis: Long) = allowPast || utcTimeMillis / DAY_MS >= today })
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = {
        TextButton(enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null && state.selectedEndDateMillis!! > state.selectedStartDateMillis!!,
            onClick = { onConfirm(state.selectedStartDateMillis!! / DAY_MS, state.selectedEndDateMillis!! / DAY_MS) }) { Text(stringResource(R.string.ok)) }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }) {
        DateRangePicker(state, Modifier.weight(1f), title = { Text(stringResource(R.string.select_dates), Modifier.padding(start = 24.dp, top = 16.dp)) })
    }
}

@Composable
fun DateRangeButton(from: Long?, to: Long?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val eth = LocalSettings.current.showEthiopianCalendar
    val lang = Locale.getDefault().language
    OutlinedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            if (from == null || to == null) Text(stringResource(R.string.select_dates), color = MaterialTheme.colorScheme.onSurfaceVariant)
            else Column(Modifier.weight(1f)) {
                Text(Dates.dayMonth(from) + "  →  " + Dates.dayMonth(to) + "  ·  " + pluralStringResource(R.plurals.nights_count, ((to - from).toInt()), ((to - from).toInt())), fontWeight = FontWeight.SemiBold)
                if (eth) Text(EthiopianCalendar.format(Dates.of(from), lang) + " → " + EthiopianCalendar.format(Dates.of(to), lang), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun Stepper(label: String, value: Int, onChange: (Int) -> Unit, min: Int = 1, max: Int = 10, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        FilledTonalIconButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min) { Icon(Icons.Outlined.Remove, stringResource(R.string.decrease)) }
        Text(value.toString(), Modifier.widthIn(min = 32.dp), style = MaterialTheme.typography.titleMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        FilledTonalIconButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max) { Icon(Icons.Outlined.Add, stringResource(R.string.increase)) }
    }
}
