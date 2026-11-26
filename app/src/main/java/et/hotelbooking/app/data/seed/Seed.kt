package et.hotelbooking.app.data.seed

import androidx.room.withTransaction
import et.hotelbooking.app.data.local.*
import et.hotelbooking.app.domain.*
import et.hotelbooking.app.util.Dates
import et.hotelbooking.app.util.PasswordHasher
import kotlin.random.Random

/**
 * SAMPLE DATA ONLY. All hotels below are fictional; names, prices, availability, and reviews are
 * illustrative and do not represent real businesses or real rates.
 */
object Seed {
    data class DemoAccount(val email: String, val password: String, val role: Role)
    val demoAccounts = listOf(
        DemoAccount("client@ethiohotels.demo", "Client@123", Role.CLIENT),
        DemoAccount("tourist@ethiohotels.demo", "Tourist@123", Role.CLIENT),
        DemoAccount("manager@ethiohotels.demo", "Manager@123", Role.MANAGER),
        DemoAccount("reception@ethiohotels.demo", "Reception@123", Role.RECEPTIONIST),
        DemoAccount("housekeeping@ethiohotels.demo", "House@123", Role.HOUSEKEEPING),
        DemoAccount("admin@ethiohotels.demo", "Admin@123", Role.ADMIN),
    )

    private data class H(val name: String, val city: String, val address: String, val stars: Int, val desc: String, val fac: String,
                         val images: String, val featured: Boolean = false, val dual: Boolean = false, val priceBase: Int,
                         val lat: Double, val lng: Double, val status: HotelStatus = HotelStatus.APPROVED, val freeCancel: Int = 2)

