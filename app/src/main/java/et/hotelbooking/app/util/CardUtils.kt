package et.hotelbooking.app.util

/** Demo-only card helpers. The full card number and CVV are never stored or logged. */
object CardUtils {
    enum class Brand { VISA, MASTERCARD, UNKNOWN }

    fun digits(s: String) = s.filter { it.isDigit() }

    fun detectBrand(number: String): Brand {
        val d = digits(number)
        if (d.startsWith("4")) return Brand.VISA
        if (d.length >= 2) {
            val two = d.take(2).toInt()
            if (two in 51..55) return Brand.MASTERCARD
        }
        if (d.length >= 4) {
            val four = d.take(4).toInt()
            if (four in 2221..2720) return Brand.MASTERCARD
        }
        return Brand.UNKNOWN
    }

    fun luhnValid(number: String): Boolean {
        val d = digits(number)
        if (d.length !in 13..19) return false
        var sum = 0; var alt = false
        for (i in d.length - 1 downTo 0) {
            var n = d[i] - '0'
            if (alt) { n *= 2; if (n > 9) n -= 9 }
            sum += n; alt = !alt
        }
        return sum % 10 == 0
    }

    fun formatGroups(number: String): String = digits(number).take(19).chunked(4).joinToString(" ")

    fun expiryValid(mmYY: String, nowYear: Int, nowMonth: Int): Boolean {
        val d = digits(mmYY); if (d.length != 4) return false
        val m = d.take(2).toInt(); val y = 2000 + d.takeLast(2).toInt()
        if (m !in 1..12) return false
        return y > nowYear || (y == nowYear && m >= nowMonth)
    }

    fun last4(number: String) = digits(number).takeLast(4)
}
