package et.hotelbooking.app.ui.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import et.hotelbooking.app.AppContainer
import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.data.repo.*
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.util.Dates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> { data object Loading : UiState<Nothing>; data class Ready<T>(val data: T) : UiState<T>; data class Error(val code: String) : UiState<Nothing> }

class HomeViewModel(c: AppContainer) : ViewModel() {
    val hotels = c.hotels.approvedHotels().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val minPrices: StateFlow<Map<Long, Pair<Double, Double>>> = c.hotels.allRoomTypes().map { types ->
        types.filter { it.active }.groupBy { it.hotelId }.mapValues { (_, l) -> l.minOf { it.priceEtb } to l.minOf { it.foreignerPriceEtb ?: it.priceEtb } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
}

class SearchViewModel(private val c: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<HotelResult>>>(UiState.Loading)
    val state: StateFlow<UiState<List<HotelResult>>> = _state
    fun run(criteria: SearchCriteria) = viewModelScope.launch {
        _state.value = UiState.Loading
        _state.value = runCatching { UiState.Ready(c.hotels.search(criteria)) }.getOrElse { UiState.Error(it.message ?: "error") }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class HotelDetailViewModel(private val c: AppContainer, val hotelId: Long) : ViewModel() {
    val hotel = c.hotels.hotel(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val types = c.hotels.roomTypes(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val reviews = c.hotels.reviews(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val seasons = c.hotels.seasons(hotelId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val _availability = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val availability: StateFlow<Map<Long, Int>> = _availability
    private val _reviewable = MutableStateFlow<BookingEntity?>(null)
    val reviewable: StateFlow<BookingEntity?> = _reviewable

    fun refreshAvailability(from: Long?, to: Long?) = viewModelScope.launch {
        if (from == null || to == null) { _availability.value = emptyMap(); return@launch }
        _availability.value = c.hotels.roomTypesFor(hotelId).associate { it.id to c.hotels.available(it.id, from, to) }
    }
    fun refreshReviewable(userId: Long?) = viewModelScope.launch { _reviewable.value = userId?.let { c.hotels.reviewableBooking(it, hotelId) } }
    fun submitReview(userId: Long, rating: Int, title: String, comment: String, done: (Boolean) -> Unit) = viewModelScope.launch {
        val b = _reviewable.value ?: return@launch done(false)
        val r = c.hotels.addReview(userId, b.id, rating, title, comment)
        refreshReviewable(userId); done(r.isSuccess)
    }
}

class SavedHotelsViewModel(private val c: AppContainer) : ViewModel() {
    private val _hotels = MutableStateFlow<List<HotelEntity>>(emptyList())
    val hotels: StateFlow<List<HotelEntity>> = _hotels
    val minPrices: StateFlow<Map<Long, Pair<Double, Double>>> = c.hotels.allRoomTypes().map { types ->
        types.filter { it.active }.groupBy { it.hotelId }.mapValues { (_, l) -> l.minOf { it.priceEtb } to l.minOf { it.foreignerPriceEtb ?: it.priceEtb } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /** Loads the given hotels in the order the user saved them. */
    fun load(ids: List<Long>) = viewModelScope.launch {
        _hotels.value = if (ids.isEmpty()) emptyList() else c.hotels.hotelsByIds(ids)
    }
}

class CompareViewModel(private val c: AppContainer) : ViewModel() {
    private val _data = MutableStateFlow<List<Pair<HotelEntity, List<RoomTypeEntity>>>>(emptyList())
    val data: StateFlow<List<Pair<HotelEntity, List<RoomTypeEntity>>>> = _data
    fun load(ids: List<Long>) = viewModelScope.launch { _data.value = c.hotels.hotelsByIds(ids).map { it to c.hotels.roomTypesFor(it.id) } }
}

data class BookingDraft(
    val hotel: HotelEntity? = null, val types: List<RoomTypeEntity> = emptyList(), val typeId: Long? = null,
    val checkIn: Long = Dates.today() + 1, val checkOut: Long = Dates.today() + 3, val adults: Int = 2, val children: Int = 0,
    val name: String = "", val phone: String = "", val email: String = "", val nationality: String = "", val idType: IdType? = null, val idNumber: String = "",
    val resident: Boolean = true, val requests: String = "", val quote: Quote? = null, val available: Int? = null,
)

class BookingViewModel(private val c: AppContainer, hotelId: Long, roomTypeId: Long?) : ViewModel() {
    private val _d = MutableStateFlow(BookingDraft(typeId = roomTypeId))
    val draft: StateFlow<BookingDraft> = _d
    private val _result = MutableStateFlow<Result<BookingEntity>?>(null)
    val result: StateFlow<Result<BookingEntity>?> = _result
    val submitting = MutableStateFlow(false)

    init { viewModelScope.launch {
        val h = c.hotels.hotelsByIds(listOf(hotelId)).firstOrNull()
        val types = c.hotels.roomTypesFor(hotelId).filter { it.active }
        _d.value = _d.value.copy(hotel = h, types = types, typeId = roomTypeId ?: types.firstOrNull()?.id)
        requote()
    } }

    fun prefill(user: UserEntity?, crit: SearchCriteria, resident: Boolean) {
        val d = _d.value
        _d.value = d.copy(name = d.name.ifBlank { user?.fullName ?: "" }, phone = d.phone.ifBlank { user?.phone ?: "" }, email = d.email.ifBlank { user?.email ?: "" },
            nationality = d.nationality.ifBlank { user?.nationality ?: "" }, idType = d.idType ?: user?.idType, idNumber = d.idNumber.ifBlank { user?.idNumber ?: "" },
            checkIn = crit.checkIn ?: d.checkIn, checkOut = crit.checkOut ?: d.checkOut, adults = crit.guests.coerceIn(1, 6), resident = resident)
        requote()
    }
    fun update(f: (BookingDraft) -> BookingDraft) { _d.value = f(_d.value); }
    fun updateAndQuote(f: (BookingDraft) -> BookingDraft) { _d.value = f(_d.value); requote() }
    fun requote() = viewModelScope.launch {
        val d = _d.value; val h = d.hotel ?: return@launch; val t = d.typeId ?: return@launch
        if (d.checkOut <= d.checkIn) return@launch
        val resident = d.resident || !h.dualPricing
        _d.value = _d.value.copy(quote = c.bookings.quote(h.id, t, d.checkIn, d.checkOut, resident), available = c.hotels.available(t, d.checkIn, d.checkOut))
    }
    fun submit(userId: Long?, payment: DemoPayment?) = viewModelScope.launch {
        val d = _d.value; val h = d.hotel ?: return@launch
        submitting.value = true
        _result.value = c.bookings.create(BookingRequest(userId, h.id, d.typeId!!, d.checkIn, d.checkOut, d.adults, d.children, d.name, d.phone, d.email,
            d.nationality, d.idType, d.idNumber, d.resident, d.requests), payment)
        submitting.value = false
    }
    fun clearResult() { _result.value = null }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MyBookingsViewModel(private val c: AppContainer, userId: Long) : ViewModel() {
    val bookings = c.bookings.myBookings(userId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val hotels = c.hotels.allHotels().map { l -> l.associateBy { it.id } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    val types = c.hotels.allRoomTypes().map { l -> l.associateBy { it.id } }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
    suspend fun preview(id: Long) = c.bookings.cancellationPreview(id)
    fun cancel(id: Long, done: (Result<Double>) -> Unit) = viewModelScope.launch { done(c.bookings.cancelByGuest(id)) }
    fun pay(id: Long, p: DemoPayment, done: (Boolean) -> Unit) = viewModelScope.launch { done(c.bookings.payDemo(id, p).isSuccess) }
    fun folio(id: Long) = c.pms.folio(id)
    suspend fun invoice(id: Long) = c.pms.invoice(id)
}

class NotificationsViewModel(private val c: AppContainer, private val userId: Long) : ViewModel() {
    val items = c.bookings.notifications(userId).stateIn(viewModelScope, SharingStarted.Eagerly, null)
    fun markAll() = viewModelScope.launch { c.bookings.markAllRead(userId) }
}
