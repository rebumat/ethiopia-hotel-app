package et.hotelbooking.app.data.repo

import androidx.room.withTransaction
import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.PasswordHasher
import et.hotelbooking.app.util.SettingsStore
import et.hotelbooking.app.data.seed.CityInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.security.SecureRandom
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DomainException(val code: String) : Exception(code)

internal object RefGen {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val rnd = SecureRandom()
    fun booking() = "EHB-" + (1..6).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")
    fun payment(prefix: String) = prefix + "-" + (1..8).map { ALPHABET[rnd.nextInt(ALPHABET.length)] }.joinToString("")
}

/** Creates in-app notifications (a push provider such as FCM would hook in here in production). */
class Notifier(private val db: AppDatabase) {
    suspend fun user(userId: Long?, type: NotificationType, a1: String = "", a2: String = "", bookingId: Long? = null) {
        if (userId == null) return
        db.notifications().insert(NotificationEntity(userId = userId, type = type, arg1 = a1, arg2 = a2, bookingId = bookingId))
    }
    suspend fun hotelStaff(hotelId: Long, type: NotificationType, a1: String = "", a2: String = "", bookingId: Long? = null) {
        db.users().staffOf(hotelId).filter { it.role == Role.MANAGER || it.role == Role.RECEPTIONIST }
            .forEach { user(it.id, type, a1, a2, bookingId) }
    }
}

class LocalAuthRepository(private val db: AppDatabase, private val settings: SettingsStore, private val notifier: Notifier) : AuthRepository {
    @OptIn(ExperimentalCoroutinesApi::class)
    override val currentUser: Flow<UserEntity?> = settings.state.map { it.sessionUserId }.distinctUntilChanged()
        .flatMapLatest { id -> if (id == null) flowOf(null) else db.users().observe(id) }

    override suspend fun login(email: String, password: String): Result<UserEntity> = runCatching {
        val u = db.users().byEmail(email.trim()) ?: throw DomainException("invalid_credentials")
        if (!PasswordHasher.verify(password, u.salt, u.passwordHash)) throw DomainException("invalid_credentials")
        if (!u.active) throw DomainException("account_disabled")
        settings.update { it.copy(sessionUserId = u.id) }
        u
    }

    override suspend fun register(name: String, email: String, phone: String, password: String, nationality: String,
                                  asHotelManager: Boolean, hotelName: String, hotelCity: String, hotelStars: Int, id: IdDocument?): Result<UserEntity> = runCatching {
        if (db.users().byEmail(email.trim()) != null) throw DomainException("email_taken")
        val salt = PasswordHasher.newSalt()
        val newId = db.withTransaction {
            var hotelId: Long? = null
            if (asHotelManager) {
                hotelId = db.hotels().insert(HotelEntity(name = hotelName.trim(), city = hotelCity, region = CityInfo.regionOf(hotelCity),
                    address = "", description = "", stars = hotelStars, latitude = CityInfo.lat(hotelCity), longitude = CityInfo.lng(hotelCity),
                    phone = phone, email = email.trim(), facilities = "WIFI", images = "hotel_building,hotel_lobby1,hotel_room1",
                    status = HotelStatus.PENDING))
            }
            val uid = db.users().insert(UserEntity(fullName = name.trim(), email = email.trim(), phone = phone.trim(),
                passwordHash = PasswordHasher.hash(password, salt), salt = salt,
                role = if (asHotelManager) Role.MANAGER else Role.CLIENT, hotelId = hotelId, nationality = id?.nationality?.ifBlank { nationality } ?: nationality,
                idType = id?.type, idNumber = id?.number?.trim() ?: "", idExpiry = id?.expiry, idFrontPath = id?.frontPath ?: "", idBackPath = id?.backPath ?: "",
                verification = if (id != null) VerificationStatus.PENDING else VerificationStatus.NONE, consentAt = if (id != null) System.currentTimeMillis() else null))
            uid
        }
        notifier.user(newId, NotificationType.WELCOME, name.trim())

        if (asHotelManager) db.users().admins().forEach { notifier.user(it.id, NotificationType.HOTEL_REGISTERED, hotelName.trim(), name.trim()) }
        if (id != null) db.users().admins().forEach { notifier.user(it.id, NotificationType.ID_SUBMITTED, name.trim()) }
        settings.update { it.copy(sessionUserId = newId) }
        db.users().byId(newId)!!
    }

