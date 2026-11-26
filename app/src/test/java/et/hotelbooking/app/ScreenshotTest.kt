package et.hotelbooking.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import et.hotelbooking.app.data.repo.BookingRequest
import et.hotelbooking.app.data.repo.DemoPayment
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.ui.components.LocalSettings
import et.hotelbooking.app.ui.screens.admin.AdminUserScreen
import et.hotelbooking.app.ui.screens.booking.ConfirmationView
import et.hotelbooking.app.ui.screens.client.CompareScreen
import et.hotelbooking.app.ui.screens.client.HomeScreen
import et.hotelbooking.app.ui.screens.client.HotelDetailScreen
import et.hotelbooking.app.ui.screens.manager.CheckOutScreen
import et.hotelbooking.app.ui.screens.manager.FrontDeskScreen
import et.hotelbooking.app.ui.screens.manager.PmsDashboardScreen
import et.hotelbooking.app.ui.screens.manager.ReportsScreen
import et.hotelbooking.app.ui.theme.EthioTheme
import et.hotelbooking.app.ui.vm.AppViewModel
import et.hotelbooking.app.ui.vm.BookingDraft
import et.hotelbooking.app.ui.vm.PmsViewModel
import et.hotelbooking.app.util.Currency
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.os.Looper

private const val OUT = "/workspace/ethiopia-hotel-app/screenshots"
private const val PHONE = "w411dp-h891dp-xhdpi"

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE)
class ScreenshotTest {
    @get:Rule val rule = createComposeRule()

    private val app get() = ApplicationProvider.getApplicationContext<EthioHotelsApp>()
    private val c get() = app.container

    private fun prepare(email: String?, password: String = "", mode: UserMode = UserMode.LOCAL, currency: Currency? = null) = runBlocking {
        c.ready.first { it }
        c.settings.setMode(mode)
        c.settings.update { it.copy(onboardingDone = true, theme = ThemeMode.LIGHT, currency = currency ?: it.currency) }
        if (email != null) c.auth.login(email, password).getOrThrow()
    }

    private fun show(content: @Composable (AppViewModel) -> Unit) {
        val vm = AppViewModel(c)
        rule.setContent {
            val s by vm.settings.collectAsState()
            EthioTheme(ThemeMode.LIGHT) {
                CompositionLocalProvider(LocalSettings provides s) {
                    Surface(color = MaterialTheme.colorScheme.background) { content(vm) }
                }
            }
        }
    }

    /** Lets Room flows (background threads) deliver and Compose settle. */
    private fun settle(rounds: Int = 25) {
        repeat(rounds) { Thread.sleep(80); shadowOf(Looper.getMainLooper()).idle(); rule.waitForIdle() }
    }

    private fun shot(name: String) { settle(); rule.onRoot().captureRoboImage("$OUT/$name.png") }

    @Test fun s01_home_landmarks() {
        prepare("client@ethiohotels.demo", "Client@123")
        show { vm -> HomeScreen(vm, {}, {}, {}, {}, {}, {}) }
        shot("01_home_landmarks")
    }

    @Config(qualifiers = "w411dp-h1500dp-xhdpi")
    @Test fun s01b_home_full_tourist() {
        prepare("tourist@ethiohotels.demo", "Tourist@123", UserMode.TOURIST, Currency.USD)
        show { vm -> HomeScreen(vm, {}, {}, {}, {}, {}, {}) }
        shot("01b_home_full_tourist_mode")
    }

    @Test fun s02_hotel_details() {
        prepare("tourist@ethiohotels.demo", "Tourist@123", UserMode.TOURIST, Currency.USD)
        show { vm -> HotelDetailScreen(vm, 1L, {}, { _, _ -> }, {}) }
        shot("02_hotel_details")
    }

    @Test fun s03_compare() {
        prepare("client@ethiohotels.demo", "Client@123")
        show { vm -> vm.toggleCompare(1L); vm.toggleCompare(2L); vm.toggleCompare(4L); CompareScreen(vm, {}, {}) }
        shot("03_compare_hotels")
    }

