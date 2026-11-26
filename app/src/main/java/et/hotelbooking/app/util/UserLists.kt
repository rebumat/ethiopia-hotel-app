package et.hotelbooking.app.util

import android.content.Context
import et.hotelbooking.app.data.repo.SearchCriteria
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** One remembered search: city key, dates (epoch days), guests and free-text query. */
data class RecentSearch(
    val city: String? = null,
    val checkIn: Long? = null,
    val checkOut: Long? = null,
    val guests: Int = 2,
    val query: String = "",
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("city", city ?: JSONObject.NULL)
        put("checkIn", checkIn ?: JSONObject.NULL)
        put("checkOut", checkOut ?: JSONObject.NULL)
        put("guests", guests)
        put("query", query)
    }

    companion object {
        fun fromJson(o: JSONObject) = RecentSearch(
            city = o.optString("city", "").takeIf { it.isNotEmpty() },
            checkIn = if (o.isNull("checkIn")) null else o.optLong("checkIn"),
            checkOut = if (o.isNull("checkOut")) null else o.optLong("checkOut"),
            guests = o.optInt("guests", 2).coerceIn(1, 8),
            query = o.optString("query", ""),
        )
    }
}

/**
 * Small on-device lists (SharedPreferences, no server, no Room migration):
 * saved (favourite) hotels, the last 5 searches and the last 8 viewed hotels.
 * Kept separate from [SettingsStore] so adding a list never touches app settings.
 */
class UserListsStore(context: Context) {
    private val prefs = context.getSharedPreferences("ethio_hotels_lists", Context.MODE_PRIVATE)

    private val _savedIds = MutableStateFlow(readIds("saved_hotels"))
    val savedIds: StateFlow<Set<Long>> = _savedIds.asStateFlow()

    private val _recentSearches = MutableStateFlow(readSearches())
    val recentSearches: StateFlow<List<RecentSearch>> = _recentSearches.asStateFlow()

    private val _recentlyViewed = MutableStateFlow(readIdsList("recently_viewed"))
    val recentlyViewed: StateFlow<List<Long>> = _recentlyViewed.asStateFlow()

    private fun readIds(key: String): Set<Long> =
        prefs.getString(key, "")!!.split(',').mapNotNull { it.toLongOrNull() }.toSet()

    private fun readIdsList(key: String): List<Long> =
        prefs.getString(key, "")!!.split(',').mapNotNull { it.toLongOrNull() }

    private fun readSearches(): List<RecentSearch> = runCatching {
        val arr = JSONArray(prefs.getString("recent_searches", "[]"))
        (0 until arr.length()).map { RecentSearch.fromJson(arr.getJSONObject(it)) }
    }.getOrDefault(emptyList())

    fun toggleSaved(hotelId: Long) {
        val next = _savedIds.value.toMutableSet().apply { if (!add(hotelId)) remove(hotelId) }
        prefs.edit().putString("saved_hotels", next.joinToString(",")).apply()
        _savedIds.value = next
    }

    /** Records a search only when it actually narrows something (city, dates or text). */
    fun addRecentSearch(criteria: SearchCriteria) {
        if (criteria.city == null && criteria.checkIn == null && criteria.query.isBlank()) return
        val entry = RecentSearch(criteria.city, criteria.checkIn, criteria.checkOut, criteria.guests, criteria.query)
        val next = (_recentSearches.value.filter { it != entry } + entry).takeLast(5)
        // Newest first for display.
        val ordered = next.reversed()
        prefs.edit().putString("recent_searches", JSONArray(ordered.map { it.toJson() }).toString()).apply()
        _recentSearches.value = ordered
    }

    fun clearRecentSearches() {
        prefs.edit().remove("recent_searches").apply()
        _recentSearches.value = emptyList()
    }

    /** Most recent first, de-duplicated, capped at 8. */
    fun recordViewed(hotelId: Long) {
        val next = (listOf(hotelId) + _recentlyViewed.value.filter { it != hotelId }).take(8)
        prefs.edit().putString("recently_viewed", next.joinToString(",")).apply()
        _recentlyViewed.value = next
    }
}
