package et.hotelbooking.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.hotelbooking.app.AppContainer
import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.data.repo.*
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.util.Dates
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FrontDesk(val arrivals: List<BookingEntity>, val departures: List<BookingEntity>, val inHouse: List<BookingEntity>, val pending: List<BookingEntity>, val overdue: List<BookingEntity>)

/** Hotel management (PMS) state for one hotel. */
class PmsViewModel(val c: AppContainer, val hotelId: Long) : ViewModel() {
    val hotel = c.hotels.hotel(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val bookings = c.pms.hotelBookings(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val rooms = c.pms.rooms(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val types = c.hotels.roomTypes(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val tasks = c.pms.tasks(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val staff = c.pms.staff(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val seasons = c.hotels.seasons(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val frontDesk: StateFlow<FrontDesk?> = bookings.map { l ->
        if (l == null) return@map null
        val t = Dates.today()
        FrontDesk(
            arrivals = l.filter { it.checkIn == t && it.status == BookingStatus.CONFIRMED },
            departures = l.filter { it.checkOut == t && it.status == BookingStatus.CHECKED_IN },
            inHouse = l.filter { it.status == BookingStatus.CHECKED_IN },
            pending = l.filter { it.status == BookingStatus.PENDING }.sortedBy { it.checkIn },
            overdue = l.filter { it.status == BookingStatus.CONFIRMED && it.checkIn < t },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** Room status for display: stored status, or RESERVED when a confirmed guest is due today. */
    val roomBoard: StateFlow<List<Pair<RoomEntity, RoomStatus>>> = combine(rooms, bookings) { r, b ->
        val t = Dates.today()
        val due = b.orEmpty().filter { it.status == BookingStatus.CONFIRMED && it.checkIn == t }.mapNotNull { it.roomId }.toSet()
        r.map { it to if (it.status == RoomStatus.AVAILABLE && it.id in due) RoomStatus.RESERVED else it.status }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _msg = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _msg
    private fun <T> Result<T>.report(ok: String = "ok") { _msg.tryEmit(if (isSuccess) ok else "err:" + (exceptionOrNull()?.message ?: "error")) }

    fun confirm(id: Long) = viewModelScope.launch { c.pms.confirm(id).report("confirmed") }
    fun reject(id: Long) = viewModelScope.launch { c.pms.reject(id).report("rejected") }
    fun noShow(id: Long) = viewModelScope.launch { c.pms.markNoShow(id).report("no_show") }
    fun verifyPayment(id: Long) = viewModelScope.launch { c.pms.markPaymentVerified(id).report("payment_verified") }
    suspend fun findByRef(ref: String) = c.pms.findByReference(hotelId, ref)
    suspend fun freeRooms(id: Long) = c.pms.freeRooms(id)
    suspend fun guestUser(id: Long?) = c.pms.guestUser(id)
    suspend fun booking(id: Long) = bookings.value?.firstOrNull { it.id == id } ?: c.db.bookings().byId(id)
    suspend fun checkIn(id: Long, roomId: Long, idType: IdType, idNo: String, nat: String, deposit: Double, m: PaymentMethod) = c.pms.checkIn(id, roomId, idType, idNo, nat, deposit, m)
    fun addCharge(id: Long, cat: ChargeCategory, desc: String, amt: Double) = viewModelScope.launch { c.pms.addCharge(id, cat, desc, amt) }
    fun removeCharge(ch: FolioChargeEntity) = viewModelScope.launch { c.pms.removeCharge(ch) }
    fun folio(id: Long) = c.pms.folio(id)
    fun payments(id: Long) = c.pms.payments(id)
    suspend fun invoice(id: Long) = c.pms.invoice(id)
    suspend fun checkOut(id: Long, m: PaymentMethod, feedback: String) = c.pms.checkOut(id, m, feedback)
    suspend fun walkIn(req: BookingRequest, roomId: Long, p: DemoPayment?) = c.pms.walkIn(req, roomId, p)
    suspend fun quote(typeId: Long, from: Long, to: Long, resident: Boolean) = c.bookings.quote(hotelId, typeId, from, to, resident)
    fun setRoomStatus(roomId: Long, s: RoomStatus) = viewModelScope.launch { c.pms.setRoomStatus(roomId, s) }
    fun addRoom(typeId: Long, number: String, floor: Int) = viewModelScope.launch { c.pms.addRoom(hotelId, typeId, number, floor).report("room_added") }
    fun deleteRoom(r: RoomEntity) = viewModelScope.launch { c.pms.deleteRoom(r).report("room_deleted") }
    fun createTask(roomId: Long, type: TaskType, note: String, high: Boolean, assignee: Long?) = viewModelScope.launch { c.pms.createTask(hotelId, roomId, type, note, high, assignee) }
    fun setTask(t: HousekeepingTaskEntity, s: TaskStatus) = viewModelScope.launch { c.pms.setTaskStatus(t, s) }
    fun addStaff(name: String, email: String, phone: String, pw: String, role: Role) = viewModelScope.launch { c.pms.addStaff(hotelId, name, email, phone, pw, role).report("staff_added") }
    fun setStaffActive(id: Long, a: Boolean) = viewModelScope.launch { c.pms.setUserActive(id, a) }
    fun saveType(t: RoomTypeEntity) = viewModelScope.launch { c.hotels.saveRoomType(t); _msg.tryEmit("saved") }
    fun saveSeason(s: SeasonRateEntity) = viewModelScope.launch { c.hotels.saveSeason(s); _msg.tryEmit("saved") }
    fun deleteSeason(s: SeasonRateEntity) = viewModelScope.launch { c.hotels.deleteSeason(s) }
    fun saveHotel(h: HotelEntity) = viewModelScope.launch { c.hotels.updateHotel(h); _msg.tryEmit("saved") }
    suspend fun report(days: Int) = c.pms.report(hotelId, days)
    suspend fun guests() = c.pms.guests(hotelId)
}

class AdminViewModel(val c: AppContainer) : ViewModel() {
    val hotels = c.hotels.allHotels().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val users = c.admin.users().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val bookings = c.admin.allBookings().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private val _stats = MutableStateFlow<PlatformStats?>(null)
    val stats: StateFlow<PlatformStats?> = _stats
    init { refresh() }
    fun refresh() = viewModelScope.launch { _stats.value = c.admin.stats() }
    fun setHotelStatus(id: Long, s: HotelStatus) = viewModelScope.launch { c.hotels.setHotelStatus(id, s); refresh() }
    fun setUserActive(id: Long, a: Boolean) = viewModelScope.launch { c.admin.setUserActive(id, a); refresh() }
    fun setVerification(id: Long, v: VerificationStatus) = viewModelScope.launch { c.admin.setVerification(id, v) }
}
