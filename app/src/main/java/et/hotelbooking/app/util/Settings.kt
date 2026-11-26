package et.hotelbooking.app.util

import android.content.Context
import et.hotelbooking.app.domain.UserMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class Currency(val code: String) { ETB("ETB"), USD("USD"), EUR("EUR") }

data class AppSettings(
    val onboardingDone: Boolean = false,
    val mode: UserMode = UserMode.LOCAL,
    val currency: Currency = Currency.ETB,
    val showEthiopianCalendar: Boolean = true,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val sessionUserId: Long? = null,
)

/** Small preferences store (SharedPreferences) exposed as a StateFlow. Language is handled by AppCompat per-app locales. */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ethio_hotels_settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(read())
    val state: StateFlow<AppSettings> = _state.asStateFlow()

    private fun read() = AppSettings(
        onboardingDone = prefs.getBoolean("onboarding", false),
        mode = runCatching { UserMode.valueOf(prefs.getString("mode", "LOCAL")!!) }.getOrDefault(UserMode.LOCAL),
        currency = runCatching { Currency.valueOf(prefs.getString("currency", "ETB")!!) }.getOrDefault(Currency.ETB),
        showEthiopianCalendar = prefs.getBoolean("ethcal", true),
        theme = runCatching { ThemeMode.valueOf(prefs.getString("theme", "SYSTEM")!!) }.getOrDefault(ThemeMode.SYSTEM),
        sessionUserId = prefs.getLong("session", -1L).takeIf { it > 0 },
    )

    fun update(block: (AppSettings) -> AppSettings) {
        val s = block(_state.value)
        prefs.edit()
            .putBoolean("onboarding", s.onboardingDone)
            .putString("mode", s.mode.name)
            .putString("currency", s.currency.name)
            .putBoolean("ethcal", s.showEthiopianCalendar)
            .putString("theme", s.theme.name)
            .putLong("session", s.sessionUserId ?: -1L)
            .apply()
        _state.value = s
    }

    /** Applies sensible defaults when the user picks Local or Tourist mode. */
    fun setMode(mode: UserMode) = update {
        when (mode) {
            UserMode.LOCAL -> it.copy(mode = mode, currency = Currency.ETB, showEthiopianCalendar = true)
            UserMode.TOURIST -> it.copy(mode = mode, currency = Currency.USD, showEthiopianCalendar = false)
        }
    }
}
