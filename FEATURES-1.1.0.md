# New features in 1.1.0

Version 1.1.0 (versionCode 2, in `app/build.gradle.kts`). All four features are
on-device only: no network calls, no new permissions, no change to demo accounts,
demo data or simulated payments. New strings are in `app/src/main/res/values/strings.xml`
(English only — other languages fall back to English for the new text).

## 1. Saved hotels (favourites) — NEW
- **What:** a heart button on every hotel card and on the hotel detail screen
  saves/unsaves a hotel. Saved hotels are listed on a new **Saved** screen
  (Profile → Saved hotels), with the empty state "No saved hotels yet".
  Works without signing in.
- **Storage:** `SharedPreferences` file `ethio_hotels_lists`, key `saved_hotels`
  (comma-separated hotel ids) — `util/UserLists.kt` (`UserListsStore`), exposed
  through `AppContainer.lists` and `AppViewModel.savedIds` / `toggleSaved()`.
  A Room table was not used: saved hotels are a per-device list, not hotel data,
  and the existing `SettingsStore` pattern (SharedPreferences + StateFlow) fits it.
- **Files changed/added:**
  - Added: `app/src/main/java/et/hotelbooking/app/util/UserLists.kt`
  - Added: `app/src/main/java/et/hotelbooking/app/ui/screens/client/SavedScreen.kt`
  - Changed: `ui/components/Common.kt` (`HotelCard`: `saved` / `onToggleSaved` params, heart button),
    `ui/screens/client/HotelDetailScreen.kt` (heart button),
    `ui/screens/client/HomeScreen.kt` + `ui/screens/client/SearchScreen.kt` (heart on cards),
    `ui/nav/AppNav.kt` (route `R0.SAVED`),
    `ui/screens/common/CommonScreens.kt` (`ProfileScreen`: Saved hotels row),
    `ui/vm/AppViewModel.kt` (saved state + toggle), `AppContainer.kt` (lists store),
    `app/src/main/res/values/strings.xml` (new strings).
- **How to test:** open any hotel card on Home or Explore, tap the heart (it turns
  filled); open Profile → Saved hotels and check the hotel is listed; tap the heart
  again to remove it. Close and reopen the app — the saved hotel is still there.
  With no saved hotels, the Saved screen shows "No saved hotels yet".

## 2. Recent searches — NEW
- **What:** the last 5 searches (city, dates, guests, text) are shown as chips at
  the top of the Explore screen. Tapping a chip re-runs that search; **Clear**
  removes all of them. A search is only remembered when it narrows something
  (a city, dates or text) — a blank search is not stored.
- **Storage:** same `UserListsStore` (`recent_searches`, JSON array, newest first).
- **Files changed/added:**
  - Added: `util/UserLists.kt` (`RecentSearch` + `addRecentSearch` / `clearRecentSearches`)
  - Changed: `ui/screens/client/SearchScreen.kt` (recent-search chips + Clear,
    records a search when filters are applied),
    `ui/screens/client/HomeScreen.kt` (records a search when Search is tapped),
    `ui/vm/AppViewModel.kt` (`recentSearches`, `recordRecentSearch`,
    `applyRecentSearch`, `clearRecentSearches`), `app/src/main/res/values/strings.xml`.
- **How to test:** on Home, pick a city (e.g. Addis Ababa) and tap Search; go back
  to Explore — the search appears as a chip at the top. Tap the chip: the same
  city/dates/guests are applied again. Tap Clear: the chips disappear. Do 6
  different searches: only the latest 5 are kept.

## 3. Upcoming trip countdown — NEW
- **What:** on the My Bookings screen, Upcoming tab, a card for the next upcoming
  trip shows the hotel name, check-in/check-out dates and "X days to go"
  ("Check-in today" / "Check-in tomorrow" when it is 0/1 days away), with a
  **View booking** button that opens that booking. If there is no upcoming
  booking (none, or all check-ins have passed), no card is shown — no fake booking.
