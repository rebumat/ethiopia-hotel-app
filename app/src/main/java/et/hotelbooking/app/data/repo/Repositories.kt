package et.hotelbooking.app.data.repo

import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.domain.*
import kotlinx.coroutines.flow.Flow

/*
 * Repository contracts. The offline demo uses the Local* implementations (Room).
 * A REST implementation (Django REST Framework backend, see data/remote/ApiContract.kt)
 * can implement the same interfaces and be swapped in AppContainer without touching the UI.
 */

data class SearchCriteria(
    val query: String = "",
    val city: String? = null,
    val region: String? = null,
    val checkIn: Long? = null,
    val checkOut: Long? = null,
    val guests: Int = 2,
    val minPrice: Double = 0.0,
    val maxPrice: Double = Double.MAX_VALUE,
    val stars: Set<Int> = emptySet(),
    val minRating: Double = 0.0,
    val facilities: Set<Facility> = emptySet(),
    val sort: SortOrder = SortOrder.RECOMMENDED,
    val resident: Boolean = true,
)

enum class SortOrder { RECOMMENDED, PRICE_LOW, PRICE_HIGH, RATING, STARS }

data class HotelResult(val hotel: HotelEntity, val fromPriceEtb: Double, val roomsLeft: Int?, val types: List<RoomTypeEntity>)

data class BookingRequest(
    val userId: Long?, val hotelId: Long, val roomTypeId: Long, val checkIn: Long, val checkOut: Long,
    val adults: Int, val children: Int, val guestName: String, val guestPhone: String, val guestEmail: String,
    val nationality: String, val idType: IdType?, val idNumber: String, val residentRate: Boolean,
    val specialRequests: String, val source: BookingSource = BookingSource.APP,
)

data class DemoPayment(val method: PaymentMethod, val amountEtb: Double, val cardBrand: String = "", val cardLast4: String = "", val payerPhone: String = "")

data class Invoice(
    val booking: BookingEntity, val hotel: HotelEntity, val roomType: RoomTypeEntity?, val roomNumber: String?,
    val charges: List<FolioChargeEntity>, val payments: List<PaymentEntity>,
) {
    val extrasTotal get() = charges.sumOf { it.amountEtb }
    val grandTotal get() = booking.totalEtb + extrasTotal
    val paid get() = payments.filter { it.type != PaymentType.REFUND }.sumOf { it.amountEtb } - payments.filter { it.type == PaymentType.REFUND }.sumOf { it.amountEtb }
    val balance get() = grandTotal - paid
}

data class ReportData(
    val periodDays: Int, val roomNightsAvailable: Int, val roomNightsSold: Int, val roomRevenue: Double,
    val extrasRevenue: Double, val revenueByMonth: List<Pair<String, Double>>, val bySource: Map<BookingSource, Int>,
    val byStatus: Map<BookingStatus, Int>, val occupancyByDay: List<Pair<Long, Double>>,
) {
    val occupancy get() = if (roomNightsAvailable == 0) 0.0 else roomNightsSold.toDouble() / roomNightsAvailable
    val adr get() = if (roomNightsSold == 0) 0.0 else roomRevenue / roomNightsSold
    val revpar get() = if (roomNightsAvailable == 0) 0.0 else roomRevenue / roomNightsAvailable
}

data class GuestProfile(val key: String, val name: String, val phone: String, val email: String, val nationality: String, val stays: List<BookingEntity>) {
    val totalSpent get() = stays.filter { it.status == BookingStatus.CHECKED_OUT || it.status == BookingStatus.CHECKED_IN }.sumOf { it.totalEtb }
    val nights get() = stays.filter { it.status == BookingStatus.CHECKED_OUT }.sumOf { it.nights }
}

data class PlatformStats(val users: Int, val clients: Int, val hotelsApproved: Int, val hotelsPending: Int, val hotelsSuspended: Int, val bookings: Int, val activeBookings: Int, val grossEtb: Double)

data class IdDocument(val type: IdType, val number: String, val expiry: Long?, val frontPath: String, val backPath: String, val nationality: String)

interface AuthRepository {
    val currentUser: Flow<UserEntity?>
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun register(name: String, email: String, phone: String, password: String, nationality: String,
                         asHotelManager: Boolean, hotelName: String, hotelCity: String, hotelStars: Int, id: IdDocument?): Result<UserEntity>
    suspend fun updateIdDocument(userId: Long, id: IdDocument): Result<Unit>
    suspend fun updateProfile(userId: Long, name: String, phone: String): Result<Unit>
    fun logout()
}

