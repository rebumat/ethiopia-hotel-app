package et.hotelbooking.app.util

import java.time.LocalDate

/** Gregorian <-> Ethiopian (Amete Mihret) calendar conversion using Julian Day Numbers. */
object EthiopianCalendar {
    private const val JD_OFFSET = 1723856L
    data class EthDate(val year: Int, val month: Int, val day: Int)

    fun fromGregorian(date: LocalDate): EthDate {
        val jdn = date.toEpochDay() + 2440588L
        val r = Math.floorMod(jdn - JD_OFFSET, 1461L)
        val n = r % 365 + 365 * (r / 1460)
        val year = 4 * Math.floorDiv(jdn - JD_OFFSET, 1461L) + r / 365 - r / 1460
        return EthDate(year.toInt(), (n / 30 + 1).toInt(), (n % 30 + 1).toInt())
    }

    private val monthsEn = listOf("Meskerem", "Tikimt", "Hidar", "Tahsas", "Tir", "Yekatit", "Megabit", "Miyazya", "Ginbot", "Sene", "Hamle", "Nehase", "Pagume")
    private val monthsAm = listOf("መስከረም", "ጥቅምት", "ኅዳር", "ታኅሣሥ", "ጥር", "የካቲት", "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰኔ", "ሐምሌ", "ነሐሴ", "ጳጉሜን")
    private val monthsTi = listOf("መስከረም", "ጥቅምቲ", "ሕዳር", "ታሕሳስ", "ጥሪ", "ለካቲት", "መጋቢት", "ሚያዝያ", "ግንቦት", "ሰነ", "ሓምለ", "ነሓሰ", "ጳጉሜን")
    private val monthsOm = listOf("Fulbaana", "Onkoloolessa", "Sadaasa", "Muddee", "Amajjii", "Guraandhala", "Bitootessa", "Eebila", "Caamsaa", "Waxabajjii", "Adoolessa", "Hagayya", "Qaammee")

    fun format(date: LocalDate, lang: String): String {
        val e = fromGregorian(date)
        val m = when (lang) { "am" -> monthsAm; "ti" -> monthsTi; "om" -> monthsOm; else -> monthsEn }[e.month - 1]
        val suffix = when (lang) { "am" -> "ዓ.ም."; "ti" -> "ዓ.ም."; "om" -> "A.L.I."; else -> "EC" }
        return "$m ${e.day}, ${e.year} $suffix"
    }
}
