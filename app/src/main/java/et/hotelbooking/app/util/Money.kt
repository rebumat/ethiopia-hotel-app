package et.hotelbooking.app.util

import java.text.NumberFormat
import java.util.Locale

/**
 * SAMPLE exchange rates used only for display conversion in this demo build.
 * Source: National Bank of Ethiopia indicative weighted-average rates published for 13 Nov 2026
 * (USD 161.19 ETB, EUR 179.61 ETB). A production build must fetch live rates from the backend.
 */
object Rates {
    const val ETB_PER_USD = 161.19
    const val ETB_PER_EUR = 179.61
    const val AS_OF = "2026-11-13"
}

object Money {
    private fun fmt(v: Double, decimals: Int): String {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        nf.maximumFractionDigits = decimals; nf.minimumFractionDigits = decimals
        return nf.format(v)
    }
    fun etb(v: Double) = "ETB " + fmt(v, 0)
    fun convert(etb: Double, c: Currency) = when (c) {
        Currency.ETB -> etb
        Currency.USD -> etb / Rates.ETB_PER_USD
        Currency.EUR -> etb / Rates.ETB_PER_EUR
    }
    fun inCurrency(etb: Double, c: Currency): String = when (c) {
        Currency.ETB -> etb(etb)
        Currency.USD -> "US$ " + fmt(convert(etb, c), 0)
        Currency.EUR -> "€ " + fmt(convert(etb, c), 0)
    }
    /** Primary string in the user's display currency; secondary always shows the other side (ETB or USD). */
    fun primary(etb: Double, c: Currency) = inCurrency(etb, c)
    fun secondary(etb: Double, c: Currency): String = if (c == Currency.ETB) "≈ " + inCurrency(etb, Currency.USD) else "≈ " + etb(etb)
    fun exact(v: Double) = "ETB " + fmt(v, 2)
}
