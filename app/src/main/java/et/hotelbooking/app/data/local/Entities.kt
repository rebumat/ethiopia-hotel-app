package et.hotelbooking.app.data.local

import androidx.room.*
import et.hotelbooking.app.domain.*

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val passwordHash: String,
    val salt: String,
    val role: Role,
    val hotelId: Long? = null,
    val active: Boolean = true,
    val nationality: String = "",
    // Identity document (stored in app-private storage only; numbers masked in UI)
    val idType: IdType? = null,
    val idNumber: String = "",
    val idExpiry: Long? = null,          // epoch day, passports
    val idFrontPath: String = "",
    val idBackPath: String = "",
    val verification: VerificationStatus = VerificationStatus.NONE,
    val consentAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hotels")
data class HotelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val city: String,             // city key, e.g. "addis_ababa" (localized in UI)
    val region: String,
    val address: String,
    val description: String,
    val stars: Int,
    val latitude: Double,
    val longitude: Double,
    val phone: String,
    val email: String,
    val checkInTime: String = "14:00",
    val checkOutTime: String = "12:00",
    val facilities: String,       // comma separated Facility names
    val images: String,           // comma separated drawable names
    val status: HotelStatus = HotelStatus.APPROVED,
    val freeCancelDays: Int = 2,
    val childrenPolicy: String = "",
    val extraPolicies: String = "",
    val dualPricing: Boolean = false,  // separate resident vs foreigner rates
    val featured: Boolean = false,
    val ratingAvg: Double = 0.0,
    val reviewCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val facilityList: List<Facility> get() = facilities.split(',').filter { it.isNotBlank() }.mapNotNull { runCatching { Facility.valueOf(it.trim()) }.getOrNull() }
    val imageList: List<String> get() = images.split(',').map { it.trim() }.filter { it.isNotBlank() }
}

@Entity(tableName = "room_types", indices = [Index("hotelId")])
data class RoomTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hotelId: Long,
    val name: String,
    val description: String,
    val capacity: Int,
    val bedType: String,
    val sizeSqm: Int,
    val priceEtb: Double,             // resident / standard nightly rate in ETB
    val foreignerPriceEtb: Double? = null, // optional foreigner (non-resident) rate in ETB
    val image: String,
    val active: Boolean = true
)

/** Physical room with a room number (PMS inventory). */
@Entity(tableName = "rooms", indices = [Index("hotelId"), Index("roomTypeId")])
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hotelId: Long,
    val roomTypeId: Long,
    val number: String,
    val floor: Int,
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val note: String = ""
)

@Entity(tableName = "season_rates", indices = [Index("hotelId")])
data class SeasonRateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hotelId: Long,
    val name: String,
    val startMonth: Int, val startDay: Int,
    val endMonth: Int, val endDay: Int,
    val adjustmentPct: Int     // +20 = 20% more, -15 = 15% discount
)

@Entity(tableName = "bookings", indices = [Index("hotelId"), Index("userId"), Index(value = ["reference"], unique = true)])
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reference: String,
    val userId: Long?,
    val hotelId: Long,
    val roomTypeId: Long,
    val roomId: Long? = null,
    val checkIn: Long,           // epoch day
    val checkOut: Long,          // epoch day
    val adults: Int,
    val children: Int = 0,
    val guestName: String,
    val guestPhone: String,
    val guestEmail: String,
    val nationality: String = "",
    val idType: IdType? = null,
    val idNumber: String = "",
    val residentRate: Boolean = true,
    val roomTotalEtb: Double,
    val serviceChargeEtb: Double,
    val vatEtb: Double,
    val totalEtb: Double,
    val status: BookingStatus = BookingStatus.PENDING,
    val paymentMethod: PaymentMethod = PaymentMethod.PAY_AT_HOTEL,
    val paymentStatus: PaymentStatus = PaymentStatus.UNPAID,
    val paidEtb: Double = 0.0,
    val refundEtb: Double = 0.0,
    val depositEtb: Double = 0.0,
    val cardBrand: String = "",
    val cardLast4: String = "",
    val source: BookingSource = BookingSource.APP,
    val specialRequests: String = "",
    val reviewed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val checkedInAt: Long? = null,
    val checkedOutAt: Long? = null,
    val cancelledAt: Long? = null,
    val guestFeedback: String = ""
) {
    val nights: Int get() = (checkOut - checkIn).toInt().coerceAtLeast(1)
}

@Entity(tableName = "folio_charges", indices = [Index("bookingId")])
data class FolioChargeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val category: ChargeCategory,
    val description: String,
    val amountEtb: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments", indices = [Index("bookingId")])
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingId: Long,
    val method: PaymentMethod,
    val type: PaymentType,
    val amountEtb: Double,
    val reference: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews", indices = [Index("hotelId"), Index(value = ["bookingId"], unique = true)])
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hotelId: Long,
    val userId: Long?,
    val bookingId: Long?,
    val authorName: String,
    val rating: Int,
    val title: String,
    val comment: String,
    val verifiedStay: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications", indices = [Index("userId")])
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: NotificationType,
    val arg1: String = "",
    val arg2: String = "",
    val bookingId: Long? = null,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "housekeeping_tasks", indices = [Index("hotelId")])
data class HousekeepingTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hotelId: Long,
    val roomId: Long,
    val type: TaskType,
    val status: TaskStatus = TaskStatus.TODO,
    val assignedTo: Long? = null,
    val note: String = "",
    val highPriority: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

class Converters {
    @TypeConverter fun role(v: Role) = v.name
    @TypeConverter fun toRole(v: String) = Role.valueOf(v)
    @TypeConverter fun hs(v: HotelStatus) = v.name
    @TypeConverter fun toHs(v: String) = HotelStatus.valueOf(v)
    @TypeConverter fun bs(v: BookingStatus) = v.name
    @TypeConverter fun toBs(v: String) = BookingStatus.valueOf(v)
    @TypeConverter fun pm(v: PaymentMethod) = v.name
    @TypeConverter fun toPm(v: String) = PaymentMethod.valueOf(v)
    @TypeConverter fun ps(v: PaymentStatus) = v.name
    @TypeConverter fun toPs(v: String) = PaymentStatus.valueOf(v)
    @TypeConverter fun pt(v: PaymentType) = v.name
    @TypeConverter fun toPt(v: String) = PaymentType.valueOf(v)
    @TypeConverter fun rs(v: RoomStatus) = v.name
    @TypeConverter fun toRs(v: String) = RoomStatus.valueOf(v)
    @TypeConverter fun src(v: BookingSource) = v.name
    @TypeConverter fun toSrc(v: String) = BookingSource.valueOf(v)
    @TypeConverter fun cc(v: ChargeCategory) = v.name
    @TypeConverter fun toCc(v: String) = ChargeCategory.valueOf(v)
    @TypeConverter fun tt(v: TaskType) = v.name
    @TypeConverter fun toTt(v: String) = TaskType.valueOf(v)
    @TypeConverter fun ts(v: TaskStatus) = v.name
    @TypeConverter fun toTs(v: String) = TaskStatus.valueOf(v)
    @TypeConverter fun nt(v: NotificationType) = v.name
    @TypeConverter fun toNt(v: String) = NotificationType.valueOf(v)
    @TypeConverter fun vs(v: VerificationStatus) = v.name
    @TypeConverter fun toVs(v: String) = VerificationStatus.valueOf(v)
    @TypeConverter fun idt(v: IdType?) = v?.name
    @TypeConverter fun toIdt(v: String?) = v?.let { IdType.valueOf(it) }
}
