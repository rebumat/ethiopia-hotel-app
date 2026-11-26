package et.hotelbooking.app.ui.screens.manager

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import et.hotelbooking.app.R
import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.data.repo.GuestProfile
import et.hotelbooking.app.data.repo.ReportData
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.*
import et.hotelbooking.app.ui.vm.PmsViewModel
import et.hotelbooking.app.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RoomsScreen(pv: PmsViewModel, perms: Set<Permission>, onBack: (() -> Unit)?) {
    val board by pv.roomBoard.collectAsStateWithLifecycle()
    val types = rememberTypes(pv)
    var filter by remember { mutableStateOf<RoomStatus?>(null) }
    var sel by remember { mutableStateOf<Pair<RoomEntity, RoomStatus>?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, contentWindowInsets = WindowInsets(0), floatingActionButton = {
        if (Permission.MANAGE_ROOMS in perms) ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text(stringResource(R.string.add_room)) })
    }, topBar = {
        Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp)) {
            Text(stringResource(R.string.room_inventory), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(filter == null, { filter = null }, label = { Text(stringResource(R.string.all) + " (" + board.size + ")") })
                RoomStatus.entries.forEach { s -> FilterChip(filter == s, { filter = s }, label = { Text(stringResource(Labels.roomStatus(s)) + " (" + board.count { it.second == s } + ")") },
                    leadingIcon = { Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(roomStatusColor(s))) }) }
            }
        }
    }) { pad ->
        LazyVerticalGrid(GridCells.Adaptive(96.dp), Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(board.filter { filter == null || it.second == filter }, key = { it.first.id }) { (r, s) ->
                val c = roomStatusColor(s)
                Card(onClick = { sel = r to s }, colors = CardDefaults.cardColors(containerColor = c.copy(alpha = 0.12f)), modifier = Modifier.border(1.5.dp, c.copy(alpha = 0.6f), MaterialTheme.shapes.medium)) {
                    Column(Modifier.padding(10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(r.number, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(types[r.roomTypeId]?.name?.substringBefore(' ') ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        Text(stringResource(Labels.roomStatus(s)), style = MaterialTheme.typography.labelSmall, color = c, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }
    }
    sel?.let { (r, s) ->
        ModalBottomSheet(onDismissRequest = { sel = null }) {
            Column(Modifier.padding(20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.room_n, r.number) + " · " + (types[r.roomTypeId]?.name ?: ""), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.floor_n, r.floor) + " · " + stringResource(Labels.roomStatus(s)) + if (r.note.isNotBlank()) " · " + r.note else "", color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (Permission.UPDATE_ROOM_STATUS in perms) {
                    Text(stringResource(R.string.set_status), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(RoomStatus.AVAILABLE, RoomStatus.CLEANING, RoomStatus.MAINTENANCE).forEach { st ->
                            FilterChip(r.status == st, { pv.setRoomStatus(r.id, st); sel = null }, label = { Text(stringResource(Labels.roomStatus(st))) }, enabled = r.status != RoomStatus.OCCUPIED)
                        }
                    }
                    if (r.status == RoomStatus.OCCUPIED) Text(stringResource(R.string.occupied_status_note), style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { pv.createTask(r.id, TaskType.CLEANING, "", false, null); sel = null }) { Icon(Icons.Outlined.CleaningServices, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.request_cleaning)) }
                    OutlinedButton(onClick = { pv.createTask(r.id, TaskType.MAINTENANCE, "", true, null); sel = null }) { Icon(Icons.Outlined.Build, null); Spacer(Modifier.width(6.dp)); Text(stringResource(R.string.report_maintenance)) }
                }
                if (Permission.MANAGE_ROOMS in perms) TextButton(onClick = { pv.deleteRoom(r); sel = null }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text(stringResource(R.string.delete_room)) }
            }
        }
    }
    if (showAdd) {
        var number by remember { mutableStateOf("") }; var floor by remember { mutableStateOf("1") }; var typeId by remember { mutableStateOf(types.keys.firstOrNull()) }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text(stringResource(R.string.add_room)) }, text = {
            Column {
                OutlinedTextField(number, { number = it }, label = { Text(stringResource(R.string.room_number)) }, singleLine = true)
                OutlinedTextField(floor, { floor = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.floor)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { types.values.forEach { t -> FilterChip(typeId == t.id, { typeId = t.id }, label = { Text(t.name) }) } }
            }
        }, confirmButton = { Button(onClick = { typeId?.let { pv.addRoom(it, number, floor.toIntOrNull() ?: 1) }; showAdd = false }, enabled = number.isNotBlank() && typeId != null) { Text(stringResource(R.string.add)) } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.cancel)) } })
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HousekeepingScreen(pv: PmsViewModel, user: UserEntity, perms: Set<Permission>, onBack: (() -> Unit)?) {
    val tasks by pv.tasks.collectAsStateWithLifecycle()
    val staff by pv.staff.collectAsStateWithLifecycle()
    val roomNo = rememberRoomNumbers(pv)
    val rooms by pv.rooms.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var mine by remember { mutableStateOf(user.role == Role.HOUSEKEEPING) }
    var showNew by remember { mutableStateOf(false) }
    val canCreate = user.role != Role.HOUSEKEEPING
    Scaffold(contentWindowInsets = WindowInsets(0), topBar = {
        Column(Modifier.statusBarsPadding()) {
            if (onBack != null) AppTopBar(stringResource(R.string.housekeeping_board), onBack)
            else Text(stringResource(R.string.housekeeping_board), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(mine, { mine = !mine }, label = { Text(stringResource(R.string.my_tasks)) })
            }
            TabRow(tab) { TaskStatus.entries.forEachIndexed { i, s -> Tab(tab == i, { tab = i }, text = { Text(stringResource(Labels.taskStatus(s)) + " (" + tasks.count { it.status == s && (!mine || it.assignedTo == user.id) } + ")") }) } }
        }
    }, floatingActionButton = { if (canCreate) FloatingActionButton(onClick = { showNew = true }) { Icon(Icons.Outlined.Add, stringResource(R.string.new_task)) } }) { pad ->
        val st = TaskStatus.entries[tab]
        val list = tasks.filter { it.status == st && (!mine || it.assignedTo == user.id) }
        if (list.isEmpty()) EmptyState(Icons.Outlined.CleaningServices, stringResource(R.string.no_tasks), stringResource(R.string.no_tasks_sub), Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { t ->
                OutlinedCard {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("#" + (roomNo[t.roomId] ?: "?"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(Labels.task(t.type)), style = MaterialTheme.typography.titleSmall)
                                if (t.note.isNotBlank()) Text(t.note, style = MaterialTheme.typography.bodySmall)
                                Text(staff.firstOrNull { it.id == t.assignedTo }?.fullName ?: stringResource(R.string.unassigned), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            if (t.highPriority) Pill(stringResource(R.string.high_priority), MaterialTheme.colorScheme.tertiary)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (t.assignedTo == null && user.role == Role.HOUSEKEEPING) TextButton(onClick = { pv.setTask(t.copy(assignedTo = user.id), t.status) }) { Text(stringResource(R.string.take_task)) }
                            when (t.status) {
                                TaskStatus.TODO -> Button(onClick = { pv.setTask(t, TaskStatus.IN_PROGRESS) }) { Text(stringResource(R.string.start)) }
                                TaskStatus.IN_PROGRESS -> Button(onClick = { pv.setTask(t, TaskStatus.DONE) }) { Icon(Icons.Outlined.Check, null); Spacer(Modifier.width(4.dp)); Text(stringResource(R.string.mark_done)) }
                                TaskStatus.DONE -> Text(t.completedAt?.let { Dates.dateTime(it) } ?: "", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showNew) {
        var roomId by remember { mutableStateOf(rooms.firstOrNull()?.id) }; var type by remember { mutableStateOf(TaskType.CLEANING) }
        var note by remember { mutableStateOf("") }; var high by remember { mutableStateOf(false) }; var assignee by remember { mutableStateOf<Long?>(null) }
        AlertDialog(onDismissRequest = { showNew = false }, title = { Text(stringResource(R.string.new_task)) }, text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.room), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { rooms.forEach { r -> FilterChip(roomId == r.id, { roomId = r.id }, label = { Text(r.number, fontSize = 12.sp) }) } }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { TaskType.entries.forEach { tt -> FilterChip(type == tt, { type = tt }, label = { Text(stringResource(Labels.task(tt))) }) } }
                Text(stringResource(R.string.assign_to), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) { staff.filter { it.role == Role.HOUSEKEEPING && it.active }.forEach { s -> FilterChip(assignee == s.id, { assignee = if (assignee == s.id) null else s.id }, label = { Text(s.fullName) }) } }
                OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.note)) })
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(high, { high = it }); Text(stringResource(R.string.high_priority)) }
            }
        }, confirmButton = { Button(onClick = { roomId?.let { pv.createTask(it, type, note, high, assignee) }; showNew = false }) { Text(stringResource(R.string.add)) } },
            dismissButton = { TextButton(onClick = { showNew = false }) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable
fun RatesScreen(pv: PmsViewModel, onBack: () -> Unit) {
    val types by pv.types.collectAsStateWithLifecycle()
    val seasons by pv.seasons.collectAsStateWithLifecycle()
    val hotel by pv.hotel.collectAsStateWithLifecycle()
    var edit by remember { mutableStateOf<RoomTypeEntity?>(null) }
    var newSeason by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, topBar = { AppTopBar(stringResource(R.string.rates_title), onBack) }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                hotel?.let { h -> SectionCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text(stringResource(R.string.dual_pricing), style = MaterialTheme.typography.titleSmall); Text(stringResource(R.string.dual_pricing_desc), style = MaterialTheme.typography.bodySmall) }
                        Switch(h.dualPricing, { pv.saveHotel(h.copy(dualPricing = it)) })
                    }
                } }
            }
            item { Text(stringResource(R.string.room_types), style = MaterialTheme.typography.titleMedium) }
            items(types, key = { it.id }) { t ->
                OutlinedCard(onClick = { edit = t }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        HotelImage(t.image, Modifier.size(52.dp).clip(MaterialTheme.shapes.small)); Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(t.name, style = MaterialTheme.typography.titleSmall)
                            Text(stringResource(R.string.resident_rate) + ": " + Money.etb(t.priceEtb), style = MaterialTheme.typography.bodySmall)
                            if (t.foreignerPriceEtb != null) Text(stringResource(R.string.foreigner_rate) + ": " + Money.etb(t.foreignerPriceEtb) + " (≈ " + Money.inCurrency(t.foreignerPriceEtb, Currency.USD) + ")", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!t.active) Pill(stringResource(R.string.closed_for_sale), MaterialTheme.colorScheme.error)
                        Icon(Icons.Outlined.Edit, null)
                    }
                }
            }
            item { Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.seasonal_rates), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); TextButton(onClick = { newSeason = true }) { Text(stringResource(R.string.add)) } } }
            items(seasons, key = { "s" + it.id }) { s ->
                ListItem(headlineContent = { Text(s.name) }, supportingContent = { Text("%02d/%02d – %02d/%02d".format(s.startDay, s.startMonth, s.endDay, s.endMonth)) },
                    trailingContent = { Row(verticalAlignment = Alignment.CenterVertically) { Text((if (s.adjustmentPct > 0) "+" else "") + s.adjustmentPct + "%", fontWeight = FontWeight.Bold); IconButton(onClick = { pv.deleteSeason(s) }) { Icon(Icons.Outlined.Delete, stringResource(R.string.remove)) } } })
            }
        }
    }
    edit?.let { t ->
        var name by remember(t.id) { mutableStateOf(t.name) }; var price by remember(t.id) { mutableStateOf(t.priceEtb.toInt().toString()) }
        var fprice by remember(t.id) { mutableStateOf(t.foreignerPriceEtb?.toInt()?.toString() ?: "") }; var active by remember(t.id) { mutableStateOf(t.active) }
        AlertDialog(onDismissRequest = { edit = null }, title = { Text(stringResource(R.string.edit_room_type)) }, text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name)) }, singleLine = true)
                OutlinedTextField(price, { price = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.resident_rate) + " (ETB)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(fprice, { fprice = it.filter { c -> c.isDigit() } }, label = { Text(stringResource(R.string.foreigner_rate) + " (ETB)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text(stringResource(R.string.foreigner_rate_hint)) })
                Row(verticalAlignment = Alignment.CenterVertically) { Text(stringResource(R.string.open_for_sale), Modifier.weight(1f)); Switch(active, { active = it }) }
            }
        }, confirmButton = { Button(onClick = { pv.saveType(t.copy(name = name, priceEtb = price.toDoubleOrNull() ?: t.priceEtb, foreignerPriceEtb = fprice.toDoubleOrNull(), active = active)); edit = null }) { Text(stringResource(R.string.save)) } },
            dismissButton = { TextButton(onClick = { edit = null }) { Text(stringResource(R.string.cancel)) } })
    }
    if (newSeason) {
        var name by remember { mutableStateOf("") }; var from by remember { mutableStateOf("01-01") }; var to by remember { mutableStateOf("01-15") }; var pct by remember { mutableStateOf("10") }
        AlertDialog(onDismissRequest = { newSeason = false }, title = { Text(stringResource(R.string.add_season)) }, text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.name)) }, singleLine = true)
                OutlinedTextField(from, { from = it }, label = { Text(stringResource(R.string.start_mm_dd)) }, singleLine = true)
                OutlinedTextField(to, { to = it }, label = { Text(stringResource(R.string.end_mm_dd)) }, singleLine = true)
                OutlinedTextField(pct, { pct = it.filter { c -> c.isDigit() || c == '-' } }, label = { Text(stringResource(R.string.adjustment_pct)) }, singleLine = true)
            }
        }, confirmButton = { Button(onClick = {
            val f = from.split('-').mapNotNull { it.toIntOrNull() }; val t = to.split('-').mapNotNull { it.toIntOrNull() }
            if (f.size == 2 && t.size == 2 && name.isNotBlank()) pv.saveSeason(SeasonRateEntity(hotelId = pv.hotelId, name = name, startMonth = f[0], startDay = f[1], endMonth = t[0], endDay = t[1], adjustmentPct = pct.toIntOrNull() ?: 0))
            newSeason = false }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = { newSeason = false }) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable
fun GuestsScreen(pv: PmsViewModel, onBack: () -> Unit, onBooking: (Long) -> Unit) {
    var guests by remember { mutableStateOf<List<GuestProfile>?>(null) }
    var q by remember { mutableStateOf("") }
    var open by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { guests = pv.guests() }
    Scaffold(topBar = { AppTopBar(stringResource(R.string.qa_guests), onBack) }) { pad ->
        val l = guests ?: return@Scaffold LoadingBox(Modifier.padding(pad))
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text(stringResource(R.string.search_guests)) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }) }
            items(l.filter { q.isBlank() || it.name.contains(q, true) || it.phone.contains(q) }, key = { it.key }) { g ->
                OutlinedCard(onClick = { open = if (open == g.key) null else g.key }) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(40.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) { Text(g.name.take(1), fontWeight = FontWeight.Bold) }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(g.name, style = MaterialTheme.typography.titleSmall)
                                Text(listOf(g.phone, g.nationality).filter { it.isNotBlank() }.joinToString(" · "), style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(pluralStringResource(R.plurals.stays_n, g.stays.size, g.stays.size), style = MaterialTheme.typography.labelMedium)
                                Text(Money.etb(g.totalSpent), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (open == g.key) {
                            if (g.stays.size >= 3) Pill(stringResource(R.string.repeat_guest), MaterialTheme.colorScheme.secondary, Modifier.padding(top = 6.dp))
                            g.stays.take(10).forEach { b ->
                                Row(Modifier.fillMaxWidth().clickable { onBooking(b.id) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(Dates.dayMonth(b.checkIn) + " · " + pluralStringResource(R.plurals.nights_count, b.nights, b.nights), Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                    StatusChip(b.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffScreen(pv: PmsViewModel, onBack: () -> Unit) {
    val staff by pv.staff.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, topBar = { AppTopBar(stringResource(R.string.qa_staff), onBack) }, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = { showAdd = true }, icon = { Icon(Icons.Outlined.PersonAdd, null) }, text = { Text(stringResource(R.string.add_staff)) })
    }) { pad ->
        LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(staff, key = { it.id }) { s ->
                ListItem(headlineContent = { Text(s.fullName) }, supportingContent = { Text(stringResource(Labels.role(s.role)) + " · " + s.email) },
                    leadingContent = { Icon(when (s.role) { Role.MANAGER -> Icons.Outlined.ManageAccounts; Role.RECEPTIONIST -> Icons.Outlined.RoomService; else -> Icons.Outlined.CleaningServices }, null) },
                    trailingContent = { if (s.role != Role.MANAGER) Switch(s.active, { pv.setStaffActive(s.id, it) }) })
            }
            item {
                SectionCard(title = stringResource(R.string.permissions_matrix)) {
                    Row { Text("", Modifier.weight(1.6f)); listOf(Role.MANAGER, Role.RECEPTIONIST, Role.HOUSEKEEPING).forEach { Text(stringResource(Labels.role(it)), Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center) } }
                    Permission.entries.forEach { p ->
                        Row(Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(p.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }, Modifier.weight(1.6f), style = MaterialTheme.typography.labelSmall)
                            listOf(Role.MANAGER, Role.RECEPTIONIST, Role.HOUSEKEEPING).forEach { r ->
                                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { if (p in Permissions.of(r)) Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary) else Text("—", color = MaterialTheme.colorScheme.outline) }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showAdd) {
        var name by remember { mutableStateOf("") }; var email by remember { mutableStateOf("") }; var phone by remember { mutableStateOf("+251") }
        var pw by remember { mutableStateOf("") }; var role by remember { mutableStateOf(Role.RECEPTIONIST) }
        AlertDialog(onDismissRequest = { showAdd = false }, title = { Text(stringResource(R.string.add_staff)) }, text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.full_name)) }, singleLine = true)
                OutlinedTextField(email, { email = it.trim() }, label = { Text(stringResource(R.string.email)) }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text(stringResource(R.string.phone)) }, singleLine = true)
                OutlinedTextField(pw, { pw = it }, label = { Text(stringResource(R.string.temp_password)) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                Row { listOf(Role.RECEPTIONIST, Role.HOUSEKEEPING).forEach { r -> FilterChip(role == r, { role = r }, label = { Text(stringResource(Labels.role(r))) }, modifier = Modifier.padding(end = 6.dp)) } }
            }
        }, confirmButton = { Button(enabled = name.isNotBlank() && email.contains('@') && pw.length >= 8, onClick = { pv.addStaff(name, email, phone, pw, role); showAdd = false }) { Text(stringResource(R.string.add)) } },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.cancel)) } })
    }
}