interface HotelRepository {
    fun approvedHotels(): Flow<List<HotelEntity>>
    fun allHotels(): Flow<List<HotelEntity>>
    fun hotel(id: Long): Flow<HotelEntity?>
    fun roomTypes(hotelId: Long): Flow<List<RoomTypeEntity>>
    fun allRoomTypes(): Flow<List<RoomTypeEntity>>
    fun reviews(hotelId: Long): Flow<List<ReviewEntity>>
    fun seasons(hotelId: Long): Flow<List<SeasonRateEntity>>
    suspend fun search(c: SearchCriteria): List<HotelResult>
    suspend fun hotelsByIds(ids: List<Long>): List<HotelEntity>
    suspend fun roomTypesFor(hotelId: Long): List<RoomTypeEntity>
    suspend fun available(typeId: Long, from: Long, to: Long): Int
    suspend fun reviewableBooking(userId: Long, hotelId: Long): BookingEntity?
    suspend fun addReview(userId: Long, bookingId: Long, rating: Int, title: String, comment: String): Result<Unit>
    suspend fun updateHotel(h: HotelEntity)
    suspend fun setHotelStatus(hotelId: Long, status: HotelStatus)
    suspend fun saveRoomType(r: RoomTypeEntity)
    suspend fun saveSeason(s: SeasonRateEntity)
    suspend fun deleteSeason(s: SeasonRateEntity)
}

interface BookingRepository {
    suspend fun quote(hotelId: Long, typeId: Long, from: Long, to: Long, resident: Boolean): Quote
    suspend fun create(req: BookingRequest, payment: DemoPayment?): Result<BookingEntity>
    fun myBookings(userId: Long): Flow<List<BookingEntity>>
    fun booking(id: Long): Flow<BookingEntity?>
    suspend fun cancellationPreview(bookingId: Long): Pair<Double, Double>  // fee, refund
    suspend fun cancelByGuest(bookingId: Long): Result<Double>
    suspend fun payDemo(bookingId: Long, p: DemoPayment): Result<Unit>
    fun notifications(userId: Long): Flow<List<NotificationEntity>>
    fun unreadCount(userId: Long): Flow<Int>
    suspend fun markAllRead(userId: Long)
}

interface PmsRepository {
    fun hotelBookings(hotelId: Long): Flow<List<BookingEntity>>
    fun rooms(hotelId: Long): Flow<List<RoomEntity>>
    fun tasks(hotelId: Long): Flow<List<HousekeepingTaskEntity>>
    fun staff(hotelId: Long): Flow<List<UserEntity>>
    fun folio(bookingId: Long): Flow<List<FolioChargeEntity>>
    fun payments(bookingId: Long): Flow<List<PaymentEntity>>
    suspend fun findByReference(hotelId: Long, ref: String): BookingEntity?
    suspend fun guestUser(userId: Long?): UserEntity?
    suspend fun confirm(bookingId: Long): Result<Unit>
    suspend fun reject(bookingId: Long): Result<Unit>
    suspend fun markNoShow(bookingId: Long): Result<Unit>
    suspend fun markPaymentVerified(bookingId: Long): Result<Unit>
    suspend fun freeRooms(bookingId: Long): List<RoomEntity>
    suspend fun checkIn(bookingId: Long, roomId: Long, idType: IdType, idNumber: String, nationality: String, deposit: Double, depositMethod: PaymentMethod): Result<Unit>
    suspend fun addCharge(bookingId: Long, category: ChargeCategory, description: String, amount: Double)
    suspend fun removeCharge(c: FolioChargeEntity)
    suspend fun invoice(bookingId: Long): Invoice?
    suspend fun checkOut(bookingId: Long, settleMethod: PaymentMethod, feedback: String): Result<Invoice>
    suspend fun walkIn(req: BookingRequest, roomId: Long, payment: DemoPayment?): Result<BookingEntity>
    suspend fun setRoomStatus(roomId: Long, status: RoomStatus)
    suspend fun addRoom(hotelId: Long, typeId: Long, number: String, floor: Int): Result<Unit>
    suspend fun deleteRoom(room: RoomEntity): Result<Unit>
    suspend fun createTask(hotelId: Long, roomId: Long, type: TaskType, note: String, high: Boolean, assignee: Long?)
    suspend fun setTaskStatus(task: HousekeepingTaskEntity, status: TaskStatus)
    suspend fun addStaff(hotelId: Long, name: String, email: String, phone: String, password: String, role: Role): Result<Unit>
    suspend fun setUserActive(userId: Long, active: Boolean)
    suspend fun report(hotelId: Long, days: Int): ReportData
    suspend fun guests(hotelId: Long): List<GuestProfile>
}

interface AdminRepository {
    fun users(): Flow<List<UserEntity>>
    fun allBookings(): Flow<List<BookingEntity>>
    suspend fun setUserActive(userId: Long, active: Boolean)
    suspend fun setVerification(userId: Long, status: VerificationStatus)
    suspend fun stats(): PlatformStats
}
