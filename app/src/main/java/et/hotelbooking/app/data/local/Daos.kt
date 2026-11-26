package et.hotelbooking.app.data.local

import androidx.room.*
import et.hotelbooking.app.domain.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE lower(email) = lower(:email) LIMIT 1") suspend fun byEmail(email: String): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id") suspend fun byId(id: Long): UserEntity?
    @Query("SELECT * FROM users WHERE id = :id") fun observe(id: Long): Flow<UserEntity?>
    @Query("SELECT * FROM users ORDER BY role, fullName") fun observeAll(): Flow<List<UserEntity>>
    @Query("SELECT * FROM users WHERE hotelId = :hotelId ORDER BY role, fullName") fun observeStaff(hotelId: Long): Flow<List<UserEntity>>
    @Query("SELECT * FROM users WHERE hotelId = :hotelId AND active = 1") suspend fun staffOf(hotelId: Long): List<UserEntity>
    @Query("SELECT * FROM users WHERE role = 'ADMIN' AND active = 1") suspend fun admins(): List<UserEntity>
    @Query("SELECT COUNT(*) FROM users") suspend fun count(): Int
    @Insert suspend fun insert(u: UserEntity): Long
    @Update suspend fun update(u: UserEntity)
}

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels WHERE status = 'APPROVED' ORDER BY featured DESC, ratingAvg DESC") fun observeApproved(): Flow<List<HotelEntity>>
    @Query("SELECT * FROM hotels ORDER BY CASE status WHEN 'PENDING' THEN 0 ELSE 1 END, name") fun observeAll(): Flow<List<HotelEntity>>
    @Query("SELECT * FROM hotels WHERE id = :id") fun observe(id: Long): Flow<HotelEntity?>
    @Query("SELECT * FROM hotels WHERE id = :id") suspend fun byId(id: Long): HotelEntity?
    @Query("SELECT * FROM hotels WHERE id IN (:ids)") suspend fun byIds(ids: List<Long>): List<HotelEntity>
    @Query("SELECT * FROM hotels WHERE status = 'APPROVED'") suspend fun approved(): List<HotelEntity>
    @Insert suspend fun insert(h: HotelEntity): Long
    @Update suspend fun update(h: HotelEntity)
}

@Dao
interface RoomTypeDao {
    @Query("SELECT * FROM room_types WHERE hotelId = :hotelId ORDER BY priceEtb") fun observeForHotel(hotelId: Long): Flow<List<RoomTypeEntity>>
    @Query("SELECT * FROM room_types WHERE hotelId = :hotelId ORDER BY priceEtb") suspend fun forHotel(hotelId: Long): List<RoomTypeEntity>
    @Query("SELECT * FROM room_types") suspend fun all(): List<RoomTypeEntity>
    @Query("SELECT * FROM room_types") fun observeAll(): Flow<List<RoomTypeEntity>>
    @Query("SELECT * FROM room_types WHERE id = :id") suspend fun byId(id: Long): RoomTypeEntity?
    @Insert suspend fun insert(r: RoomTypeEntity): Long
    @Update suspend fun update(r: RoomTypeEntity)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms WHERE hotelId = :hotelId ORDER BY floor, number") fun observeForHotel(hotelId: Long): Flow<List<RoomEntity>>
    @Query("SELECT * FROM rooms WHERE hotelId = :hotelId ORDER BY floor, number") suspend fun forHotel(hotelId: Long): List<RoomEntity>
    @Query("SELECT * FROM rooms WHERE roomTypeId = :typeId ORDER BY number") suspend fun forType(typeId: Long): List<RoomEntity>
    @Query("SELECT * FROM rooms WHERE id = :id") suspend fun byId(id: Long): RoomEntity?
    @Query("SELECT COUNT(*) FROM rooms WHERE roomTypeId = :typeId AND status != 'MAINTENANCE'") suspend fun sellableCount(typeId: Long): Int
    @Query("SELECT * FROM rooms") fun observeAll(): Flow<List<RoomEntity>>
    @Insert suspend fun insert(r: RoomEntity): Long
    @Insert suspend fun insertAll(r: List<RoomEntity>)
    @Update suspend fun update(r: RoomEntity)
    @Delete suspend fun delete(r: RoomEntity)
}

