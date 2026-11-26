package et.hotelbooking.app.data.remote

/**
 * REST contract for the planned Django REST Framework backend (not used by the offline demo build).
 * All endpoints are relative to BuildConfig.API_BASE_URL, JSON, JWT bearer auth.
 * Implement Remote*Repository classes against these and switch them in AppContainer.
 *
 *  Auth
 *   POST   auth/register/                {name,email,phone,password,role,id_document?}
 *   POST   auth/token/                   {email,password} -> {access,refresh}
 *   POST   auth/token/refresh/
 *   GET    me/   PATCH me/
 *   POST   me/id-document/               multipart (front, back, type, number, expiry)
 *  Catalogue
 *   GET    hotels/?city=&region=&check_in=&check_out=&guests=&min_price=&max_price=&stars=&min_rating=&facilities=&sort=
 *   GET    hotels/{id}/   GET hotels/{id}/room-types/?check_in=&check_out=   GET hotels/{id}/reviews/
 *   GET    landmarks/     GET cities/
 *  Bookings (client)
 *   POST   bookings/quote/               {room_type,check_in,check_out,resident}
 *   POST   bookings/                     {...BookingRequest}  -> status PENDING, reference
 *   GET    bookings/mine/   GET bookings/{ref}/   POST bookings/{ref}/cancel/
 *   POST   payments/intent/              {booking, method} -> provider redirect / USSD push (Telebirr, CBE Birr, card gateway)
 *   POST   payments/webhook/{provider}/  (server-to-server confirmation; client never marks itself paid)
 *   POST   reviews/                      {booking, rating, title, comment} (server verifies CHECKED_OUT)
 *   GET    notifications/   POST notifications/read-all/   (FCM push token: POST devices/)
 *  Hotel management (PMS) — scoped to the staff member's hotel and permission set
 *   GET    pms/front-desk/?date=         arrivals, departures, in-house
 *   POST   pms/bookings/{id}/confirm|reject|no-show|check-in|check-out/
 *   POST   pms/walk-ins/   GET|POST pms/bookings/{id}/folio/   GET pms/bookings/{id}/invoice.pdf
 *   GET|POST|PATCH pms/rooms/  pms/room-types/  pms/seasons/  pms/housekeeping-tasks/  pms/staff/
 *   GET    pms/guests/   GET pms/reports/?from=&to=   (occupancy, ADR, RevPAR, revenue, sources)
 *  Admin
 *   GET    admin/stats/   GET|PATCH admin/hotels/{id}/ (approve/reject/suspend)
 *   GET|PATCH admin/users/{id}/ (activate, verify ID)   GET admin/bookings/
 */
object ApiContract {
    const val VERSION = "v1"
}
