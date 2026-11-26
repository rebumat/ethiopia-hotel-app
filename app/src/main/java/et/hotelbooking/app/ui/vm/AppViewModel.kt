package et.hotelbooking.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.hotelbooking.app.AppContainer
import et.hotelbooking.app.data.local.UserEntity
import et.hotelbooking.app.data.repo.IdDocument
import et.hotelbooking.app.data.repo.SearchCriteria
import et.hotelbooking.app.domain.UserMode
import et.hotelbooking.app.util.AppSettings
import et.hotelbooking.app.util.Currency
import et.hotelbooking.app.util.RecentSearch
import et.hotelbooking.app.util.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Activity-scoped state: session, settings, compare list, and the shared search criteria. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(val c: AppContainer) : ViewModel() {
    val settings: StateFlow<AppSettings> = c.settings.state
    val ready: StateFlow<Boolean> = c.ready
    val user: StateFlow<UserEntity?> = c.auth.currentUser.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val unread: StateFlow<Int> = user.flatMapLatest { u -> if (u == null) flowOf(0) else c.bookings.unreadCount(u.id) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _compare = MutableStateFlow<List<Long>>(emptyList())
    val compare: StateFlow<List<Long>> = _compare
    /** Returns false when the compare list is already full (max 3). */
    fun toggleCompare(id: Long): Boolean {
        val l = _compare.value
        if (id in l) { _compare.value = l - id; return true }
        if (l.size >= 3) return false
        _compare.value = l + id; return true
    }
    fun clearCompare() { _compare.value = emptyList() }

    val criteria = MutableStateFlow(SearchCriteria())

    // On-device lists (saved hotels, recent searches, recently viewed) — see util/UserLists.kt.
    val savedIds: StateFlow<Set<Long>> = c.lists.savedIds
    val recentSearches: StateFlow<List<RecentSearch>> = c.lists.recentSearches
    val recentlyViewed: StateFlow<List<Long>> = c.lists.recentlyViewed
    fun toggleSaved(hotelId: Long) = c.lists.toggleSaved(hotelId)
    fun recordRecentSearch() = c.lists.addRecentSearch(criteria.value)
    fun clearRecentSearches() = c.lists.clearRecentSearches()
    fun applyRecentSearch(s: RecentSearch) {
        criteria.value = SearchCriteria(city = s.city, checkIn = s.checkIn, checkOut = s.checkOut, guests = s.guests, query = s.query)
        recordRecentSearch()
    }
    fun recordViewed(hotelId: Long) = c.lists.recordViewed(hotelId)

    fun setMode(m: UserMode) = c.settings.setMode(m)
    fun setCurrency(cur: Currency) = c.settings.update { it.copy(currency = cur) }
    fun setTheme(t: ThemeMode) = c.settings.update { it.copy(theme = t) }
    fun setEthCalendar(b: Boolean) = c.settings.update { it.copy(showEthiopianCalendar = b) }
    fun finishOnboarding() = c.settings.update { it.copy(onboardingDone = true) }

    suspend fun login(email: String, pw: String) = c.auth.login(email, pw)
    suspend fun register(name: String, email: String, phone: String, pw: String, nationality: String, asManager: Boolean,
                         hotelName: String, hotelCity: String, stars: Int, id: IdDocument?) =
        c.auth.register(name, email, phone, pw, nationality, asManager, hotelName, hotelCity, stars, id)
    fun logout() { c.auth.logout(); clearCompare() }
    fun updateId(doc: IdDocument, done: (Boolean) -> Unit) = viewModelScope.launch {
        val u = user.value ?: return@launch
        done(c.auth.updateIdDocument(u.id, doc).isSuccess)
    }
    fun updateProfile(name: String, phone: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val u = user.value ?: return@launch
        done(c.auth.updateProfile(u.id, name, phone).isSuccess)
    }
}