    override suspend fun updateIdDocument(userId: Long, id: IdDocument): Result<Unit> = runCatching {
        val u = db.users().byId(userId) ?: throw DomainException("not_found")
        db.users().update(u.copy(idType = id.type, idNumber = id.number.trim(), idExpiry = id.expiry, idFrontPath = id.frontPath, idBackPath = id.backPath,
            nationality = id.nationality.ifBlank { u.nationality }, verification = VerificationStatus.PENDING, consentAt = System.currentTimeMillis()))
        db.users().admins().forEach { notifier.user(it.id, NotificationType.ID_SUBMITTED, u.fullName) }
    }

    override suspend fun updateProfile(userId: Long, name: String, phone: String): Result<Unit> = runCatching {
        val u = db.users().byId(userId) ?: throw DomainException("not_found")
        if (name.isBlank()) throw DomainException("invalid")
        db.users().update(u.copy(fullName = name.trim(), phone = phone.trim()))
    }

    override fun logout() = settings.update { it.copy(sessionUserId = null) }
}

class LocalHotelRepository(private val db: AppDatabase, private val notifier: Notifier) : HotelRepository {
    override fun approvedHotels() = db.hotels().observeApproved()
    override fun allHotels() = db.hotels().observeAll()
    override fun hotel(id: Long) = db.hotels().observe(id)
    override fun roomTypes(hotelId: Long) = db.roomTypes().observeForHotel(hotelId)
    override fun allRoomTypes() = db.roomTypes().observeAll()
    override fun reviews(hotelId: Long) = db.reviews().observeForHotel(hotelId)
    override fun seasons(hotelId: Long) = db.seasons().observeForHotel(hotelId)
    override suspend fun hotelsByIds(ids: List<Long>) = db.hotels().byIds(ids).sortedBy { ids.indexOf(it.id) }
    override suspend fun roomTypesFor(hotelId: Long) = db.roomTypes().forHotel(hotelId)

    override suspend fun available(typeId: Long, from: Long, to: Long): Int {
        val total = db.rooms().sellableCount(typeId)
        val overlapping = db.bookings().overlappingForType(typeId, from, to)
        // Peak concurrent occupancy across the stay
        var peak = 0
        for (d in from until maxOf(to, from + 1)) peak = maxOf(peak, overlapping.count { it.checkIn <= d && it.checkOut > d })
        return (total - peak).coerceAtLeast(0)
    }

    override suspend fun search(c: SearchCriteria): List<HotelResult> {
        val q = c.query.trim().lowercase()
        val results = db.hotels().approved().filter { h ->
            (c.city == null || h.city == c.city) && (c.region == null || h.region == c.region) &&
            (q.isEmpty() || h.name.lowercase().contains(q) || h.city.replace('_', ' ').contains(q) || CityInfo.matches(h.city, q) || h.address.lowercase().contains(q)) &&
            (c.stars.isEmpty() || h.stars in c.stars) && h.ratingAvg >= c.minRating &&
            h.facilityList.containsAll(c.facilities)
        }.mapNotNull { h ->
            val types = db.roomTypes().forHotel(h.id).filter { it.active && it.capacity >= minOf(c.guests, 4) }
            if (types.isEmpty()) return@mapNotNull null
            var left: Int? = null
            val usable = if (c.checkIn != null && c.checkOut != null) {
                val withAvail = types.map { it to available(it.id, c.checkIn, c.checkOut) }
                left = withAvail.sumOf { it.second }
                withAvail.filter { it.second > 0 }.map { it.first }
            } else types
            if (usable.isEmpty()) return@mapNotNull null
            val from = usable.minOf { Pricing.baseRate(it, c.resident) }
            if (from < c.minPrice || from > c.maxPrice) return@mapNotNull null
            HotelResult(h, from, left, usable)
        }
        return when (c.sort) {
            SortOrder.RECOMMENDED -> results.sortedWith(compareByDescending<HotelResult> { it.hotel.featured }.thenByDescending { it.hotel.ratingAvg })
            SortOrder.PRICE_LOW -> results.sortedBy { it.fromPriceEtb }
            SortOrder.PRICE_HIGH -> results.sortedByDescending { it.fromPriceEtb }
            SortOrder.RATING -> results.sortedByDescending { it.hotel.ratingAvg }
            SortOrder.STARS -> results.sortedByDescending { it.hotel.stars }
        }
    }

