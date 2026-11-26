package et.hotelbooking.app.domain

import et.hotelbooking.app.data.local.RoomTypeEntity
import et.hotelbooking.app.data.local.SeasonRateEntity
import java.time.LocalDate

data class Quote(
    val nights: Int,
    val nightly: List<Pair<Long, Double>>,   // epochDay -> rate
    val roomTotal: Double,
    val serviceCharge: Double,
    val vat: Double,
    val total: Double,
) {
    val firstNight get() = nightly.firstOrNull()?.second ?: 0.0
}

/**
 * Pricing rules (sample, configurable per deployment):
 *  - base rate = resident rate, or foreigner rate when the hotel uses dual pricing and the guest is a non-resident
 *  - seasonal adjustment in % applied per night
 *  - 10% service charge on the room total, then 15% VAT on (room + service charge)
 */
object Pricing {
    const val SERVICE_CHARGE = 0.10
    const val VAT = 0.15

    fun baseRate(type: RoomTypeEntity, resident: Boolean): Double =
        if (!resident && type.foreignerPriceEtb != null) type.foreignerPriceEtb else type.priceEtb

    fun seasonFor(date: LocalDate, seasons: List<SeasonRateEntity>): SeasonRateEntity? = seasons.firstOrNull { s ->
        val d = date.monthValue * 100 + date.dayOfMonth
        val start = s.startMonth * 100 + s.startDay
        val end = s.endMonth * 100 + s.endDay
        if (start <= end) d in start..end else d >= start || d <= end
    }

    fun nightly(type: RoomTypeEntity, resident: Boolean, epochDay: Long, seasons: List<SeasonRateEntity>): Double {
        val adj = seasonFor(LocalDate.ofEpochDay(epochDay), seasons)?.adjustmentPct ?: 0
        return Math.round(baseRate(type, resident) * (100 + adj) / 100.0).toDouble()
    }

    fun quote(type: RoomTypeEntity, resident: Boolean, from: Long, to: Long, seasons: List<SeasonRateEntity>): Quote {
        val days = (from until maxOf(to, from + 1)).toList()
        val nightly = days.map { it to nightly(type, resident, it, seasons) }
        val room = nightly.sumOf { it.second }
        val service = Math.round(room * SERVICE_CHARGE).toDouble()
        val vat = Math.round((room + service) * VAT).toDouble()
        return Quote(days.size, nightly, room, service, vat, room + service + vat)
    }

    /** Cancellation: free until [freeCancelDays] before check-in, otherwise the first night (incl. taxes) is charged. */
    fun cancellationFee(freeCancelDays: Int, checkIn: Long, today: Long, firstNightGross: Double): Double =
        if (checkIn - today >= freeCancelDays) 0.0 else firstNightGross
}
