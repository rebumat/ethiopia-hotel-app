package et.hotelbooking.app.data.seed

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import et.hotelbooking.app.R

data class City(val key: String, @StringRes val name: Int, val region: String, @DrawableRes val image: Int, val lat: Double, val lng: Double)

object CityInfo {
    val cities = listOf(
        City("addis_ababa", R.string.city_addis_ababa, "addis_ababa", R.drawable.city_addis, 9.0108, 38.7613),
        City("bahir_dar", R.string.city_bahir_dar, "amhara", R.drawable.city_bahirdar, 11.5936, 37.3908),
        City("gondar", R.string.city_gondar, "amhara", R.drawable.city_gondar, 12.6075, 37.4667),
        City("lalibela", R.string.city_lalibela, "amhara", R.drawable.city_lalibela, 12.0317, 39.0476),
        City("hawassa", R.string.city_hawassa, "sidama", R.drawable.city_hawassa, 7.0504, 38.4955),
        City("bishoftu", R.string.city_bishoftu, "oromia", R.drawable.city_bishoftu, 8.7525, 38.9783),
        City("adama", R.string.city_adama, "oromia", R.drawable.city_adama, 8.5400, 39.2700),
        City("axum", R.string.city_axum, "tigray", R.drawable.city_axum, 14.1211, 38.7231),
        City("mekelle", R.string.city_mekelle, "tigray", R.drawable.city_mekelle, 13.4967, 39.4753),
        City("harar", R.string.city_harar, "harari", R.drawable.city_harar, 9.3126, 42.1227),
        City("dire_dawa", R.string.city_dire_dawa, "dire_dawa", R.drawable.city_diredawa, 9.5931, 41.8661),
        City("arba_minch", R.string.city_arba_minch, "south_ethiopia", R.drawable.city_arbaminch, 6.0333, 37.5500),
        City("jimma", R.string.city_jimma, "oromia", R.drawable.hotel_resort2, 7.6667, 36.8333),
    )
    private val byKey = cities.associateBy { it.key }
    fun get(key: String) = byKey[key]
    fun nameRes(key: String) = byKey[key]?.name ?: R.string.city_addis_ababa
    fun regionOf(key: String) = byKey[key]?.region ?: "addis_ababa"
    fun lat(key: String) = byKey[key]?.lat ?: 9.0108
    fun lng(key: String) = byKey[key]?.lng ?: 38.7613
    private val aliases = mapOf("addis_ababa" to listOf("addis", "finfinne", "አዲስ"), "axum" to listOf("aksum", "አክሱም"), "bahir_dar" to listOf("bahirdar", "ባሕር ዳር", "ባህር ዳር"),
        "gondar" to listOf("gonder", "ጎንደር"), "lalibela" to listOf("ላሊበላ"), "hawassa" to listOf("awasa", "awassa", "ሀዋሳ"), "adama" to listOf("nazret", "nazareth", "አዳማ"),
        "bishoftu" to listOf("debre zeyit", "ቢሾፍቱ"), "harar" to listOf("hararge", "ሐረር"), "dire_dawa" to listOf("ድሬ ዳዋ"), "mekelle" to listOf("mekele", "መቐለ", "መቀሌ"), "arba_minch" to listOf("arbaminch", "አርባ ምንጭ"), "jimma" to listOf("ጅማ"))
    fun matches(key: String, q: String) = aliases[key]?.any { it.contains(q) || q.contains(it) } == true

    val regions = listOf("addis_ababa", "amhara", "oromia", "sidama", "tigray", "harari", "dire_dawa", "south_ethiopia")
    @StringRes fun regionName(key: String): Int = when (key) {
        "addis_ababa" -> R.string.region_addis_ababa; "amhara" -> R.string.region_amhara; "oromia" -> R.string.region_oromia
        "sidama" -> R.string.region_sidama; "tigray" -> R.string.region_tigray; "harari" -> R.string.region_harari
        "dire_dawa" -> R.string.region_dire_dawa; else -> R.string.region_south_ethiopia
    }
    val popular = listOf("addis_ababa", "bahir_dar", "lalibela", "gondar", "hawassa", "bishoftu", "axum", "harar", "arba_minch", "dire_dawa")
}

data class Landmark(val key: String, @StringRes val name: Int, @StringRes val desc: Int, @DrawableRes val image: Int, val nearCity: String, val unesco: Boolean, val lat: Double, val lng: Double)

/** Ethiopian landmarks shown in the "Discover Ethiopia" section of the home screen. */
object Landmarks {
    val all = listOf(
        Landmark("lalibela", R.string.lm_lalibela, R.string.lm_lalibela_desc, R.drawable.lm_lalibela, "lalibela", true, 12.0317, 39.0411),
        Landmark("axum", R.string.lm_axum, R.string.lm_axum_desc, R.drawable.lm_axum, "axum", true, 14.1300, 38.7190),
        Landmark("gondar", R.string.lm_gondar, R.string.lm_gondar_desc, R.drawable.lm_gondar, "gondar", true, 12.6080, 37.4690),
        Landmark("simien", R.string.lm_simien, R.string.lm_simien_desc, R.drawable.lm_simien, "gondar", true, 13.1833, 38.0667),
        Landmark("blue_nile", R.string.lm_blue_nile, R.string.lm_blue_nile_desc, R.drawable.lm_blue_nile, "bahir_dar", false, 11.4880, 37.5870),
        Landmark("lake_tana", R.string.lm_lake_tana, R.string.lm_lake_tana_desc, R.drawable.lm_lake_tana, "bahir_dar", true, 11.6000, 37.3833),
        Landmark("danakil", R.string.lm_danakil, R.string.lm_danakil_desc, R.drawable.lm_danakil, "mekelle", false, 14.2417, 40.3000),
        Landmark("erta_ale", R.string.lm_erta_ale, R.string.lm_erta_ale_desc, R.drawable.lm_erta_ale, "mekelle", false, 13.6000, 40.6700),
        Landmark("harar", R.string.lm_harar, R.string.lm_harar_desc, R.drawable.lm_harar, "harar", true, 9.3100, 42.1250),
        Landmark("bale", R.string.lm_bale, R.string.lm_bale_desc, R.drawable.lm_bale, "bishoftu", true, 6.8000, 39.8000),
        Landmark("addis", R.string.lm_addis, R.string.lm_addis_desc, R.drawable.lm_addis, "addis_ababa", false, 9.0300, 38.7400),
        Landmark("tiya", R.string.lm_tiya, R.string.lm_tiya_desc, R.drawable.lm_tiya, "addis_ababa", true, 8.4350, 38.6110),
        Landmark("konso", R.string.lm_konso, R.string.lm_konso_desc, R.drawable.lm_konso, "arba_minch", true, 5.3333, 37.4333),
    )
}
