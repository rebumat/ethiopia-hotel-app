package et.hotelbooking.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

object Dates {
    fun today(): Long = LocalDate.now().toEpochDay()
    fun of(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)
    fun short(epochDay: Long, locale: Locale = Locale.getDefault()): String =
        of(epochDay).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", safe(locale)))
    fun medium(epochDay: Long, locale: Locale = Locale.getDefault()): String =
        of(epochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(safe(locale)))
    /** Gregorian date, optionally followed by the Ethiopian calendar date. */
    fun display(epochDay: Long, eth: Boolean, locale: Locale = Locale.getDefault()): String {
        val g = short(epochDay, locale)
        return if (eth) g + " · " + EthiopianCalendar.format(of(epochDay), locale.language) else g
    }
    fun dayMonth(epochDay: Long, locale: Locale = Locale.getDefault()): String =
        of(epochDay).format(DateTimeFormatter.ofPattern("d MMM", safe(locale)))
    fun dateTime(millis: Long, locale: Locale = Locale.getDefault()): String =
        java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT, safe(locale)).format(java.util.Date(millis))
    // Afaan Oromo has limited CLDR data on older devices; fall back to English month names.
    private fun safe(l: Locale): Locale = if (l.language == "om") Locale.ENGLISH else l
}