    override suspend fun reviewableBooking(userId: Long, hotelId: Long) = db.bookings().reviewable(userId, hotelId).firstOrNull()

    override suspend fun addReview(userId: Long, bookingId: Long, rating: Int, title: String, comment: String): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        // Verified reviews only: the booking must belong to the user and be checked out, one review per stay.
        if (b.userId != userId || b.status != BookingStatus.CHECKED_OUT || b.reviewed) throw DomainException("review_not_allowed")
        val user = db.users().byId(userId)!!
        db.withTransaction {
            db.reviews().insert(ReviewEntity(hotelId = b.hotelId, userId = userId, bookingId = b.id, authorName = user.fullName,
                rating = rating.coerceIn(1, 5), title = title.trim(), comment = comment.trim()))
            db.bookings().update(b.copy(reviewed = true))
            val all = db.reviews().forHotel(b.hotelId)
            val h = db.hotels().byId(b.hotelId)!!
            db.hotels().update(h.copy(ratingAvg = all.map { it.rating }.average(), reviewCount = all.size))
        }
    }

    override suspend fun updateHotel(h: HotelEntity) = db.hotels().update(h)

    override suspend fun setHotelStatus(hotelId: Long, status: HotelStatus) {
        val h = db.hotels().byId(hotelId) ?: return
        db.hotels().update(h.copy(status = status))
        val type = when (status) { HotelStatus.APPROVED -> NotificationType.HOTEL_APPROVED; HotelStatus.SUSPENDED, HotelStatus.REJECTED -> NotificationType.HOTEL_SUSPENDED; else -> null }
        if (type != null) db.users().staffOf(hotelId).filter { it.role == Role.MANAGER }.forEach { notifier.user(it.id, type, h.name, status.name) }
    }

    override suspend fun saveRoomType(r: RoomTypeEntity) { if (r.id == 0L) db.roomTypes().insert(r) else db.roomTypes().update(r) }
    override suspend fun saveSeason(s: SeasonRateEntity) { if (s.id == 0L) db.seasons().insert(s) else db.seasons().update(s) }
    override suspend fun deleteSeason(s: SeasonRateEntity) = db.seasons().delete(s)
}

class LocalBookingRepository(private val db: AppDatabase, private val hotels: HotelRepository, private val notifier: Notifier) : BookingRepository {
    override suspend fun quote(hotelId: Long, typeId: Long, from: Long, to: Long, resident: Boolean): Quote {
        val type = db.roomTypes().byId(typeId)!!
        return Pricing.quote(type, resident, from, to, db.seasons().forHotel(hotelId))
    }

    override suspend fun create(req: BookingRequest, payment: DemoPayment?): Result<BookingEntity> = runCatching {
        if (req.checkOut <= req.checkIn) throw DomainException("invalid_dates")
        if (req.checkIn < Dates.today()) throw DomainException("invalid_dates")
        val hotel = db.hotels().byId(req.hotelId) ?: throw DomainException("not_found")
        if (hotel.status != HotelStatus.APPROVED) throw DomainException("hotel_unavailable")
        val type = db.roomTypes().byId(req.roomTypeId) ?: throw DomainException("not_found")
        if (req.adults + req.children > type.capacity + 1) throw DomainException("capacity")
        val resident = req.residentRate || !hotel.dualPricing
        val id = db.withTransaction {
            if (hotels.available(type.id, req.checkIn, req.checkOut) <= 0) throw DomainException("sold_out")
            val q = Pricing.quote(type, resident, req.checkIn, req.checkOut, db.seasons().forHotel(hotel.id))
            var b = BookingEntity(reference = RefGen.booking(), userId = req.userId, hotelId = hotel.id, roomTypeId = type.id,
                checkIn = req.checkIn, checkOut = req.checkOut, adults = req.adults, children = req.children,
                guestName = req.guestName.trim(), guestPhone = req.guestPhone.trim(), guestEmail = req.guestEmail.trim(),
                nationality = req.nationality, idType = req.idType, idNumber = req.idNumber, residentRate = resident,
                roomTotalEtb = q.roomTotal, serviceChargeEtb = q.serviceCharge, vatEtb = q.vat, totalEtb = q.total,
                specialRequests = req.specialRequests, source = req.source)
            if (payment != null) {
                val pending = payment.method == PaymentMethod.BANK_TRANSFER
                b = b.copy(paymentMethod = payment.method, cardBrand = payment.cardBrand, cardLast4 = payment.cardLast4,
                    paymentStatus = if (pending) PaymentStatus.PENDING_VERIFICATION else PaymentStatus.PAID,
                    paidEtb = if (pending) 0.0 else q.total)
            }
            val bid = db.bookings().insert(b)
            if (payment != null && payment.method != PaymentMethod.BANK_TRANSFER && payment.method != PaymentMethod.PAY_AT_HOTEL)
                db.payments().insert(PaymentEntity(bookingId = bid, method = payment.method, type = PaymentType.PREPAYMENT, amountEtb = q.total, reference = RefGen.payment("DEMO")))
            bid
        }
        val saved = db.bookings().byId(id)!!
        notifier.user(req.userId, NotificationType.BOOKING_SUBMITTED, saved.reference, hotel.name, id)
        notifier.hotelStaff(hotel.id, NotificationType.NEW_RESERVATION, saved.reference, saved.guestName, id)
        saved
    }