    private val hotels = listOf(
        H("Sheger Grand Hotel", "addis_ababa", "Bole Road, near Edna Mall, Addis Ababa", 5,
            "A modern five-star city hotel on Bole Road, minutes from Addis Ababa Bole International Airport. Rooftop restaurant with views of Entoto, traditional coffee ceremony every afternoon, spa, and a conference centre for business travellers.",
            "WIFI,RESTAURANT,PARKING,POOL,SPA,GYM,AIRPORT_SHUTTLE,BAR,CONFERENCE,LAUNDRY,ROOM_SERVICE,BREAKFAST,COFFEE_CEREMONY,ACCESSIBLE",
            "hotel_building,hotel_lobby2,hotel_room2,hotel_suite,hotel_pool3,hotel_restaurant1,hotel_bath", true, true, 14500, 8.9958, 38.7869, freeCancel = 2),
        H("Entoto Hills Lodge", "addis_ababa", "Entoto Road, Gullele, Addis Ababa", 4,
            "A peaceful eucalyptus-scented lodge on the slopes of Mount Entoto with panoramic views over the capital. Ideal for hiking, horse riding and visiting Entoto Park.",
            "WIFI,RESTAURANT,PARKING,GARDEN,MOUNTAIN_VIEW,BREAKFAST,COFFEE_CEREMONY,FAMILY_ROOMS",
            "hotel_lobby1,hotel_room1,lm_addis,hotel_restaurant2", false, true, 8200, 9.0833, 38.7667),
        H("Piassa Heritage Inn", "addis_ababa", "Churchill Avenue, Piassa, Addis Ababa", 3,
            "A friendly, good-value inn in historic Piassa, walking distance to St. George's Cathedral, cafés and the National Theatre.",
            "WIFI,RESTAURANT,BREAKFAST,LAUNDRY,COFFEE_CEREMONY",
            "hotel_room2,hotel_lobby3,hotel_restaurant1", false, false, 3600, 9.0353, 38.7525),
        H("Bole Skyline Business Hotel", "addis_ababa", "Africa Avenue, Bole, Addis Ababa", 4,
            "Business-friendly rooms with fast Wi-Fi, a 24-hour gym and meeting rooms close to the African Union and embassies.",
            "WIFI,RESTAURANT,PARKING,GYM,CONFERENCE,AIRPORT_SHUTTLE,BAR,ROOM_SERVICE,BREAKFAST,ACCESSIBLE",
            "hotel_lobby2,hotel_room1,hotel_restaurant2,hotel_bath", false, true, 9800, 9.0050, 38.7800),
        H("Tana Lakeside Resort", "bahir_dar", "Lake Tana shore, Bahir Dar", 4,
            "Garden bungalows on the shore of Lake Tana with boat trips to the island monasteries and easy access to the Blue Nile Falls.",
            "WIFI,RESTAURANT,PARKING,POOL,GARDEN,LAKE_VIEW,BAR,BREAKFAST,FAMILY_ROOMS",
            "hotel_resort1,hotel_pool1,hotel_room1,lm_lake_tana,hotel_restaurant2", true, true, 7800, 11.6000, 37.3900),
        H("Blue Nile Gardens Hotel", "bahir_dar", "Kebele 4, near the Blue Nile bridge, Bahir Dar", 3,
            "Comfortable mid-range hotel with a palm-lined garden a short walk from the Blue Nile outflow.",
            "WIFI,RESTAURANT,PARKING,GARDEN,BREAKFAST",
            "hotel_room2,hotel_lobby3,city_bahirdar", false, false, 4200, 11.5950, 37.3950),
        H("Fasil Castle View Hotel", "gondar", "Piazza, opposite the Royal Enclosure, Gondar", 3,
            "Rooms overlooking the castles of Fasil Ghebbi, with a rooftop terrace serving Ethiopian dishes.",
            "WIFI,RESTAURANT,PARKING,BREAKFAST,COFFEE_CEREMONY,MOUNTAIN_VIEW",
            "hotel_room1,city_gondar,hotel_restaurant1", false, true, 4800, 12.6070, 37.4680),
        H("Simien Gateway Lodge", "gondar", "Debark road, gateway to Simien Mountains National Park", 3,
            "Stone-built lodge for trekkers heading into the Simien Mountains; guides and scouts can be arranged at reception.",
            "WIFI,RESTAURANT,PARKING,MOUNTAIN_VIEW,BREAKFAST,GARDEN",
            "lm_simien,hotel_room2,hotel_restaurant2", false, true, 5200, 13.1500, 37.9000),
        H("Lalibela Highlands Lodge", "lalibela", "Hilltop, 2 km from the rock-hewn churches, Lalibela", 4,
            "Eco-designed tukul-style suites on a ridge with sunset views over the Lasta mountains, a short drive from the rock-hewn churches.",
            "WIFI,RESTAURANT,PARKING,MOUNTAIN_VIEW,BREAKFAST,COFFEE_CEREMONY,GARDEN,AIRPORT_SHUTTLE",
            "city_lalibela,hotel_suite,hotel_lobby1,lm_lalibela", true, true, 9600, 12.0400, 39.0500, freeCancel = 7),
        H("Rock Church Guesthouse", "lalibela", "Near Bete Medhane Alem, Lalibela", 2,
            "Simple, clean and welcoming family guesthouse steps from the northern church group.",
            "WIFI,BREAKFAST,COFFEE_CEREMONY", "hotel_room2,city_lalibela", false, false, 2400, 12.0320, 39.0430),
        H("Hawassa Lakeshore Resort", "hawassa", "Lakeshore Road, Hawassa", 4,
            "Family resort on Lake Hawassa with a large pool, lakeside walks, bird watching and the famous fish market nearby.",
            "WIFI,RESTAURANT,PARKING,POOL,SPA,GARDEN,LAKE_VIEW,BAR,BREAKFAST,FAMILY_ROOMS,CONFERENCE",
            "hotel_pool2,hotel_resort2,hotel_room1,city_hawassa,hotel_restaurant1", true, false, 8400, 7.0600, 38.4800),
        H("Sidama Garden Hotel", "hawassa", "Piazza, Hawassa", 3, "Central hotel with a leafy garden and Sidama coffee served all day.",
            "WIFI,RESTAURANT,PARKING,GARDEN,BREAKFAST,COFFEE_CEREMONY", "hotel_room2,hotel_lobby3", false, false, 3900, 7.0500, 38.4900),
        H("Adama Rift Valley Hotel", "adama", "Main road to Awash, Adama", 4,
            "Conference and leisure hotel in the Rift Valley, popular for weekend getaways and hot-spring trips to Sodere.",
            "WIFI,RESTAURANT,PARKING,POOL,GYM,CONFERENCE,BAR,BREAKFAST,FAMILY_ROOMS",
            "hotel_pool3,hotel_building,hotel_room1,city_adama", false, false, 6200, 8.5400, 39.2700),
        H("Aksum Obelisk Hotel", "axum", "Near the Northern Stelae Field, Aksum", 3,
            "Walk to the ancient stelae and St. Mary of Zion church; rooftop views of the obelisks at sunrise.",
            "WIFI,RESTAURANT,PARKING,BREAKFAST,COFFEE_CEREMONY", "city_axum,hotel_room2,hotel_restaurant2", false, true, 4500, 14.1300, 38.7200),
        H("Harar Jugol Heritage Guesthouse", "harar", "Inside the Jugol walls, Harar", 2,
            "A restored Harari house with colourful traditional interiors, a short walk to the hyena-feeding site in the evening.",
            "WIFI,BREAKFAST,COFFEE_CEREMONY,GARDEN", "city_harar,lm_harar,hotel_room2", false, false, 2800, 9.3120, 42.1230),
        H("Forty Springs Lodge", "arba_minch", "Ridge between Lake Abaya and Lake Chamo, Arba Minch", 4,
            "Lodge on the ridge known as the \"Bridge of God\" with views of both lakes and boat trips to see crocodiles and hippos.",
            "WIFI,RESTAURANT,PARKING,POOL,LAKE_VIEW,BAR,BREAKFAST,GARDEN", "city_arbaminch,hotel_pool1,hotel_room1", false, true, 7200, 6.0300, 37.5600),
        H("Dire Dawa Railway Hotel", "dire_dawa", "Kezira, near the railway station, Dire Dawa", 3,
            "Colonial-era inspired hotel near the Ethio-Djibouti railway station and Kefira market.",
            "WIFI,RESTAURANT,PARKING,BREAKFAST,BAR", "city_diredawa,hotel_room2,hotel_lobby3", false, false, 4100, 9.6000, 41.8600),
        H("Bishoftu Crater Lake Resort", "bishoftu", "Lake Hora shore, Bishoftu", 5,
            "Luxury crater-lake resort 45 km from Addis Ababa with spa, water sports and lakeside villas.",
            "WIFI,RESTAURANT,PARKING,POOL,SPA,GYM,LAKE_VIEW,BAR,CONFERENCE,ROOM_SERVICE,BREAKFAST,FAMILY_ROOMS,ACCESSIBLE",
            "hotel_resort2,hotel_pool1,hotel_suite,city_bishoftu,hotel_lobby1,hotel_bath", true, true, 16500, 8.7600, 38.9800, freeCancel = 3),
        H("Mekelle Highlands Hotel", "mekelle", "Romanat Square area, Mekelle", 3,
            "Base for exploring the rock-hewn churches of Tigray and tours to the Danakil Depression.",
            "WIFI,RESTAURANT,PARKING,BREAKFAST,GYM", "city_mekelle,hotel_room1,lm_danakil", false, false, 4300, 13.4960, 39.4750),
        H("Jimma Coffee Land Hotel", "jimma", "Jimma town centre", 3,
            "Newly registered hotel awaiting admin verification (demo of the approval workflow).",
            "WIFI,RESTAURANT,PARKING,BREAKFAST,COFFEE_CEREMONY", "hotel_building,hotel_room2", false, false, 3800, 7.6700, 36.8300, status = HotelStatus.PENDING),
    )

