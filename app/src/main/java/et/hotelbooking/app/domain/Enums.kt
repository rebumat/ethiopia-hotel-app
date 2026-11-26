package et.hotelbooking.app.domain

/** User roles. Staff roles (MANAGER, RECEPTIONIST, HOUSEKEEPING) are always bound to one hotel. */
enum class Role { CLIENT, MANAGER, RECEPTIONIST, HOUSEKEEPING, ADMIN;
    val isStaff get() = this == MANAGER || this == RECEPTIONIST || this == HOUSEKEEPING
}

/** Fine-grained permissions for the hotel management (PMS) module. */
enum class Permission {
    VIEW_DASHBOARD, FRONT_DESK, CHECK_IN_OUT, WALK_IN, MANAGE_RESERVATIONS, VIEW_GUESTS,
    VIEW_ROOMS, UPDATE_ROOM_STATUS, MANAGE_ROOMS, HOUSEKEEPING, MANAGE_RATES, MANAGE_STAFF,
    VIEW_REPORTS, EDIT_HOTEL_PROFILE, VIEW_CALENDAR
}

object Permissions {
    fun of(role: Role): Set<Permission> = when (role) {
        Role.MANAGER -> Permission.entries.toSet()
        Role.RECEPTIONIST -> setOf(
            Permission.VIEW_DASHBOARD, Permission.FRONT_DESK, Permission.CHECK_IN_OUT, Permission.WALK_IN,
            Permission.MANAGE_RESERVATIONS, Permission.VIEW_GUESTS, Permission.VIEW_ROOMS,
            Permission.UPDATE_ROOM_STATUS, Permission.VIEW_CALENDAR, Permission.HOUSEKEEPING
        )
        Role.HOUSEKEEPING -> setOf(Permission.VIEW_ROOMS, Permission.UPDATE_ROOM_STATUS, Permission.HOUSEKEEPING)
        else -> emptySet()
    }
}

/**
 * Booking lifecycle:
 * PENDING -> CONFIRMED -> CHECKED_IN -> CHECKED_OUT
 * PENDING -> REJECTED (by hotel) ; PENDING/CONFIRMED -> CANCELLED (by guest or hotel)
 * CONFIRMED -> NO_SHOW (guest did not arrive by the end of the check-in day)
 */
enum class BookingStatus { PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, REJECTED, NO_SHOW;
    val isActive get() = this == PENDING || this == CONFIRMED || this == CHECKED_IN
    val blocksInventory get() = this == PENDING || this == CONFIRMED || this == CHECKED_IN
}

enum class PaymentStatus { UNPAID, PENDING_VERIFICATION, PARTIALLY_PAID, PAID, REFUNDED, PARTIALLY_REFUNDED }

enum class PaymentMethod { TELEBIRR, CBE_BIRR, BANK_TRANSFER, VISA, MASTERCARD, CASH, PAY_AT_HOTEL;
    val isCard get() = this == VISA || this == MASTERCARD
    val isLocal get() = this == TELEBIRR || this == CBE_BIRR || this == BANK_TRANSFER
}

enum class PaymentType { DEPOSIT, PREPAYMENT, SETTLEMENT, REFUND }

enum class RoomStatus { AVAILABLE, OCCUPIED, RESERVED, CLEANING, MAINTENANCE }

enum class HotelStatus { PENDING, APPROVED, REJECTED, SUSPENDED }

enum class BookingSource { APP, WALK_IN, PHONE }

enum class UserMode { LOCAL, TOURIST }

enum class ChargeCategory { ROOM, MINIBAR, RESTAURANT, LAUNDRY, SPA, TRANSPORT, OTHER }

enum class TaskType { CLEANING, INSPECTION, MAINTENANCE, TURNDOWN }

enum class TaskStatus { TODO, IN_PROGRESS, DONE }

enum class NotificationType {
    BOOKING_SUBMITTED, BOOKING_CONFIRMED, BOOKING_REJECTED, BOOKING_CANCELLED, CHECKED_IN, CHECKED_OUT,
    REVIEW_REMINDER, NEW_RESERVATION, PAYMENT_RECEIVED, HOTEL_APPROVED, HOTEL_SUSPENDED, WELCOME, NO_SHOW, HOTEL_REGISTERED, ID_VERIFIED, ID_REJECTED, ID_SUBMITTED
}

enum class IdType { NATIONAL_ID, KEBELE_ID, PASSPORT, DRIVING_LICENSE }

enum class Facility { WIFI, RESTAURANT, PARKING, POOL, SPA, GYM, AIRPORT_SHUTTLE, BAR, CONFERENCE, LAUNDRY, ROOM_SERVICE, GARDEN, LAKE_VIEW, MOUNTAIN_VIEW, FAMILY_ROOMS, ACCESSIBLE, BREAKFAST, COFFEE_CEREMONY }

/** Identity verification state for a user's uploaded ID document (reviewed by admins). */
enum class VerificationStatus { NONE, PENDING, VERIFIED, REJECTED }