    override fun myBookings(userId: Long) = db.bookings().observeForUser(userId)
    override fun booking(id: Long) = db.bookings().observe(id)

    override suspend fun cancellationPreview(bookingId: Long): Pair<Double, Double> {
        val b = db.bookings().byId(bookingId)!!
        val h = db.hotels().byId(b.hotelId)!!
        val firstNightGross = b.totalEtb / b.nights
        val fee = if (b.status == BookingStatus.PENDING) 0.0 else Pricing.cancellationFee(h.freeCancelDays, b.checkIn, Dates.today(), firstNightGross)
        val refund = (b.paidEtb - fee).coerceAtLeast(0.0)
        return fee to refund
    }

    override suspend fun cancelByGuest(bookingId: Long): Result<Double> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.PENDING && b.status != BookingStatus.CONFIRMED) throw DomainException("cannot_cancel")
        val (_, refund) = cancellationPreview(bookingId)
        db.withTransaction {
            db.bookings().update(b.copy(status = BookingStatus.CANCELLED, cancelledAt = System.currentTimeMillis(), refundEtb = refund,
                paymentStatus = when { b.paidEtb <= 0 -> b.paymentStatus; refund >= b.paidEtb -> PaymentStatus.REFUNDED; refund > 0 -> PaymentStatus.PARTIALLY_REFUNDED; else -> b.paymentStatus }))
            if (refund > 0) db.payments().insert(PaymentEntity(bookingId = b.id, method = b.paymentMethod, type = PaymentType.REFUND, amountEtb = refund, reference = RefGen.payment("RFND")))
        }
        val h = db.hotels().byId(b.hotelId)!!
        notifier.user(b.userId, NotificationType.BOOKING_CANCELLED, b.reference, h.name, b.id)
        notifier.hotelStaff(b.hotelId, NotificationType.BOOKING_CANCELLED, b.reference, b.guestName, b.id)
        refund
    }

    override suspend fun payDemo(bookingId: Long, p: DemoPayment): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        val pending = p.method == PaymentMethod.BANK_TRANSFER
        db.bookings().update(b.copy(paymentMethod = p.method, cardBrand = p.cardBrand, cardLast4 = p.cardLast4,
            paymentStatus = if (pending) PaymentStatus.PENDING_VERIFICATION else PaymentStatus.PAID, paidEtb = if (pending) b.paidEtb else b.totalEtb))
        if (!pending) db.payments().insert(PaymentEntity(bookingId = b.id, method = p.method, type = PaymentType.PREPAYMENT, amountEtb = b.totalEtb - b.paidEtb, reference = RefGen.payment("DEMO")))
        notifier.user(b.userId, NotificationType.PAYMENT_RECEIVED, b.reference, "", b.id)
    }

    override fun notifications(userId: Long) = db.notifications().observe(userId)
    override fun unreadCount(userId: Long) = db.notifications().unread(userId)
    override suspend fun markAllRead(userId: Long) = db.notifications().markAllRead(userId)
}

