package et.hotelbooking.app.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import et.hotelbooking.app.R
import et.hotelbooking.app.domain.*

object Labels {
    @StringRes fun role(r: Role) = when (r) {
        Role.CLIENT -> R.string.role_client; Role.MANAGER -> R.string.role_manager; Role.RECEPTIONIST -> R.string.role_receptionist
        Role.HOUSEKEEPING -> R.string.role_housekeeping; Role.ADMIN -> R.string.role_admin
    }
    @StringRes fun status(s: BookingStatus) = when (s) {
        BookingStatus.PENDING -> R.string.status_pending; BookingStatus.CONFIRMED -> R.string.status_confirmed
        BookingStatus.CHECKED_IN -> R.string.status_checked_in; BookingStatus.CHECKED_OUT -> R.string.status_checked_out
        BookingStatus.CANCELLED -> R.string.status_cancelled; BookingStatus.REJECTED -> R.string.status_rejected; BookingStatus.NO_SHOW -> R.string.status_no_show
    }
    @StringRes fun payStatus(s: PaymentStatus) = when (s) {
        PaymentStatus.UNPAID -> R.string.pay_unpaid; PaymentStatus.PENDING_VERIFICATION -> R.string.pay_pending_verification
        PaymentStatus.PARTIALLY_PAID -> R.string.pay_partially_paid; PaymentStatus.PAID -> R.string.pay_paid
        PaymentStatus.REFUNDED -> R.string.pay_refunded; PaymentStatus.PARTIALLY_REFUNDED -> R.string.pay_partially_refunded
    }
    @StringRes fun method(m: PaymentMethod) = when (m) {
        PaymentMethod.TELEBIRR -> R.string.pm_telebirr; PaymentMethod.CBE_BIRR -> R.string.pm_cbe_birr; PaymentMethod.BANK_TRANSFER -> R.string.pm_bank
        PaymentMethod.VISA -> R.string.pm_visa; PaymentMethod.MASTERCARD -> R.string.pm_mastercard; PaymentMethod.CASH -> R.string.pm_cash
        PaymentMethod.PAY_AT_HOTEL -> R.string.pm_pay_at_hotel
    }
    @StringRes fun roomStatus(s: RoomStatus) = when (s) {
        RoomStatus.AVAILABLE -> R.string.room_available; RoomStatus.OCCUPIED -> R.string.room_occupied; RoomStatus.RESERVED -> R.string.room_reserved
        RoomStatus.CLEANING -> R.string.room_cleaning; RoomStatus.MAINTENANCE -> R.string.room_maintenance
    }
    @StringRes fun hotelStatus(s: HotelStatus) = when (s) {
        HotelStatus.PENDING -> R.string.hs_pending; HotelStatus.APPROVED -> R.string.hs_approved; HotelStatus.REJECTED -> R.string.hs_rejected; HotelStatus.SUSPENDED -> R.string.hs_suspended
    }
    @StringRes fun source(s: BookingSource) = when (s) { BookingSource.APP -> R.string.src_app; BookingSource.WALK_IN -> R.string.src_walk_in; BookingSource.PHONE -> R.string.src_phone }
    @StringRes fun charge(c: ChargeCategory) = when (c) {
        ChargeCategory.ROOM -> R.string.charge_room; ChargeCategory.MINIBAR -> R.string.charge_minibar; ChargeCategory.RESTAURANT -> R.string.charge_restaurant
        ChargeCategory.LAUNDRY -> R.string.charge_laundry; ChargeCategory.SPA -> R.string.charge_spa; ChargeCategory.TRANSPORT -> R.string.charge_transport; ChargeCategory.OTHER -> R.string.charge_other
    }
    @StringRes fun task(t: TaskType) = when (t) { TaskType.CLEANING -> R.string.task_cleaning; TaskType.INSPECTION -> R.string.task_inspection; TaskType.MAINTENANCE -> R.string.task_maintenance; TaskType.TURNDOWN -> R.string.task_turndown }
    @StringRes fun taskStatus(t: TaskStatus) = when (t) { TaskStatus.TODO -> R.string.ts_todo; TaskStatus.IN_PROGRESS -> R.string.ts_in_progress; TaskStatus.DONE -> R.string.ts_done }
    @StringRes fun idType(t: IdType) = when (t) { IdType.NATIONAL_ID -> R.string.id_fayda; IdType.KEBELE_ID -> R.string.id_kebele; IdType.PASSPORT -> R.string.id_passport; IdType.DRIVING_LICENSE -> R.string.id_driving }
    @StringRes fun verification(v: VerificationStatus) = when (v) { VerificationStatus.NONE -> R.string.ver_none; VerificationStatus.PENDING -> R.string.ver_pending; VerificationStatus.VERIFIED -> R.string.ver_verified; VerificationStatus.REJECTED -> R.string.ver_rejected }
    @StringRes fun facility(f: Facility) = when (f) {
        Facility.WIFI -> R.string.fac_wifi; Facility.RESTAURANT -> R.string.fac_restaurant; Facility.PARKING -> R.string.fac_parking; Facility.POOL -> R.string.fac_pool
        Facility.SPA -> R.string.fac_spa; Facility.GYM -> R.string.fac_gym; Facility.AIRPORT_SHUTTLE -> R.string.fac_airport_shuttle; Facility.BAR -> R.string.fac_bar
        Facility.CONFERENCE -> R.string.fac_conference; Facility.LAUNDRY -> R.string.fac_laundry; Facility.ROOM_SERVICE -> R.string.fac_room_service; Facility.GARDEN -> R.string.fac_garden
        Facility.LAKE_VIEW -> R.string.fac_lake_view; Facility.MOUNTAIN_VIEW -> R.string.fac_mountain_view; Facility.FAMILY_ROOMS -> R.string.fac_family_rooms
        Facility.ACCESSIBLE -> R.string.fac_accessible; Facility.BREAKFAST -> R.string.fac_breakfast; Facility.COFFEE_CEREMONY -> R.string.fac_coffee_ceremony
    }
    fun facilityIcon(f: Facility): ImageVector = when (f) {
        Facility.WIFI -> Icons.Outlined.Wifi; Facility.RESTAURANT -> Icons.Outlined.Restaurant; Facility.PARKING -> Icons.Outlined.LocalParking
        Facility.POOL -> Icons.Outlined.Pool; Facility.SPA -> Icons.Outlined.Spa; Facility.GYM -> Icons.Outlined.FitnessCenter
        Facility.AIRPORT_SHUTTLE -> Icons.Outlined.AirportShuttle; Facility.BAR -> Icons.Outlined.LocalBar; Facility.CONFERENCE -> Icons.Outlined.Groups
        Facility.LAUNDRY -> Icons.Outlined.LocalLaundryService; Facility.ROOM_SERVICE -> Icons.Outlined.RoomService; Facility.GARDEN -> Icons.Outlined.Park
        Facility.LAKE_VIEW -> Icons.Outlined.Water; Facility.MOUNTAIN_VIEW -> Icons.Outlined.Landscape; Facility.FAMILY_ROOMS -> Icons.Outlined.FamilyRestroom
        Facility.ACCESSIBLE -> Icons.Outlined.Accessible; Facility.BREAKFAST -> Icons.Outlined.FreeBreakfast; Facility.COFFEE_CEREMONY -> Icons.Outlined.Coffee
    }
}