    private val roomTemplates = listOf(
        Triple("Standard Room", 1.0, 2), Triple("Deluxe Room", 1.35, 2), Triple("Family Room", 1.7, 4), Triple("Executive Suite", 2.4, 3))

    private val guestPool = listOf(
        Triple("Abebe Kebede", "+251911234567", "Ethiopian"), Triple("Meron Tadesse", "+251912345678", "Ethiopian"),
        Triple("Tewodros Haile", "+251913456789", "Ethiopian"), Triple("Chaltu Gemechu", "+251914567890", "Ethiopian"),
        Triple("Lemlem Gebremedhin", "+251915678901", "Ethiopian"), Triple("John Miller", "+14155550123", "American"),
        Triple("Sophie Martin", "+33612345678", "French"), Triple("Li Wei", "+8613800138000", "Chinese"),
        Triple("Ahmed Al-Farsi", "+971501234567", "Emirati"), Triple("Carlos García", "+34612345678", "Spanish"),
        Triple("Amina Yusuf", "+254712345678", "Kenyan"), Triple("Biniam Tesfay", "+251916789012", "Ethiopian"),
        Triple("Hanna Schmidt", "+491701234567", "German"), Triple("Kenji Watanabe", "+819012345678", "Japanese"),
        Triple("Fatuma Ali", "+251917890123", "Ethiopian"), Triple("Daniel Okafor", "+2348031234567", "Nigerian"))