@Dao
interface SeasonDao {
    @Query("SELECT * FROM season_rates WHERE hotelId = :hotelId") fun observeForHotel(hotelId: Long): Flow<List<SeasonRateEntity>>
    @Query("SELECT * FROM season_rates WHERE hotelId = :hotelId") suspend fun forHotel(hotelId: Long): List<SeasonRateEntity>
    @Insert suspend fun insert(s: SeasonRateEntity): Long
    @Update suspend fun update(s: SeasonRateEntity)
    @Delete suspend fun delete(s: SeasonRateEntity)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY checkIn DESC") fun observeForUser(userId: Long): Flow<List<BookingEntity>>
    @Query("SELECT * FROM bookings WHERE hotelId = :hotelId ORDER BY checkIn DESC") fun observeForHotel(hotelId: Long): Flow<List<BookingEntity>>
    @Query("SELECT * FROM bookings WHERE hotelId = :hotelId") suspend fun forHotel(hotelId: Long): List<BookingEntity>
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC") fun observeAll(): Flow<List<BookingEntity>>
    @Query("SELECT * FROM bookings WHERE id = :id") fun observe(id: Long): Flow<BookingEntity?>
    @Query("SELECT * FROM bookings WHERE id = :id") suspend fun byId(id: Long): BookingEntity?
    @Query("SELECT * FROM bookings WHERE upper(reference) = upper(:ref) LIMIT 1") suspend fun byReference(ref: String): BookingEntity?
    @Query("SELECT * FROM bookings WHERE roomTypeId = :typeId AND status IN ('PENDING','CONFIRMED','CHECKED_IN') AND checkIn < :to AND checkOut > :from")
    suspend fun overlappingForType(typeId: Long, from: Long, to: Long): List<BookingEntity>
    @Query("SELECT * FROM bookings WHERE roomId = :roomId AND status IN ('PENDING','CONFIRMED','CHECKED_IN') AND checkIn < :to AND checkOut > :from")
    suspend fun overlappingForRoom(roomId: Long, from: Long, to: Long): List<BookingEntity>
    @Query("SELECT * FROM bookings WHERE userId = :userId AND hotelId = :hotelId AND status = 'CHECKED_OUT' AND reviewed = 0")
    suspend fun reviewable(userId: Long, hotelId: Long): List<BookingEntity>
    @Insert suspend fun insert(b: BookingEntity): Long
    @Insert suspend fun insertAll(b: List<BookingEntity>): List<Long>
    @Update suspend fun update(b: BookingEntity)
}

@Dao
interface FolioDao {
    @Query("SELECT * FROM folio_charges WHERE bookingId = :bookingId ORDER BY createdAt") fun observe(bookingId: Long): Flow<List<FolioChargeEntity>>
    @Query("SELECT * FROM folio_charges WHERE bookingId = :bookingId ORDER BY createdAt") suspend fun forBooking(bookingId: Long): List<FolioChargeEntity>
    @Insert suspend fun insert(c: FolioChargeEntity): Long
    @Delete suspend fun delete(c: FolioChargeEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE bookingId = :bookingId ORDER BY createdAt") fun observe(bookingId: Long): Flow<List<PaymentEntity>>
    @Query("SELECT * FROM payments WHERE bookingId = :bookingId ORDER BY createdAt") suspend fun forBooking(bookingId: Long): List<PaymentEntity>
    @Insert suspend fun insert(p: PaymentEntity): Long
}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE hotelId = :hotelId ORDER BY createdAt DESC") fun observeForHotel(hotelId: Long): Flow<List<ReviewEntity>>
    @Query("SELECT * FROM reviews WHERE hotelId = :hotelId") suspend fun forHotel(hotelId: Long): List<ReviewEntity>
    @Insert suspend fun insert(r: ReviewEntity): Long
    @Insert suspend fun insertAll(r: List<ReviewEntity>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC") fun observe(userId: Long): Flow<List<NotificationEntity>>
    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND read = 0") fun unread(userId: Long): Flow<Int>
    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId") suspend fun markAllRead(userId: Long)
    @Insert suspend fun insert(n: NotificationEntity): Long
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM housekeeping_tasks WHERE hotelId = :hotelId ORDER BY status, highPriority DESC, createdAt DESC") fun observeForHotel(hotelId: Long): Flow<List<HousekeepingTaskEntity>>
    @Query("SELECT * FROM housekeeping_tasks WHERE roomId = :roomId AND status != 'DONE'") suspend fun openForRoom(roomId: Long): List<HousekeepingTaskEntity>
    @Insert suspend fun insert(t: HousekeepingTaskEntity): Long
    @Update suspend fun update(t: HousekeepingTaskEntity)
}

@Database(
    entities = [UserEntity::class, HotelEntity::class, RoomTypeEntity::class, RoomEntity::class, SeasonRateEntity::class,
        BookingEntity::class, FolioChargeEntity::class, PaymentEntity::class, ReviewEntity::class,
        NotificationEntity::class, HousekeepingTaskEntity::class],
    version = 1, exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun users(): UserDao
    abstract fun hotels(): HotelDao
    abstract fun roomTypes(): RoomTypeDao
    abstract fun rooms(): RoomDao
    abstract fun seasons(): SeasonDao
    abstract fun bookings(): BookingDao
    abstract fun folio(): FolioDao
    abstract fun payments(): PaymentDao
    abstract fun reviews(): ReviewDao
    abstract fun notifications(): NotificationDao
    abstract fun tasks(): TaskDao
}