- **Files changed:**
  - Changed: `ui/screens/client/BookingsScreens.kt` (`MyBookingsScreen`: next-trip
    card, computed from active bookings with `checkIn >= today`),
    `app/src/main/res/values/strings.xml` (`next_trip`, `checkin_today`,
    `checkin_tomorrow`, `days_to_go` plural).
- **How to test:** sign in as the demo client (`client@ethiohotels.demo` /
  `Client@123`), make a booking for a future date, then open My Bookings →
  Upcoming: the countdown card is at the top with the correct number of days.
  Tap View booking — the booking detail opens. Cancel that booking (or use an
  account with no bookings): the card disappears.

## 4. Leave a review — ALREADY PRESENT, no change
- The requested review feature already exists in full in the app, so it was **not**
  re-added. Evidence:
  - `HotelDetailScreen.kt`: "Write a review" button (shown only when the signed-in
    user has a checked-out booking for that hotel), `ReviewDialog` with a 1–5 star
    picker, title and comment fields.
  - `ClientViewModels.kt` (`HotelDetailViewModel`): `reviewable` / `refreshReviewable` /
    `submitReview`; `LocalRepositories.kt` `addReview()`: only a booking that belongs
    to the user, is `CHECKED_OUT` and has not been reviewed yet may be reviewed
    (one review per stay); the review is inserted as a Room `ReviewEntity` and the
    hotel's `ratingAvg` / `reviewCount` are recomputed from all its reviews.
  - Reviews are listed on the hotel detail screen with the average rating badge.
- **How to test (unchanged behaviour):** as a demo client, check out a stay
  (or use a checked-out booking), open that hotel, tap Write a review, give stars
  and a comment — the review appears in the list and the average rating updates.
- **Substitute feature added instead — Recently viewed hotels:**
  - **What:** hotels you open are remembered (last 8, most recent first) and shown
    in a "Recently viewed" row on the Home screen, below Featured hotels.
  - **Storage:** same `UserListsStore` (`recently_viewed`, comma-separated ids).
  - **Files changed/added:**
    - Added: `util/UserLists.kt` (`recordViewed`)
    - Changed: `ui/screens/client/HotelDetailScreen.kt` (records a view on open),
      `ui/screens/client/HomeScreen.kt` ("Recently viewed" row),
      `ui/vm/AppViewModel.kt` (`recentlyViewed`, `recordViewed`),
      `app/src/main/res/values/strings.xml`.
  - **How to test:** open 2–3 different hotel detail screens, go back to Home —
    a "Recently viewed" row appears with those hotels, most recent first. Open a
    hotel twice: it appears once, at the front.

## Build / verification
- `./gradlew assembleDebug` could **not** be run in this VM: there is no JDK here
  (restored Temurin 17 via curl, which works), but the Android SDK manager fails
  because Java cannot tunnel through this VM's egress proxy, and the project
  targets compileSdk 37 / AGP 9.5.1, which is not published in Google's stable
  SDK repository (highest platform listed there is android-36) — so the platform
  cannot be installed here. This is a pre-existing environment limitation, not
  caused by these changes; the project itself is unchanged in that respect.
- Instead verified: every new `R.string` / `R.plurals` reference resolves to a
  string in `values/strings.xml` (script-checked, none missing); brace/paren
  balance is 0 in all 12 changed Kotlin files; `aapt2 compile` passes on the
  changed `strings.xml` (and `colors.xml` / `themes.xml`); all `HotelCard` /
  `ProfileScreen` call sites match their updated signatures; new icons
  (`Icons.Filled.Favorite`, `Icons.Outlined.History`) are in
  material-icons-extended, already a dependency.
- **Manual test on a phone (parent):** run `./gradlew assembleDebug` on a machine
  with JDK 17 + Android SDK (compileSdk 37), install the debug APK, and follow
  the "How to test" steps above for each feature. Check in particular that the
  Saved screen opens from Profile, the countdown card appears only with a future
  booking, and recent-search chips re-apply the right city/dates/guests.