    private val reviewPool = listOf(
        Triple(5, "Wonderful stay", "Warm Ethiopian hospitality, spotless rooms and a lovely coffee ceremony in the afternoon."),
        Triple(4, "Great location", "Very convenient location and friendly staff. Breakfast could have more variety."),
        Triple(5, "እጅግ በጣም ጥሩ", "በጣም ንጹህ ክፍል፣ ጥሩ አገልግሎት እና ምርጥ ቡና። እንደገና እመጣለሁ!"),
        Triple(4, "Tajaajila gaarii", "Hojjettoonni baay'ee gargaaraa turan. Kutaan qulqulluu fi nagaa qaba."),
        Triple(3, "Good value", "Decent room for the price. Hot water was sometimes slow in the morning."),
        Triple(5, "Perfect for tours", "Reception arranged our guide and transport. Highly recommended for sightseeing."),
        Triple(4, "Relaxing", "Beautiful garden and views, the injera and shiro were delicious."),
        Triple(5, "Excellent service", "Smooth check-in, the staff remembered our names. Fast Wi-Fi for work."))

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.users().count() > 0) return
        val rnd = Random(42)
        val today = Dates.today()
        db.withTransaction {
            val hotelIds = hotels.map { h ->
                db.hotels().insert(HotelEntity(name = h.name, city = h.city, region = CityInfo.regionOf(h.city), address = h.address, description = h.desc,
                    stars = h.stars, latitude = h.lat, longitude = h.lng, phone = "+251 11 " + (5000000 + rnd.nextInt(999999)), email = "info@" + h.name.lowercase().replace(" ", "").take(18) + ".example",
                    facilities = h.fac, images = h.images, status = h.status, freeCancelDays = h.freeCancel, dualPricing = h.dual, featured = h.featured,
                    childrenPolicy = "Children under 6 stay free using existing beds. Extra bed available on request.",
                    extraPolicies = "Valid ID or passport required at check-in. No smoking in rooms. Pets not allowed.",
                    checkInTime = "14:00", checkOutTime = if (h.stars >= 4) "12:00" else "11:00"))
            }
            // Room types and physical rooms
            val typeIdsByHotel = mutableMapOf<Long, List<Long>>()
            hotels.forEachIndexed { i, h ->
                val hid = hotelIds[i]
                val templates = when { h.stars >= 4 -> roomTemplates; h.stars == 3 -> roomTemplates.take(3); else -> roomTemplates.take(2) }
                val roomImages = listOf("hotel_room2", "hotel_room1", "hotel_resort1", "hotel_suite")
                val ids = templates.mapIndexed { ti, (name, mult, cap) ->
                    val base = Math.round(h.priceBase * mult / 50.0) * 50.0
                    db.roomTypes().insert(RoomTypeEntity(hotelId = hid, name = name, capacity = cap, bedType = when (ti) { 0 -> "Queen or twin"; 1 -> "King"; 2 -> "1 King + 2 Single"; else -> "King + living room" },
                        sizeSqm = listOf(24, 32, 42, 60)[ti], priceEtb = base, foreignerPriceEtb = if (h.dual) Math.round(base * 1.5 / 50.0) * 50.0 else null,
                        image = roomImages[ti], description = when (ti) {
                            0 -> "Comfortable room with work desk, free Wi-Fi, flat-screen TV and private bathroom."
                            1 -> "Spacious room with city or garden view, minibar, seating area and rain shower."
                            2 -> "Large room for families with one king and two single beds."
                            else -> "Separate living room, premium amenities, butler-style service and best views."
                        }))
                }
                typeIdsByHotel[hid] = ids
                val rooms = mutableListOf<RoomEntity>()
                val counts = if (i == 0) listOf(12, 8, 4, 4) else listOf(4, 3, 2, 2)
                ids.forEachIndexed { ti, tid ->
                    repeat(counts[ti]) { n ->
                        val floor = if (i == 0) listOf(1, 3, 3, 5)[ti] + (if (ti == 0 && n >= 6) 1 else if (ti == 1 && n >= 4) 1 else if (ti == 2 && n >= 2) 1 else 0) else ti + 1
                        val num = if (i == 0) when (ti) { 0 -> "${floor}0${n % 6 + 1}"; 1 -> "${floor}0${n % 4 + 1}"; 2 -> "${floor}0${n % 2 + 5}"; else -> "50${n + 1}" } else "${floor}0${n + 1}"
                        rooms += RoomEntity(hotelId = hid, roomTypeId = tid, number = num, floor = floor)
                    }
                }
                db.rooms().insertAll(rooms)
                if (h.dual || h.featured) {
                    db.seasons().insert(SeasonRateEntity(hotelId = hid, name = "Ethiopian New Year & Meskel", startMonth = 9, startDay = 8, endMonth = 9, endDay = 30, adjustmentPct = 20))
                    db.seasons().insert(SeasonRateEntity(hotelId = hid, name = "Genna & Timket", startMonth = 1, startDay = 5, endMonth = 1, endDay = 22, adjustmentPct = 25))
                    db.seasons().insert(SeasonRateEntity(hotelId = hid, name = "Kiremt (rainy season)", startMonth = 6, startDay = 15, endMonth = 8, endDay = 31, adjustmentPct = -10))
                }
            }

            // Users
            fun user(name: String, email: String, pw: String, role: Role, phone: String, hotelId: Long? = null, nationality: String = "Ethiopian",
                     idType: IdType? = null, idNumber: String = "", expiry: Long? = null, ver: VerificationStatus = VerificationStatus.NONE): UserEntity {
                val salt = PasswordHasher.newSalt()
                return UserEntity(fullName = name, email = email, phone = phone, passwordHash = PasswordHasher.hash(pw, salt), salt = salt, role = role,
                    hotelId = hotelId, nationality = nationality, idType = idType, idNumber = idNumber, idExpiry = expiry, verification = ver,
                    consentAt = if (idType != null) System.currentTimeMillis() else null)
            }
            val sheger = hotelIds[0]
            val clientId = db.users().insert(user("Hana Alemu", "client@ethiohotels.demo", "Client@123", Role.CLIENT, "+251911000111",
                idType = IdType.NATIONAL_ID, idNumber = "4821 7735 9021 6614", ver = VerificationStatus.VERIFIED))
            val touristId = db.users().insert(user("Emma Clarke", "tourist@ethiohotels.demo", "Tourist@123", Role.CLIENT, "+447700900123", nationality = "British",
                idType = IdType.PASSPORT, idNumber = "533829174", expiry = today + 1500, ver = VerificationStatus.PENDING))
            db.users().insert(user("Kenji Watanabe", "kenji@ethiohotels.demo", "Tourist@123", Role.CLIENT, "+819012345678", nationality = "Japanese",
                idType = IdType.PASSPORT, idNumber = "TZ1234567", expiry = today + 900, ver = VerificationStatus.PENDING))
            val managerId = db.users().insert(user("Selam Tesfaye", "manager@ethiohotels.demo", "Manager@123", Role.MANAGER, "+251911222333", sheger))
            db.users().insert(user("Dawit Bekele", "reception@ethiohotels.demo", "Reception@123", Role.RECEPTIONIST, "+251911444555", sheger))
            val hkId = db.users().insert(user("Almaz Girma", "housekeeping@ethiohotels.demo", "House@123", Role.HOUSEKEEPING, "+251911666777", sheger))
            val adminId = db.users().insert(user("Platform Admin", "admin@ethiohotels.demo", "Admin@123", Role.ADMIN, "+251911888999"))
            hotelIds.forEachIndexed { i, hid -> if (i in 1 until hotelIds.size - 1) db.users().insert(user("Manager " + hotels[i].name, "manager$i@ethiohotels.demo", "Manager@123", Role.MANAGER, "+2519110000" + (10 + i), hid)) }
            db.users().insert(user("Gemechu Dibaba", "jimma.manager@ethiohotels.demo", "Manager@123", Role.MANAGER, "+251917000222", hotelIds.last()))

            // Reviews for every approved hotel
            var totalReviewsInserted = 0
            hotels.forEachIndexed { i, h ->
                if (h.status != HotelStatus.APPROVED) return@forEachIndexed
                val n = 3 + rnd.nextInt(4)
                val list = (0 until n).map { k ->
                    val (r, t, c) = reviewPool[(i + k * 3) % reviewPool.size]
                    val rating = (r - if (h.stars <= 2 && rnd.nextBoolean()) 1 else 0).coerceIn(3, 5)
                    ReviewEntity(hotelId = hotelIds[i], userId = null, bookingId = null, authorName = guestPool[(i + k) % guestPool.size].first, rating = rating, title = t, comment = c,
                        createdAt = System.currentTimeMillis() - (k + 1) * 86_400_000L * (5 + rnd.nextInt(40)))
                }
                db.reviews().insertAll(list); totalReviewsInserted += n
                db.hotels().byId(hotelIds[i])?.let { db.hotels().update(it.copy(ratingAvg = list.map { r -> r.rating }.average(), reviewCount = list.size)) }
            }

            // ---- Bookings ----
            val usedRefs = HashSet<String>()
            val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
            fun genRef(): String { while (true) { val r = "EHB-" + (1..6).map { alphabet[rnd.nextInt(alphabet.length)] }.joinToString(""); if (usedRefs.add(r)) return r } }
            fun mk(hotelIdx: Int, typeIdx: Int, inDay: Long, nights: Int, guest: Triple<String, String, String>, status: BookingStatus, userId: Long? = null,
                   source: BookingSource = BookingSource.APP, roomId: Long? = null, email: String? = null, pay: PaymentMethod = PaymentMethod.PAY_AT_HOTEL,
                   paid: Boolean = false, reviewed: Boolean = false): BookingEntity {
                val hid = hotelIds[hotelIdx]
                val tid = typeIdsByHotel[hid]!![typeIdx.coerceAtMost(typeIdsByHotel[hid]!!.size - 1)]
                val base = hotels[hotelIdx].priceBase * roomTemplates[typeIdx].second
                val resident = guest.third == "Ethiopian" || !hotels[hotelIdx].dual
                val rate = Math.round((if (resident) base else base * 1.5) / 50.0) * 50.0
                val room = rate * nights; val sc = Math.round(room * 0.10).toDouble(); val vat = Math.round((room + sc) * 0.15).toDouble()
                val total = room + sc + vat
                return BookingEntity(reference = genRef(), userId = userId, hotelId = hid,
                    roomTypeId = tid, roomId = roomId, checkIn = inDay, checkOut = inDay + nights, adults = 2, children = 0, guestName = guest.first,
                    guestPhone = guest.second, guestEmail = email ?: (guest.first.lowercase().replace(" ", ".").replace("í", "i") + "@example.com"), nationality = guest.third,
                    idType = if (status == BookingStatus.CHECKED_IN || status == BookingStatus.CHECKED_OUT) (if (guest.third == "Ethiopian") IdType.NATIONAL_ID else IdType.PASSPORT) else null,
                    idNumber = if (status == BookingStatus.CHECKED_IN || status == BookingStatus.CHECKED_OUT) "P" + (1000000 + rnd.nextInt(8999999)) else "",
                    residentRate = resident, roomTotalEtb = room, serviceChargeEtb = sc, vatEtb = vat, totalEtb = total, status = status,
                    paymentMethod = pay, paymentStatus = if (paid || status == BookingStatus.CHECKED_OUT) PaymentStatus.PAID else PaymentStatus.UNPAID,
                    paidEtb = if (paid || status == BookingStatus.CHECKED_OUT) total else 0.0, source = source, reviewed = reviewed,
                    createdAt = System.currentTimeMillis() - (today - inDay + 10).coerceAtLeast(1) * 86_400_000L,
                    checkedInAt = if (status == BookingStatus.CHECKED_IN || status == BookingStatus.CHECKED_OUT) (inDay * 86_400_000L + 14 * 3_600_000L) else null,
                    checkedOutAt = if (status == BookingStatus.CHECKED_OUT) ((inDay + nights) * 86_400_000L + 11 * 3_600_000L) else null)
            }

            val shegerRooms = db.rooms().forHotel(sheger)
            val shegerTypes = typeIdsByHotel[sheger]!!
            fun roomOf(typeIdx: Int, n: Int) = shegerRooms.filter { it.roomTypeId == shegerTypes[typeIdx] }[n].id
            val hana = Triple("Hana Alemu", "+251911000111", "Ethiopian")
            val emma = Triple("Emma Clarke", "+447700900123", "British")

            // Client (Hana) bookings: completed stay (reviewable), upcoming confirmed, pending request
            db.bookings().insert(mk(4, 1, today - 20, 3, hana, BookingStatus.CHECKED_OUT, clientId, email = "client@ethiohotels.demo", pay = PaymentMethod.TELEBIRR, paid = true))
            db.bookings().insert(mk(0, 1, today + 12, 2, hana, BookingStatus.CONFIRMED, clientId, email = "client@ethiohotels.demo", pay = PaymentMethod.CBE_BIRR, paid = true, roomId = roomOf(1, 2)))
            db.bookings().insert(mk(8, 0, today + 30, 2, hana, BookingStatus.PENDING, clientId, email = "client@ethiohotels.demo"))
            // Tourist (Emma): arriving today at Sheger Grand (front-desk demo), past Lalibela stay already reviewed
            val emmaArrival = db.bookings().insert(mk(0, 3, today, 3, emma, BookingStatus.CONFIRMED, touristId, email = "tourist@ethiohotels.demo", pay = PaymentMethod.VISA, paid = true, roomId = roomOf(3, 0)).copy(cardBrand = "VISA", cardLast4 = "4242"))
            db.bookings().insert(mk(8, 1, today - 60, 2, emma, BookingStatus.CHECKED_OUT, touristId, email = "tourist@ethiohotels.demo", pay = PaymentMethod.MASTERCARD, paid = true, reviewed = true))

            // Sheger Grand operations: arrivals today, in-house guests (some departing today), pending requests
            db.bookings().insert(mk(0, 0, today, 2, guestPool[0], BookingStatus.CONFIRMED, source = BookingSource.PHONE, roomId = roomOf(0, 0)))
            db.bookings().insert(mk(0, 1, today, 1, guestPool[6], BookingStatus.CONFIRMED, roomId = roomOf(1, 0), pay = PaymentMethod.MASTERCARD, paid = true))
            val inHouse = listOf(
                mk(0, 0, today - 2, 2, guestPool[5], BookingStatus.CHECKED_IN, roomId = roomOf(0, 1), pay = PaymentMethod.VISA, paid = true),
                mk(0, 0, today - 1, 1, guestPool[1], BookingStatus.CHECKED_IN, roomId = roomOf(0, 2), source = BookingSource.WALK_IN, pay = PaymentMethod.CASH),
                mk(0, 1, today - 1, 3, guestPool[7], BookingStatus.CHECKED_IN, roomId = roomOf(1, 1), pay = PaymentMethod.VISA, paid = true),
                mk(0, 2, today - 3, 5, guestPool[3], BookingStatus.CHECKED_IN, roomId = roomOf(2, 0), pay = PaymentMethod.TELEBIRR, paid = true),
                mk(0, 3, today - 1, 4, guestPool[8], BookingStatus.CHECKED_IN, roomId = roomOf(3, 1), pay = PaymentMethod.MASTERCARD, paid = true),
                mk(0, 0, today - 4, 6, guestPool[12], BookingStatus.CHECKED_IN, roomId = roomOf(0, 3), source = BookingSource.PHONE),
            )
            val inHouseIds = db.bookings().insertAll(inHouse)
            inHouse.forEach { b -> b.roomId?.let { rid -> db.rooms().byId(rid)?.let { db.rooms().update(it.copy(status = RoomStatus.OCCUPIED)) } } }
            db.folio().insert(FolioChargeEntity(bookingId = inHouseIds[0], category = ChargeCategory.RESTAURANT, description = "Dinner, rooftop", amountEtb = 2350.0))
            db.folio().insert(FolioChargeEntity(bookingId = inHouseIds[0], category = ChargeCategory.MINIBAR, description = "Water, juice", amountEtb = 420.0))
            db.folio().insert(FolioChargeEntity(bookingId = inHouseIds[1], category = ChargeCategory.LAUNDRY, description = "4 items", amountEtb = 600.0))
            db.folio().insert(FolioChargeEntity(bookingId = inHouseIds[2], category = ChargeCategory.SPA, description = "Massage 60 min", amountEtb = 3200.0))
            inHouse.forEachIndexed { k, b -> if (b.paidEtb > 0) db.payments().insert(PaymentEntity(bookingId = inHouseIds[k], method = b.paymentMethod, type = PaymentType.PREPAYMENT, amountEtb = b.paidEtb, reference = "DEMO-SEED$k")) }
            db.bookings().insert(mk(0, 0, today + 3, 2, guestPool[9], BookingStatus.PENDING, pay = PaymentMethod.BANK_TRANSFER).copy(paymentStatus = PaymentStatus.PENDING_VERIFICATION))
            db.bookings().insert(mk(0, 1, today + 5, 3, guestPool[10], BookingStatus.PENDING))
            db.bookings().insert(mk(0, 2, today + 8, 2, guestPool[11], BookingStatus.CONFIRMED, roomId = roomOf(2, 1)))
            db.bookings().insert(mk(0, 0, today - 1, 1, guestPool[13], BookingStatus.CONFIRMED, roomId = roomOf(0, 5)))  // overdue arrival -> no-show candidate

            // History for reports: ~6 months of completed stays
            val hist = mutableListOf<BookingEntity>()
            var d = today - 180
            while (d < today - 5) {
                val perDay = 1 + rnd.nextInt(4)
                repeat(perDay) {
                    val ti = listOf(0, 0, 0, 1, 1, 2, 3)[rnd.nextInt(7)]
                    val g = guestPool[rnd.nextInt(guestPool.size)]
                    val src = listOf(BookingSource.APP, BookingSource.APP, BookingSource.APP, BookingSource.WALK_IN, BookingSource.WALK_IN, BookingSource.PHONE)[rnd.nextInt(6)]
                    val st = when (rnd.nextInt(20)) { 0 -> BookingStatus.CANCELLED; 1 -> BookingStatus.NO_SHOW; else -> BookingStatus.CHECKED_OUT }
                    val pm = listOf(PaymentMethod.TELEBIRR, PaymentMethod.CBE_BIRR, PaymentMethod.VISA, PaymentMethod.MASTERCARD, PaymentMethod.CASH, PaymentMethod.BANK_TRANSFER)[rnd.nextInt(6)]
                    hist += mk(0, ti, d, 1 + rnd.nextInt(4), g, st, source = src, pay = pm, roomId = roomOf(ti, rnd.nextInt(4)))
                }
                d += 1 + rnd.nextInt(2)
            }
            val histIds = db.bookings().insertAll(hist)
            hist.forEachIndexed { k, b -> if (b.status == BookingStatus.CHECKED_OUT && k % 4 == 0)
                db.folio().insert(FolioChargeEntity(bookingId = histIds[k], category = listOf(ChargeCategory.RESTAURANT, ChargeCategory.MINIBAR, ChargeCategory.LAUNDRY, ChargeCategory.TRANSPORT)[k % 4], description = "Extras", amountEtb = (300 + rnd.nextInt(2500)).toDouble())) }

            // Other hotels: a few bookings so availability varies
            for (i in 1 until hotels.size - 1) repeat(2) { k -> db.bookings().insert(mk(i, k, today + 2 + k * 4, 2, guestPool[(i + k) % guestPool.size], BookingStatus.CONFIRMED)) }

            // Room statuses and housekeeping
            db.rooms().update(db.rooms().byId(roomOf(0, 4))!!.copy(status = RoomStatus.CLEANING))
            db.rooms().update(db.rooms().byId(roomOf(1, 3))!!.copy(status = RoomStatus.CLEANING))
            db.rooms().update(db.rooms().byId(roomOf(0, 7))!!.copy(status = RoomStatus.MAINTENANCE, note = "AC repair"))
            db.tasks().insert(HousekeepingTaskEntity(hotelId = sheger, roomId = roomOf(0, 4), type = TaskType.CLEANING, note = "Departure clean", highPriority = true, assignedTo = hkId))
            db.tasks().insert(HousekeepingTaskEntity(hotelId = sheger, roomId = roomOf(1, 3), type = TaskType.CLEANING, status = TaskStatus.IN_PROGRESS, note = "Departure clean", assignedTo = hkId))
            db.tasks().insert(HousekeepingTaskEntity(hotelId = sheger, roomId = roomOf(0, 7), type = TaskType.MAINTENANCE, note = "Air conditioner not cooling"))
            db.tasks().insert(HousekeepingTaskEntity(hotelId = sheger, roomId = roomOf(3, 0), type = TaskType.INSPECTION, note = "VIP arrival – suite inspection, welcome coffee & flowers", highPriority = true, assignedTo = hkId))
            db.tasks().insert(HousekeepingTaskEntity(hotelId = sheger, roomId = roomOf(1, 1), type = TaskType.TURNDOWN, note = "Evening turndown"))

            // Notifications
            suspend fun n(u: Long, t: NotificationType, a1: String = "", a2: String = "") = db.notifications().insert(NotificationEntity(userId = u, type = t, arg1 = a1, arg2 = a2))
            n(clientId, NotificationType.WELCOME, "Hana Alemu")
            n(clientId, NotificationType.REVIEW_REMINDER, "", hotels[4].name)
            n(touristId, NotificationType.WELCOME, "Emma Clarke")
            n(touristId, NotificationType.BOOKING_CONFIRMED, db.bookings().byId(emmaArrival)!!.reference, hotels[0].name)
            n(managerId, NotificationType.NEW_RESERVATION, "", guestPool[10].first)
            n(adminId, NotificationType.HOTEL_REGISTERED, hotels.last().name, "Gemechu Dibaba")
            n(adminId, NotificationType.ID_SUBMITTED, "Emma Clarke")
        }
    }
}
