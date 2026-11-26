package et.hotelbooking.app.ui.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import et.hotelbooking.app.R
import et.hotelbooking.app.domain.Permission
import et.hotelbooking.app.domain.Permissions
import et.hotelbooking.app.domain.Role
import et.hotelbooking.app.ui.components.LocalSettings
import et.hotelbooking.app.ui.screens.admin.*
import et.hotelbooking.app.ui.screens.auth.*
import et.hotelbooking.app.ui.screens.booking.*
import et.hotelbooking.app.ui.screens.client.*
import et.hotelbooking.app.ui.screens.common.*
import et.hotelbooking.app.ui.screens.manager.*
import et.hotelbooking.app.ui.screens.onboarding.OnboardingScreen
import et.hotelbooking.app.ui.theme.EthioTheme
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.PmsViewModel

object R0 {
    const val ONBOARDING = "onboarding"; const val LOGIN = "login"; const val REGISTER = "register"
    const val HOME = "home"; const val EXPLORE = "explore"; const val BOOKINGS = "bookings"; const val ALERTS = "alerts"; const val PROFILE = "profile"
    const val HOTEL = "hotel/{id}"; const val COMPARE = "compare"; const val SAVED = "saved"; const val BOOK = "book/{hotelId}?type={type}"; const val BOOKING = "booking/{id}"
    const val LANDMARK = "landmark/{key}"; const val CREDITS = "credits"; const val ABOUT = "about"; const val PRIVACY = "privacy"; const val ID_UPLOAD = "id_upload"
    const val PMS_DASH = "pms"; const val PMS_DESK = "pms_desk"; const val PMS_ROOMS = "pms_rooms"; const val PMS_HK = "pms_hk"; const val PMS_RES = "pms_res"
    const val PMS_CHECKIN = "pms_checkin/{id}"; const val PMS_CHECKOUT = "pms_checkout/{id}"; const val PMS_WALKIN = "pms_walkin"; const val PMS_LOOKUP = "pms_lookup"
    const val PMS_RATES = "pms_rates"; const val PMS_GUESTS = "pms_guests"; const val PMS_STAFF = "pms_staff"; const val PMS_CAL = "pms_cal"; const val PMS_REPORTS = "pms_reports"
    const val PMS_HOTEL = "pms_hotel"; const val PMS_BOOKING = "pms_booking/{id}"
    const val ADMIN = "admin"; const val ADMIN_HOTELS = "admin_hotels"; const val ADMIN_USERS = "admin_users"; const val ADMIN_BOOKINGS = "admin_bookings"; const val ADMIN_USER = "admin_user/{id}"
    fun hotel(id: Long) = "hotel/$id"; fun book(h: Long, t: Long? = null) = "book/$h" + (if (t != null) "?type=$t" else "")
    fun booking(id: Long) = "booking/$id"; fun landmark(k: String) = "landmark/$k"
    fun checkin(id: Long) = "pms_checkin/$id"; fun checkout(id: Long) = "pms_checkout/$id"; fun pmsBooking(id: Long) = "pms_booking/$id"; fun adminUser(id: Long) = "admin_user/$id"
}

data class Tab(val route: String, val label: Int, val icon: ImageVector, val selectedIcon: ImageVector)