class LocalPmsRepository(private val db: AppDatabase, private val hotels: HotelRepository, private val notifier: Notifier) : PmsRepository {
    override fun hotelBookings(hotelId: Long) = db.bookings().observeForHotel(hotelId)
    override fun rooms(hotelId: Long) = db.rooms().observeForHotel(hotelId)
    override fun tasks(hotelId: Long) = db.tasks().observeForHotel(hotelId)
    override fun staff(hotelId: Long) = db.users().observeStaff(hotelId)
    override fun folio(bookingId: Long) = db.folio().observe(bookingId)
    override fun payments(bookingId: Long) = db.payments().observe(bookingId)

    override suspend fun guestUser(userId: Long?): UserEntity? = userId?.let { db.users().byId(it) }

    override suspend fun findByReference(hotelId: Long, ref: String): BookingEntity? =
        db.bookings().byReference(ref.trim())?.takeIf { it.hotelId == hotelId }

    private suspend fun firstFreeRoom(b: BookingEntity): RoomEntity? = db.rooms().forType(b.roomTypeId)
        .filter { it.status != RoomStatus.MAINTENANCE }
        .firstOrNull { r -> db.bookings().overlappingForRoom(r.id, b.checkIn, b.checkOut).none { it.id != b.id } }

    override suspend fun confirm(bookingId: Long): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.PENDING) throw DomainException("invalid_state")
        val room = b.roomId ?: firstFreeRoom(b)?.id   // tentative room allocation, can be changed at check-in
        db.bookings().update(b.copy(status = BookingStatus.CONFIRMED, roomId = room))
        val h = db.hotels().byId(b.hotelId)!!
        notifier.user(b.userId, NotificationType.BOOKING_CONFIRMED, b.reference, h.name, b.id)
    }

    override suspend fun reject(bookingId: Long): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.PENDING) throw DomainException("invalid_state")
        db.withTransaction {
            db.bookings().update(b.copy(status = BookingStatus.REJECTED, refundEtb = b.paidEtb,
                paymentStatus = if (b.paidEtb > 0) PaymentStatus.REFUNDED else b.paymentStatus))
            if (b.paidEtb > 0) db.payments().insert(PaymentEntity(bookingId = b.id, method = b.paymentMethod, type = PaymentType.REFUND, amountEtb = b.paidEtb, reference = RefGen.payment("RFND")))
        }
        val h = db.hotels().byId(b.hotelId)!!
        notifier.user(b.userId, NotificationType.BOOKING_REJECTED, b.reference, h.name, b.id)
    }

    override suspend fun markNoShow(bookingId: Long): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.CONFIRMED || b.checkIn > Dates.today()) throw DomainException("invalid_state")
        db.bookings().update(b.copy(status = BookingStatus.NO_SHOW))
        notifier.user(b.userId, NotificationType.NO_SHOW, b.reference, db.hotels().byId(b.hotelId)!!.name, b.id)
    }

    override suspend fun markPaymentVerified(bookingId: Long): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.paymentStatus != PaymentStatus.PENDING_VERIFICATION) throw DomainException("invalid_state")
        db.bookings().update(b.copy(paymentStatus = PaymentStatus.PAID, paidEtb = b.totalEtb))
        db.payments().insert(PaymentEntity(bookingId = b.id, method = PaymentMethod.BANK_TRANSFER, type = PaymentType.PREPAYMENT, amountEtb = b.totalEtb, reference = RefGen.payment("BANK")))
        notifier.user(b.userId, NotificationType.PAYMENT_RECEIVED, b.reference, "", b.id)
    }

    override suspend fun freeRooms(bookingId: Long): List<RoomEntity> {
        val b = db.bookings().byId(bookingId) ?: return emptyList()
        return db.rooms().forType(b.roomTypeId).filter { it.status == RoomStatus.AVAILABLE || it.id == b.roomId }
            .filter { r -> db.bookings().overlappingForRoom(r.id, b.checkIn, b.checkOut).none { it.id != b.id } }
    }

    override suspend fun checkIn(bookingId: Long, roomId: Long, idType: IdType, idNumber: String, nationality: String, deposit: Double, depositMethod: PaymentMethod): Result<Unit> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.CONFIRMED) throw DomainException("invalid_state")
        if (b.checkIn > Dates.today()) throw DomainException("too_early")
        if (idNumber.isBlank()) throw DomainException("id_required")
        val room = db.rooms().byId(roomId) ?: throw DomainException("not_found")
        db.withTransaction {
            db.bookings().update(b.copy(status = BookingStatus.CHECKED_IN, roomId = roomId, idType = idType, idNumber = idNumber.trim(),
                nationality = nationality.ifBlank { b.nationality }, depositEtb = deposit, checkedInAt = System.currentTimeMillis()))
            db.rooms().update(room.copy(status = RoomStatus.OCCUPIED))
            if (deposit > 0) db.payments().insert(PaymentEntity(bookingId = b.id, method = depositMethod, type = PaymentType.DEPOSIT, amountEtb = deposit, reference = RefGen.payment("DEP")))
        }
        notifier.user(b.userId, NotificationType.CHECKED_IN, b.reference, room.number, b.id)
    }

    override suspend fun addCharge(bookingId: Long, category: ChargeCategory, description: String, amount: Double) {
        db.folio().insert(FolioChargeEntity(bookingId = bookingId, category = category, description = description.trim(), amountEtb = amount))
    }
    override suspend fun removeCharge(c: FolioChargeEntity) = db.folio().delete(c)

    override suspend fun invoice(bookingId: Long): Invoice? {
        val b = db.bookings().byId(bookingId) ?: return null
        return Invoice(b, db.hotels().byId(b.hotelId)!!, db.roomTypes().byId(b.roomTypeId), b.roomId?.let { db.rooms().byId(it)?.number },
            db.folio().forBooking(b.id), db.payments().forBooking(b.id))
    }

    override suspend fun checkOut(bookingId: Long, settleMethod: PaymentMethod, feedback: String): Result<Invoice> = runCatching {
        val b = db.bookings().byId(bookingId) ?: throw DomainException("not_found")
        if (b.status != BookingStatus.CHECKED_IN) throw DomainException("invalid_state")
        val inv = invoice(bookingId)!!
        db.withTransaction {
            val balance = inv.balance
            if (balance > 0.009) db.payments().insert(PaymentEntity(bookingId = b.id, method = settleMethod, type = PaymentType.SETTLEMENT, amountEtb = balance, reference = RefGen.payment("SET")))
            if (balance < -0.009) db.payments().insert(PaymentEntity(bookingId = b.id, method = settleMethod, type = PaymentType.REFUND, amountEtb = -balance, reference = RefGen.payment("RFND")))
            db.bookings().update(b.copy(status = BookingStatus.CHECKED_OUT, checkedOutAt = System.currentTimeMillis(), paymentStatus = PaymentStatus.PAID,
                paidEtb = inv.grandTotal, guestFeedback = feedback.trim()))
            b.roomId?.let { rid ->
                db.rooms().byId(rid)?.let { db.rooms().update(it.copy(status = RoomStatus.CLEANING)) }
                db.tasks().insert(HousekeepingTaskEntity(hotelId = b.hotelId, roomId = rid, type = TaskType.CLEANING, note = "Check-out " + b.reference, highPriority = true))
            }
        }
        notifier.user(b.userId, NotificationType.CHECKED_OUT, b.reference, inv.hotel.name, b.id)
        notifier.user(b.userId, NotificationType.REVIEW_REMINDER, b.reference, inv.hotel.name, b.id)
        invoice(bookingId)!!
    }

    override suspend fun walkIn(req: BookingRequest, roomId: Long, payment: DemoPayment?): Result<BookingEntity> = runCatching {
        val type = db.roomTypes().byId(req.roomTypeId) ?: throw DomainException("not_found")
        val hotel = db.hotels().byId(req.hotelId)!!
        val room = db.rooms().byId(roomId) ?: throw DomainException("not_found")
        if (db.bookings().overlappingForRoom(roomId, req.checkIn, req.checkOut).isNotEmpty() || room.status != RoomStatus.AVAILABLE) throw DomainException("room_unavailable")
        val resident = req.residentRate || !hotel.dualPricing
        val q = Pricing.quote(type, resident, req.checkIn, req.checkOut, db.seasons().forHotel(hotel.id))
        val id = db.withTransaction {
            val paid = payment?.amountEtb ?: 0.0
            val bid = db.bookings().insert(BookingEntity(reference = RefGen.booking(), userId = null, hotelId = hotel.id, roomTypeId = type.id, roomId = roomId,
                checkIn = req.checkIn, checkOut = req.checkOut, adults = req.adults, children = req.children, guestName = req.guestName.trim(),
                guestPhone = req.guestPhone.trim(), guestEmail = req.guestEmail.trim(), nationality = req.nationality, idType = req.idType, idNumber = req.idNumber,
                residentRate = resident, roomTotalEtb = q.roomTotal, serviceChargeEtb = q.serviceCharge, vatEtb = q.vat, totalEtb = q.total,
                status = BookingStatus.CHECKED_IN, source = BookingSource.WALK_IN, checkedInAt = System.currentTimeMillis(),
                paymentMethod = payment?.method ?: PaymentMethod.CASH,
                paymentStatus = when { paid <= 0 -> PaymentStatus.UNPAID; paid >= q.total -> PaymentStatus.PAID; else -> PaymentStatus.PARTIALLY_PAID },
                paidEtb = paid, cardBrand = payment?.cardBrand ?: "", cardLast4 = payment?.cardLast4 ?: ""))
            if (payment != null && paid > 0) db.payments().insert(PaymentEntity(bookingId = bid, method = payment.method, type = PaymentType.PREPAYMENT, amountEtb = paid, reference = RefGen.payment("WALK")))
            db.rooms().update(room.copy(status = RoomStatus.OCCUPIED))
            bid
        }
        db.bookings().byId(id)!!
    }

    override suspend fun setRoomStatus(roomId: Long, status: RoomStatus) {
        val r = db.rooms().byId(roomId) ?: return
        db.rooms().update(r.copy(status = status))
        if (status == RoomStatus.AVAILABLE) db.tasks().openForRoom(roomId).filter { it.type == TaskType.CLEANING }
            .forEach { db.tasks().update(it.copy(status = TaskStatus.DONE, completedAt = System.currentTimeMillis())) }
    }

    override suspend fun addRoom(hotelId: Long, typeId: Long, number: String, floor: Int): Result<Unit> = runCatching {
        if (number.isBlank()) throw DomainException("invalid")
        if (db.rooms().forHotel(hotelId).any { it.number.equals(number.trim(), true) }) throw DomainException("room_exists")
        db.rooms().insert(RoomEntity(hotelId = hotelId, roomTypeId = typeId, number = number.trim(), floor = floor))
    }

    override suspend fun deleteRoom(room: RoomEntity): Result<Unit> = runCatching {
        if (db.bookings().overlappingForRoom(room.id, Dates.today(), Dates.today() + 3650).isNotEmpty()) throw DomainException("room_in_use")
        db.rooms().delete(room)
    }

    override suspend fun createTask(hotelId: Long, roomId: Long, type: TaskType, note: String, high: Boolean, assignee: Long?) {
        db.tasks().insert(HousekeepingTaskEntity(hotelId = hotelId, roomId = roomId, type = type, note = note.trim(), highPriority = high, assignedTo = assignee))
        if (type == TaskType.MAINTENANCE) setRoomStatus(roomId, RoomStatus.MAINTENANCE)
    }

    override suspend fun setTaskStatus(task: HousekeepingTaskEntity, status: TaskStatus) {
        db.tasks().update(task.copy(status = status, completedAt = if (status == TaskStatus.DONE) System.currentTimeMillis() else null))
        val room = db.rooms().byId(task.roomId) ?: return
        if (status == TaskStatus.IN_PROGRESS && task.type == TaskType.CLEANING && room.status != RoomStatus.OCCUPIED) db.rooms().update(room.copy(status = RoomStatus.CLEANING))
        if (status == TaskStatus.DONE && (room.status == RoomStatus.CLEANING || room.status == RoomStatus.MAINTENANCE)) db.rooms().update(room.copy(status = RoomStatus.AVAILABLE))
    }

    override suspend fun addStaff(hotelId: Long, name: String, email: String, phone: String, password: String, role: Role): Result<Unit> = runCatching {
        if (!role.isStaff) throw DomainException("invalid")
        if (db.users().byEmail(email.trim()) != null) throw DomainException("email_taken")
        val salt = PasswordHasher.newSalt()
        db.users().insert(UserEntity(fullName = name.trim(), email = email.trim(), phone = phone.trim(), passwordHash = PasswordHasher.hash(password, salt), salt = salt, role = role, hotelId = hotelId))
    }

    override suspend fun setUserActive(userId: Long, active: Boolean) { db.users().byId(userId)?.let { db.users().update(it.copy(active = active)) } }

    override suspend fun report(hotelId: Long, days: Int): ReportData {
        val today = Dates.today()
        val from = today - days
        val rooms = db.rooms().forHotel(hotelId).size.coerceAtLeast(1)
        val bookings = db.bookings().forHotel(hotelId)
        val stayed = bookings.filter { it.status == BookingStatus.CHECKED_OUT || it.status == BookingStatus.CHECKED_IN }
        var sold = 0; var revenue = 0.0
        val occByDay = mutableListOf<Pair<Long, Double>>()
        for (d in from until today) {
            val inHouse = stayed.filter { it.checkIn <= d && it.checkOut > d }
            sold += inHouse.size
            revenue += inHouse.sumOf { it.roomTotalEtb / it.nights }
            occByDay += d to inHouse.size.toDouble() / rooms
        }
        val inPeriod = bookings.filter { it.checkIn in from..today }
        var extras = 0.0
        stayed.filter { it.checkIn in from..today }.forEach { b -> extras += db.folio().forBooking(b.id).sumOf { it.amountEtb } }
        val fmt = DateTimeFormatter.ofPattern("MMM")
        val months = (5 downTo 0).map { LocalDate.ofEpochDay(today).minusMonths(it.toLong()).withDayOfMonth(1) }
        val byMonth = months.map { m ->
            val start = m.toEpochDay(); val end = m.plusMonths(1).toEpochDay()
            m.format(fmt) to stayed.filter { it.checkIn in start until end }.sumOf { it.totalEtb }
        }
        return ReportData(days, rooms * days, sold, revenue, extras, byMonth,
            BookingSource.entries.associateWith { s -> inPeriod.count { it.source == s } },
            BookingStatus.entries.associateWith { s -> inPeriod.count { it.status == s } }, occByDay)
    }

    override suspend fun guests(hotelId: Long): List<GuestProfile> = db.bookings().forHotel(hotelId)
        .groupBy { (it.guestEmail.ifBlank { it.guestPhone.ifBlank { it.guestName } }).lowercase() }
        .map { (k, list) -> val l = list.sortedByDescending { it.checkIn }; val f = l.first()
            GuestProfile(k, f.guestName, f.guestPhone, f.guestEmail, l.firstOrNull { it.nationality.isNotBlank() }?.nationality ?: "", l) }
        .sortedByDescending { it.stays.size }
}