    @Test fun s04_booking_confirmation() {
        prepare("tourist@ethiohotels.demo", "Tourist@123", UserMode.TOURIST, Currency.USD)
        val (b, draft) = runBlocking {
            val u = c.auth.currentUser.first()!!
            val hotel = c.hotels.hotel(1L).first()!!
            val type = c.hotels.roomTypes(1L).first().first()
            val from = Dates.today() + 20; val to = from + 3
            val q = c.bookings.quote(1L, type.id, from, to, false)
            val req = BookingRequest(u.id, 1L, type.id, from, to, 2, 0, u.fullName, u.phone, u.email, u.nationality, u.idType, u.idNumber, false, "Late arrival")
            val b = c.bookings.create(req, DemoPayment(PaymentMethod.VISA, q.total, "VISA", "1111")).getOrThrow()
            b to BookingDraft(hotel = hotel, typeId = type.id, checkIn = from, checkOut = to, quote = q, resident = false)
        }
        show { ConfirmationView(b.reference, b.status, draft, b.paymentStatus) {} }
        shot("04_booking_confirmation")
    }

    @Config(qualifiers = "am-$PHONE")
    @Test fun s05_amharic_home() {
        prepare("client@ethiohotels.demo", "Client@123")
        show { vm -> HomeScreen(vm, {}, {}, {}, {}, {}, {}) }
        shot("05_amharic_home")
    }

    @Config(qualifiers = "om-$PHONE")
    @Test fun s05b_oromo_hotel() {
        prepare("client@ethiohotels.demo", "Client@123")
        show { vm -> HotelDetailScreen(vm, 1L, {}, { _, _ -> }, {}) }
        shot("05b_afaan_oromo_hotel_details")
    }

    @Config(qualifiers = "ar-$PHONE")
    @Test fun s05c_arabic_rtl_home() {
        prepare("tourist@ethiohotels.demo", "Tourist@123", UserMode.TOURIST, Currency.USD)
        show { vm -> HomeScreen(vm, {}, {}, {}, {}, {}, {}) }
        shot("05c_arabic_rtl_home_tourist")
    }

    @Config(qualifiers = "w411dp-h1900dp-xhdpi")
    @Test fun s11_register_id_upload() {
        prepare(null, mode = UserMode.LOCAL)
        show { vm -> et.hotelbooking.app.ui.screens.auth.RegisterScreen(vm, {}, {}, {}) }
        shot("11_register_id_upload_local")
    }

    @Test fun s06_manager_dashboard() {
        prepare("manager@ethiohotels.demo", "Manager@123")
        val u = runBlocking { c.auth.currentUser.first()!! }
        show { PmsDashboardScreen(PmsViewModel(c, u.hotelId!!), u, Permissions.of(u.role)) {} }
        shot("06_manager_dashboard")
    }

    @Test fun s07_front_desk() {
        prepare("reception@ethiohotels.demo", "Reception@123")
        val u = runBlocking { c.auth.currentUser.first()!! }
        show { FrontDeskScreen(PmsViewModel(c, u.hotelId!!), Permissions.of(u.role)) {} }
        shot("07_front_desk")
    }

    @Test fun s08_checkout_folio() {
        prepare("reception@ethiohotels.demo", "Reception@123")
        val u = runBlocking { c.auth.currentUser.first()!! }
        val inHouse = runBlocking { c.pms.hotelBookings(u.hotelId!!).first().first { it.status == BookingStatus.CHECKED_IN && c.pms.folio(it.id).first().isNotEmpty() } }
        show { CheckOutScreen(PmsViewModel(c, u.hotelId!!), inHouse.id) {} }
        shot("08_checkout_folio")
    }

    @Test fun s09_reports() {
        prepare("manager@ethiohotels.demo", "Manager@123")
        val u = runBlocking { c.auth.currentUser.first()!! }
        show { ReportsScreen(PmsViewModel(c, u.hotelId!!)) {} }
        shot("09_reports")
    }

    @Test fun s10_admin_id_review() {
        prepare("admin@ethiohotels.demo", "Admin@123")
        val pending = runBlocking { c.admin.users().first().first { it.verification == VerificationStatus.PENDING } }
        show { vm -> AdminUserScreen(vm, pending.id) {} }
        shot("10_admin_id_review")
    }
}
