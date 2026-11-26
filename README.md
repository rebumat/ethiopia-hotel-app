# Ethio Hotels: Ethiopia Hotel Booking and Hotel Management (Android)

A native Android app (Kotlin, Jetpack Compose, Material 3) for finding, comparing and booking hotels in Ethiopia, with a built-in hotel management system (PMS) for front desk, housekeeping and managers, and an admin panel.

**Demo build:** all hotels, prices, reviews and availability are sample data. Payments are simulated and no money moves. Data stays on the device.

## What's new in 1.1.0
- **Saved hotels (favourites):** heart button on hotel cards and on the hotel detail screen; saved hotels are listed on a new Saved screen (Profile → Saved hotels) and kept on the device.
- **Recent searches:** the last 5 searches (city, dates, guests) are shown on the Explore screen; tap one to run it again, or Clear to remove them.
- **Upcoming trip countdown:** the My Bookings screen shows a card for the next upcoming trip with days until check-in, hotel name, dates and a View booking button. Nothing is shown when there is no upcoming booking.
- **Recently viewed hotels:** hotels you open are remembered (last 8) and shown in a "Recently viewed" row on the Home screen.
- Reviews were already in the app before 1.1.0: after a stay is checked out, the guest can leave a 1–5 star rating and comment from the hotel detail screen, saved in Room, and the hotel's average rating updates. See `FEATURES-1.1.0.md`.

## Build
```
source ~/android-env.sh          # JDK 17 + Android SDK
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew recordRoborazziDebug   # regenerates screenshots/ (Robolectric, no emulator needed)
python3 docs/build_requirements_pdf.py   # regenerates the requirements PDF (reportlab)
```
Toolchain: AGP 9.5.1 (built-in Kotlin 2.4.21), compileSdk 37, targetSdk 36, minSdk 26, Compose BOM 2026.09.00, Room 2.8.5, Navigation 2.10.2.

## Demo accounts
| Role | Email | Password |
|---|---|---|
| Client, local (ID verified) | client@ethiohotels.demo | Client@123 |
| Client, tourist (passport pending) | tourist@ethiohotels.demo | Tourist@123 |
| Hotel manager (Sheger Grand Hotel) | manager@ethiohotels.demo | Manager@123 |
| Receptionist | reception@ethiohotels.demo | Reception@123 |
| Housekeeping | housekeeping@ethiohotels.demo | House@123 |
| Administrator | admin@ethiohotels.demo | Admin@123 |

The login screen also has a demo-accounts quick fill.

## Structure
- `domain/`: enums (roles, permissions, statuses), pricing and cancellation rules
- `data/local/`: Room entities and DAOs
- `data/repo/`: repository interfaces and local implementations (swap for REST later; see `data/remote/ApiContract.kt`)
- `data/seed/`: 20 fictional hotels, rooms, bookings, users, landmarks, cities, image credits
- `ui/`: theme, components, view models, navigation and screens (client, booking, manager/PMS, admin, common)
- `util/`: Ethiopian calendar, money and FX sample rates, card brand detection, QR, invoice PDF, ID image store, languages, and `UserLists.kt` (saved hotels, recent searches, recently viewed — SharedPreferences, no server)
- `docs/`: revised requirements (v2) PDF and its generator

## Languages
English, Amharic, Afaan Oromo, Tigrinya, Arabic (RTL), French, Chinese (Simplified), Spanish. Translation sources are in `tools/i18n` (generator `gen.py`). Amharic and Afaan Oromo are fully hand-translated. Tigrinya, French, Spanish, Arabic and Chinese cover their core screens by hand and the rest by machine (NLLB-200), so they need native review before release.