class LocalAdminRepository(private val db: AppDatabase, private val notifier: Notifier) : AdminRepository {
    override suspend fun setVerification(userId: Long, status: VerificationStatus) {
        val u = db.users().byId(userId) ?: return
        db.users().update(u.copy(verification = status))
        when (status) {
            VerificationStatus.VERIFIED -> notifier.user(userId, NotificationType.ID_VERIFIED, u.fullName)
            VerificationStatus.REJECTED -> notifier.user(userId, NotificationType.ID_REJECTED, u.fullName)
            else -> {}
        }
    }
    override fun users() = db.users().observeAll()
    override fun allBookings() = db.bookings().observeAll()
    override suspend fun setUserActive(userId: Long, active: Boolean) { db.users().byId(userId)?.let { db.users().update(it.copy(active = active)) } }
    override suspend fun stats(): PlatformStats {
        val users = db.users().observeAll().first()
        val hotels = db.hotels().observeAll().first()
        val bookings = db.bookings().observeAll().first()
        return PlatformStats(users.size, users.count { it.role == Role.CLIENT }, hotels.count { it.status == HotelStatus.APPROVED },
            hotels.count { it.status == HotelStatus.PENDING }, hotels.count { it.status == HotelStatus.SUSPENDED },
            bookings.size, bookings.count { it.status.isActive }, bookings.filter { it.status == BookingStatus.CHECKED_OUT }.sumOf { it.totalEtb })
    }
}