@Composable
fun CalendarScreen(pv: PmsViewModel, onBack: () -> Unit, onBooking: (Long) -> Unit) {
    val rooms by pv.rooms.collectAsStateWithLifecycle()
    val bookings by pv.bookings.collectAsStateWithLifecycle()
    val types = rememberTypes(pv)
    var start by remember { mutableLongStateOf(Dates.today() - 1) }
    val days = (0 until 14).map { start + it }
    val active = bookings.orEmpty().filter { it.roomId != null && it.status in setOf(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT, BookingStatus.PENDING) && it.checkOut > start && it.checkIn < start + 14 }
    val byRoom = active.groupBy { it.roomId!! }
    val cellW = 44.dp; val labelW = 64.dp
    Scaffold(topBar = { AppTopBar(stringResource(R.string.occupancy_calendar), onBack, actions = {
        IconButton(onClick = { start -= 7 }) { Icon(Icons.Outlined.ChevronLeft, null) }
        TextButton(onClick = { start = Dates.today() - 1 }) { Text(stringResource(R.string.today)) }
        IconButton(onClick = { start += 7 }) { Icon(Icons.Outlined.ChevronRight, null) }
    }) }) { pad ->
        Column(Modifier.padding(pad)) {
            Row(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.CHECKED_OUT).forEach { s ->
                    Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).background(calColor(s))); Spacer(Modifier.width(4.dp)); Text(stringResource(Labels.status(s)), style = MaterialTheme.typography.labelSmall) }
                }
            }
            val hs = rememberScrollState()
            Row(Modifier.padding(start = 8.dp)) {
                Spacer(Modifier.width(labelW))
                Row(Modifier.horizontalScroll(hs)) {
                    days.forEach { d ->
                        val isToday = d == Dates.today()
                        Column(Modifier.width(cellW).padding(2.dp).background(if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, MaterialTheme.shapes.extraSmall), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(Dates.of(d).dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()).take(3), style = MaterialTheme.typography.labelSmall)
                            Text(Dates.of(d).dayOfMonth.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            LazyColumn(Modifier.padding(start = 8.dp)) {
                items(rooms, key = { it.id }) { r ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.width(labelW)) {
                            Text(r.number, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(types[r.roomTypeId]?.name?.substringBefore(' ') ?: "", style = MaterialTheme.typography.labelSmall, maxLines = 1, fontSize = 9.sp)
                        }
                        Row(Modifier.horizontalScroll(hs)) {
                            days.forEach { d ->
                                val b = byRoom[r.id]?.firstOrNull { it.checkIn <= d && it.checkOut > d }
                                Box(Modifier.width(cellW).height(34.dp).padding(1.dp).background(if (b != null) calColor(b.status) else MaterialTheme.colorScheme.surfaceContainerLow, MaterialTheme.shapes.extraSmall)
                                    .then(if (b != null) Modifier.clickable { onBooking(b.id) } else Modifier), contentAlignment = Alignment.CenterStart) {
                                    if (b != null && (b.checkIn == d || d == start)) Text(b.guestName.substringBefore(' '), color = Color.White, style = MaterialTheme.typography.labelSmall, maxLines = 1, fontSize = 9.sp, modifier = Modifier.padding(start = 3.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun calColor(s: BookingStatus): Color = when (s) {
    BookingStatus.CHECKED_IN -> MaterialTheme.colorScheme.primary
    BookingStatus.CHECKED_OUT -> MaterialTheme.colorScheme.outline
    BookingStatus.PENDING -> et.hotelbooking.app.ui.theme.LocalExtraColors.current.warning
    else -> et.hotelbooking.app.ui.theme.LocalExtraColors.current.info
}

@Composable
fun ReportsScreen(pv: PmsViewModel, onBack: () -> Unit) {
    var days by remember { mutableIntStateOf(30) }
    var data by remember { mutableStateOf<ReportData?>(null) }
    LaunchedEffect(days) { data = null; data = pv.report(days) }
    val cur = LocalSettings.current.currency
    Scaffold(topBar = { AppTopBar(stringResource(R.string.qa_reports), onBack) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(7, 30, 90, 180).forEach { d -> FilterChip(days == d, { days = d }, label = { Text(stringResource(R.string.last_n_days, d)) }) } }
            val r = data ?: return@Column LoadingBox()
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(R.string.occupancy_rate), "%.0f%%".format(r.occupancy * 100), Icons.Outlined.PieChart, Modifier.weight(1f))
                StatCard(stringResource(R.string.adr), Money.inCurrency(r.adr, cur), Icons.Outlined.Bed, Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(R.string.revpar), Money.inCurrency(r.revpar, cur), Icons.Outlined.TrendingUp, Modifier.weight(1f), tint = MaterialTheme.colorScheme.tertiary)
                StatCard(stringResource(R.string.room_revenue), Money.inCurrency(r.roomRevenue, cur), Icons.Outlined.Payments, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(stringResource(R.string.room_nights_sold), r.roomNightsSold.toString() + " / " + r.roomNightsAvailable, Icons.Outlined.NightsStay, Modifier.weight(1f))
                StatCard(stringResource(R.string.extras_revenue), Money.inCurrency(r.extrasRevenue, cur), Icons.Outlined.LocalBar, Modifier.weight(1f), tint = MaterialTheme.colorScheme.secondary)
            }
            SectionCard(title = stringResource(R.string.occupancy_trend)) { LineSpark(r.occupancyByDay.map { it.second }) }
            SectionCard(title = stringResource(R.string.revenue_by_month)) { BarChart(r.revenueByMonth, valueLabel = { v -> if (v >= 1_000_000) "%.1fM".format(v / 1_000_000) else "%.0fk".format(v / 1000) }) }
            SectionCard(title = stringResource(R.string.bookings_by_source)) {
                BarChart(r.bySource.map { (k, v) -> stringResource(Labels.source(k)) to v.toDouble() }, barColor = MaterialTheme.colorScheme.secondary)
            }
            SectionCard(title = stringResource(R.string.bookings_by_status)) {
                r.byStatus.filter { it.value > 0 }.forEach { (s, n) -> KeyValue(stringResource(Labels.status(s)), n.toString()) }
            }
            Text(stringResource(R.string.kpi_definitions), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotelProfileEditScreen(pv: PmsViewModel, onBack: () -> Unit) {
    val hotel by pv.hotel.collectAsStateWithLifecycle()
    val h0 = hotel ?: return LoadingBox()
    var h by remember(h0.id) { mutableStateOf(h0) }
    val snack = remember { SnackbarHostState() }
    PmsMessages(pv, snack)
    Scaffold(snackbarHost = { SnackbarHost(snack) }, topBar = { AppTopBar(stringResource(R.string.qa_hotel_profile), onBack, actions = { TextButton(onClick = { pv.saveHotel(h) }) { Text(stringResource(R.string.save)) } }) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(stringResource(Labels.hotelStatus(h0.status)), if (h0.status == HotelStatus.APPROVED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary)
            if (h0.status == HotelStatus.PENDING) Text(stringResource(R.string.hotel_pending_note), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) { h.imageList.forEach { HotelImage(it, Modifier.size(90.dp, 64.dp).clip(MaterialTheme.shapes.small)) } }
            Text(stringResource(R.string.photo_upload_note), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(h.name, { h = h.copy(name = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.hotel_name)) })
            OutlinedTextField(h.description, { h = h.copy(description = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.description)) }, minLines = 3)
            OutlinedTextField(h.address, { h = h.copy(address = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.address)) })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(h.latitude.toString(), { v -> v.toDoubleOrNull()?.let { h = h.copy(latitude = it) } }, Modifier.weight(1f), label = { Text(stringResource(R.string.latitude)) }, singleLine = true)
                OutlinedTextField(h.longitude.toString(), { v -> v.toDoubleOrNull()?.let { h = h.copy(longitude = it) } }, Modifier.weight(1f), label = { Text(stringResource(R.string.longitude)) }, singleLine = true)
            }
            OutlinedTextField(h.phone, { h = h.copy(phone = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.phone)) }, singleLine = true)
            OutlinedTextField(h.email, { h = h.copy(email = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.email)) }, singleLine = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(h.checkInTime, { h = h.copy(checkInTime = it) }, Modifier.weight(1f), label = { Text(stringResource(R.string.check_in)) }, singleLine = true)
                OutlinedTextField(h.checkOutTime, { h = h.copy(checkOutTime = it) }, Modifier.weight(1f), label = { Text(stringResource(R.string.check_out)) }, singleLine = true)
            }
            Stepper(stringResource(R.string.free_cancellation_days), h.freeCancelDays, { h = h.copy(freeCancelDays = it) }, 0, 30)
            Text(stringResource(R.string.facilities), style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Facility.entries.forEach { f -> val on = f in h.facilityList
                    FilterChip(on, { val l = if (on) h.facilityList - f else h.facilityList + f; h = h.copy(facilities = l.joinToString(",") { it.name }) }, label = { Text(stringResource(Labels.facility(f))) }) }
            }
            OutlinedTextField(h.childrenPolicy, { h = h.copy(childrenPolicy = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.children_policy)) })
            OutlinedTextField(h.extraPolicies, { h = h.copy(extraPolicies = it) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.other_policies)) })
            Button(onClick = { pv.saveHotel(h) }, Modifier.fillMaxWidth().height(50.dp)) { Text(stringResource(R.string.save)) }
        }
    }
}