fun tabsFor(role: Role?): List<Tab> = when (role) {
    Role.ADMIN -> listOf(
        Tab(R0.ADMIN, R.string.nav_dashboard, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        Tab(R0.ADMIN_HOTELS, R.string.nav_hotels, Icons.Outlined.Apartment, Icons.Filled.Apartment),
        Tab(R0.ADMIN_USERS, R.string.nav_users, Icons.Outlined.Group, Icons.Filled.Group),
        Tab(R0.ADMIN_BOOKINGS, R.string.nav_reservations, Icons.Outlined.EventNote, Icons.Filled.EventNote),
        Tab(R0.PROFILE, R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person))
    Role.MANAGER, Role.RECEPTIONIST -> listOf(
        Tab(R0.PMS_DASH, R.string.nav_dashboard, Icons.Outlined.Dashboard, Icons.Filled.Dashboard),
        Tab(R0.PMS_DESK, R.string.nav_front_desk, Icons.Outlined.ConciergeBell(), Icons.Filled.RoomService),
        Tab(R0.PMS_ROOMS, R.string.nav_rooms, Icons.Outlined.MeetingRoom, Icons.Filled.MeetingRoom),
        Tab(R0.PMS_RES, R.string.nav_reservations, Icons.Outlined.EventNote, Icons.Filled.EventNote),
        Tab(R0.PROFILE, R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person))
    Role.HOUSEKEEPING -> listOf(
        Tab(R0.PMS_HK, R.string.nav_housekeeping, Icons.Outlined.CleaningServices, Icons.Filled.CleaningServices),
        Tab(R0.PMS_ROOMS, R.string.nav_rooms, Icons.Outlined.MeetingRoom, Icons.Filled.MeetingRoom),
        Tab(R0.PROFILE, R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person))
    else -> listOf(
        Tab(R0.HOME, R.string.nav_home, Icons.Outlined.Home, Icons.Filled.Home),
        Tab(R0.EXPLORE, R.string.nav_explore, Icons.Outlined.TravelExplore, Icons.Filled.TravelExplore),
        Tab(R0.BOOKINGS, R.string.nav_bookings, Icons.Outlined.Luggage, Icons.Filled.Luggage),
        Tab(R0.ALERTS, R.string.nav_alerts, Icons.Outlined.Notifications, Icons.Filled.Notifications),
        Tab(R0.PROFILE, R.string.nav_profile, Icons.Outlined.Person, Icons.Filled.Person))
}

@Suppress("FunctionName") private fun Icons.Outlined.ConciergeBell() = Icons.Outlined.RoomService

fun startFor(role: Role?) = tabsFor(role).first().route

@Composable
fun AppRoot(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val ready by vm.ready.collectAsStateWithLifecycle()
    val user by vm.user.collectAsStateWithLifecycle()
    EthioTheme(settings.theme) {
        CompositionLocalProvider(LocalSettings provides settings) {
            Surface(color = MaterialTheme.colorScheme.background) {
                if (!ready) return@Surface
                val nav = rememberNavController()
                val start = remember { if (!settings.onboardingDone) R0.ONBOARDING else startFor(null) }
                // Route the user to the right workspace whenever the role changes (login/logout).
                var lastRole by remember { mutableStateOf<Role?>(null) }
                var roleInit by remember { mutableStateOf(false) }
                LaunchedEffect(user?.role, settings.onboardingDone) {
                    val r = user?.role
                    if (!settings.onboardingDone) return@LaunchedEffect
                    if (!roleInit || r != lastRole) {
                        val firstTime = !roleInit
                        roleInit = true; lastRole = r
                        if (firstTime && r == null) return@LaunchedEffect
                        nav.navigate(startFor(r)) { popUpTo(0) { inclusive = true }; launchSingleTop = true }
                    }
                }
                MainScaffold(vm, nav, start)
            }
        }
    }
}

@Composable
private fun MainScaffold(vm: AppViewModel, nav: NavHostController, start: String) {
    val user by vm.user.collectAsStateWithLifecycle()
    val unread by vm.unread.collectAsStateWithLifecycle()
    val tabs = tabsFor(user?.role)
    val back by nav.currentBackStackEntryAsState()
    val route = back?.destination?.route
    val showBar = tabs.any { it.route == route }
    Scaffold(
        bottomBar = {
            if (showBar) NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 3.dp) {
                tabs.forEach { t ->
                    val sel = route == t.route
                    NavigationBarItem(selected = sel, onClick = {
                        nav.navigate(t.route) { popUpTo(nav.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true }
                    }, icon = {
                        if (t.route == R0.ALERTS && unread > 0) BadgedBox(badge = { Badge { Text(unread.toString()) } }) { Icon(if (sel) t.selectedIcon else t.icon, null) }
                        else Icon(if (sel) t.selectedIcon else t.icon, null)
                    }, label = { Text(stringResource(t.label), maxLines = 1) })
                }
            }
        },
        contentWindowInsets = WindowInsets(0)
    ) { pad ->
        Box(Modifier.padding(pad).consumeWindowInsets(pad)) { Graph(vm, nav, start) }
    }
}

@Composable
private fun Pms(vm: PmsViewModel?, user: et.hotelbooking.app.data.local.UserEntity?, content: @Composable (PmsViewModel, et.hotelbooking.app.data.local.UserEntity) -> Unit) {
    if (vm == null || user == null) et.hotelbooking.app.ui.components.EmptyState(Icons.Outlined.Lock, stringResource(R.string.no_access_title), stringResource(R.string.no_access_msg))
    else content(vm, user)
}

@Composable
private fun Graph(vm: AppViewModel, nav: NavHostController, start: String) {
    val user by vm.user.collectAsStateWithLifecycle()
    val hotelId = user?.hotelId
    val up: () -> Unit = { nav.navigateUp() }
    val pmsVm: PmsViewModel? = if (hotelId != null && user?.role?.isStaff == true) viewModel(key = "pms_$hotelId") { PmsViewModel(vm.c, hotelId) } else null
    val perms = user?.role?.let { Permissions.of(it) } ?: emptySet()
    val requireLogin: (String) -> Unit = { nav.navigate(R0.LOGIN) }

    NavHost(nav, startDestination = start) {
        composable(R0.ONBOARDING) { OnboardingScreen(vm, onDone = { login -> vm.finishOnboarding(); nav.navigate(if (login) R0.LOGIN else R0.HOME) { popUpTo(0) { inclusive = true } } }) }
        composable(R0.LOGIN) { LoginScreen(vm, onBack = if (nav.previousBackStackEntry != null) up else null, onRegister = { nav.navigate(R0.REGISTER) }, onDone = { if (nav.previousBackStackEntry != null) nav.popBackStack() }) }
        composable(R0.REGISTER) { RegisterScreen(vm, onBack = up, onDone = { nav.popBackStack(R0.LOGIN, inclusive = true) }, onPrivacy = { nav.navigate(R0.PRIVACY) }) }
        // Client
        composable(R0.HOME) { HomeScreen(vm, onSearch = { nav.navigate(R0.EXPLORE) { launchSingleTop = true } }, onHotel = { nav.navigate(R0.hotel(it)) },
            onLandmark = { nav.navigate(R0.landmark(it)) }, onCity = { vm.criteria.value = vm.criteria.value.copy(city = it); nav.navigate(R0.EXPLORE) { launchSingleTop = true } },
            onCompare = { nav.navigate(R0.COMPARE) }, onLogin = { nav.navigate(R0.LOGIN) }) }
        composable(R0.EXPLORE) { SearchScreen(vm, onHotel = { nav.navigate(R0.hotel(it)) }, onCompare = { nav.navigate(R0.COMPARE) }) }
        composable(R0.BOOKINGS) { MyBookingsScreen(vm, onOpen = { nav.navigate(R0.booking(it)) }, onLogin = { nav.navigate(R0.LOGIN) }, onExplore = { nav.navigate(R0.EXPLORE) }) }
        composable(R0.ALERTS) { NotificationsScreen(vm, onLogin = { nav.navigate(R0.LOGIN) }, onBooking = { nav.navigate(R0.booking(it)) }) }
        composable(R0.PROFILE) { ProfileScreen(vm, onLogin = { nav.navigate(R0.LOGIN) }, onCredits = { nav.navigate(R0.CREDITS) }, onAbout = { nav.navigate(R0.ABOUT) },
            onPrivacy = { nav.navigate(R0.PRIVACY) }, onIdUpload = { nav.navigate(R0.ID_UPLOAD) }, onSaved = { nav.navigate(R0.SAVED) }) }
        composable(R0.SAVED) { SavedHotelsScreen(vm, onHotel = { nav.navigate(R0.hotel(it)) }, onBack = up) }
        composable(R0.HOTEL, listOf(navArgument("id") { type = NavType.LongType })) { e ->
            HotelDetailScreen(vm, e.arguments!!.getLong("id"), onBack = up, onBook = { h, t -> if (user == null) requireLogin("") else nav.navigate(R0.book(h, t)) }, onCompare = { nav.navigate(R0.COMPARE) })
        }
        composable(R0.COMPARE) { CompareScreen(vm, onBack = up, onHotel = { nav.navigate(R0.hotel(it)) }) }
        composable(R0.BOOK, listOf(navArgument("hotelId") { type = NavType.LongType }, navArgument("type") { type = NavType.LongType; defaultValue = -1L })) { e ->
            val t = e.arguments!!.getLong("type").takeIf { it > 0 }
            BookingFlowScreen(vm, e.arguments!!.getLong("hotelId"), t, onBack = up, onDone = { id -> nav.navigate(R0.booking(id)) { popUpTo(R0.HOME) } })
        }
        composable(R0.BOOKING, listOf(navArgument("id") { type = NavType.LongType })) { e -> BookingDetailScreen(vm, e.arguments!!.getLong("id"), onBack = up, onHotel = { nav.navigate(R0.hotel(it)) }) }
        composable(R0.LANDMARK, listOf(navArgument("key") { type = NavType.StringType })) { e ->
            LandmarkScreen(e.arguments!!.getString("key")!!, onBack = up, onFindHotels = { city -> vm.criteria.value = vm.criteria.value.copy(city = city); nav.navigate(R0.EXPLORE) })
        }
        composable(R0.CREDITS) { CreditsScreen(onBack = up) }
        composable(R0.ABOUT) { AboutScreen(onBack = up) }
        composable(R0.PRIVACY) { PrivacyScreen(onBack = up) }
        composable(R0.ID_UPLOAD) { IdUploadScreen(vm, onBack = up) }
        // Hotel management (PMS)
        composable(R0.PMS_DASH) { Pms(pmsVm, user) { pv, u -> PmsDashboardScreen(pv, u, perms, go = { nav.navigate(it) }) } }
        composable(R0.PMS_DESK) { Pms(pmsVm, user) { pv, u -> FrontDeskScreen(pv, perms, go = { nav.navigate(it) }) } }
        composable(R0.PMS_ROOMS) { Pms(pmsVm, user) { pv, u -> RoomsScreen(pv, perms, onBack = null) } }
        composable(R0.PMS_HK) { Pms(pmsVm, user) { pv, u -> HousekeepingScreen(pv, u, perms, onBack = if (u.role == Role.HOUSEKEEPING) null else up) } }
        composable(R0.PMS_RES) { Pms(pmsVm, user) { pv, u -> ReservationsScreen(pv, perms, onOpen = { nav.navigate(R0.pmsBooking(it)) }) } }
        composable(R0.PMS_BOOKING, listOf(navArgument("id") { type = NavType.LongType })) { e -> Pms(pmsVm, user) { pv, u -> PmsBookingScreen(pv, e.arguments!!.getLong("id"), perms, onBack = up, go = { nav.navigate(it) }) } }
        composable(R0.PMS_CHECKIN, listOf(navArgument("id") { type = NavType.LongType })) { e -> Pms(pmsVm, user) { pv, u -> CheckInScreen(pv, e.arguments!!.getLong("id"), onBack = up, onDone = { nav.popBackStack() }) } }
        composable(R0.PMS_CHECKOUT, listOf(navArgument("id") { type = NavType.LongType })) { e -> Pms(pmsVm, user) { pv, u -> CheckOutScreen(pv, e.arguments!!.getLong("id"), onBack = up) } }
        composable(R0.PMS_WALKIN) { Pms(pmsVm, user) { pv, u -> WalkInScreen(pv, onBack = up, onDone = { nav.popBackStack() }) } }
        composable(R0.PMS_LOOKUP) { Pms(pmsVm, user) { pv, u -> LookupScreen(pv, onBack = up, onFound = { nav.navigate(R0.pmsBooking(it)) { popUpTo(R0.PMS_LOOKUP) { inclusive = true } } }) } }
        composable(R0.PMS_RATES) { Pms(pmsVm, user) { pv, u -> RatesScreen(pv, onBack = up) } }
        composable(R0.PMS_GUESTS) { Pms(pmsVm, user) { pv, u -> GuestsScreen(pv, onBack = up, onBooking = { nav.navigate(R0.pmsBooking(it)) }) } }
        composable(R0.PMS_STAFF) { Pms(pmsVm, user) { pv, u -> StaffScreen(pv, onBack = up) } }
        composable(R0.PMS_CAL) { Pms(pmsVm, user) { pv, u -> CalendarScreen(pv, onBack = up, onBooking = { nav.navigate(R0.pmsBooking(it)) }) } }
        composable(R0.PMS_REPORTS) { Pms(pmsVm, user) { pv, u -> ReportsScreen(pv, onBack = up) } }
        composable(R0.PMS_HOTEL) { Pms(pmsVm, user) { pv, u -> HotelProfileEditScreen(pv, onBack = up) } }
        // Admin
        composable(R0.ADMIN) { AdminDashboardScreen(vm, go = { nav.navigate(it) }) }
        composable(R0.ADMIN_HOTELS) { AdminHotelsScreen(vm) }
        composable(R0.ADMIN_USERS) { AdminUsersScreen(vm, onOpen = { nav.navigate(R0.adminUser(it)) }) }
        composable(R0.ADMIN_BOOKINGS) { AdminBookingsScreen(vm) }
        composable(R0.ADMIN_USER, listOf(navArgument("id") { type = NavType.LongType })) { e -> AdminUserScreen(vm, e.arguments!!.getLong("id"), onBack = up) }
    }
}
