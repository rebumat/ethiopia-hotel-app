"""Generates docs/Ethiopia_Hotel_Booking_App_Requirements_v2.pdf (reportlab)."""
import os
from reportlab.lib.pagesizes import A4
from reportlab.lib import colors
from reportlab.lib.units import mm
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (BaseDocTemplate, PageTemplate, Frame, Paragraph, Spacer, Table, TableStyle, PageBreak,
                                Image, KeepTogether, ListFlowable, ListItem)

HERE = os.path.dirname(os.path.abspath(__file__))
OUT = os.path.join(HERE, "Ethiopia_Hotel_Booking_App_Requirements_v2.pdf")
SHOTS = os.path.join(HERE, "..", "screenshots")
GREEN = colors.HexColor("#0B6B45"); GOLD = colors.HexColor("#E0A526"); RED = colors.HexColor("#B83A2E"); INK = colors.HexColor("#1D1B16")
LIGHT = colors.HexColor("#F3EFE4")

ss = getSampleStyleSheet()
H1 = ParagraphStyle("H1", parent=ss["Heading1"], fontName="Helvetica-Bold", fontSize=16, textColor=GREEN, spaceBefore=10, spaceAfter=6)
H2 = ParagraphStyle("H2", parent=ss["Heading2"], fontName="Helvetica-Bold", fontSize=12.5, textColor=INK, spaceBefore=8, spaceAfter=4)
B = ParagraphStyle("B", parent=ss["BodyText"], fontName="Helvetica", fontSize=9.6, leading=13.4, spaceAfter=4)
SM = ParagraphStyle("SM", parent=B, fontSize=8.4, leading=11)
CELL = ParagraphStyle("CELL", parent=B, fontSize=8.4, leading=10.8, spaceAfter=0)
CELLB = ParagraphStyle("CELLB", parent=CELL, fontName="Helvetica-Bold", textColor=colors.white)
NOTE = ParagraphStyle("NOTE", parent=B, backColor=LIGHT, borderPadding=6, leftIndent=4, rightIndent=4, spaceBefore=4, spaceAfter=8)

def P(t, s=B): return Paragraph(t, s)
def bullets(items, s=B):
    return ListFlowable([ListItem(P(i, s), leftIndent=10, value="•") for i in items], bulletType="bullet", start="•", leftIndent=12, bulletFontSize=8)
def table(rows, widths, head=True):
    data = [[P(c, CELLB if (head and r == 0) else CELL) for c in row] for r, row in enumerate(rows)]
    t = Table(data, colWidths=widths, repeatRows=1 if head else 0)
    st = [("GRID", (0, 0), (-1, -1), 0.4, colors.HexColor("#CFC8B5")), ("VALIGN", (0, 0), (-1, -1), "TOP"),
          ("TOPPADDING", (0, 0), (-1, -1), 3), ("BOTTOMPADDING", (0, 0), (-1, -1), 3)]
    if head: st += [("BACKGROUND", (0, 0), (-1, 0), GREEN)]
    for r in range(1 if head else 0, len(rows)):
        if r % 2 == 0: st.append(("BACKGROUND", (0, r), (-1, r), colors.HexColor("#FAF8F2")))
    t.setStyle(TableStyle(st)); return t

def deco(canvas, doc):
    canvas.saveState()
    w, h = A4
    # tibeb-like strip
    seg = w / 30
    for i in range(30):
        canvas.setFillColor([GREEN, GOLD, RED][i % 3]); canvas.rect(i * seg, h - 7, seg, 7, stroke=0, fill=1)
    canvas.setFont("Helvetica", 7.5); canvas.setFillColor(colors.grey)
    canvas.drawString(18 * mm, 10 * mm, "Ethiopia Hotel Booking App - Requirements v2.0 (mobile-first)")
    canvas.drawRightString(w - 18 * mm, 10 * mm, "Page %d" % doc.page)
    canvas.restoreState()

def cover(canvas, doc):
    w, h = A4
    canvas.saveState()
    canvas.setFillColor(GREEN); canvas.rect(0, h * 0.45, w, h * 0.55, stroke=0, fill=1)
    seg = w / 24
    for i in range(24):
        canvas.setFillColor([GREEN, GOLD, RED][i % 3] if i % 3 else GOLD); canvas.rect(i * seg, h * 0.45 - 9, seg, 9, stroke=0, fill=1)
    canvas.setFillColor(GOLD); canvas.setFont("Helvetica-Bold", 12); canvas.drawString(20 * mm, h - 40 * mm, "PROJECT REQUIREMENTS - VERSION 2.0")
    canvas.setFillColor(colors.white); canvas.setFont("Helvetica-Bold", 30)
    canvas.drawString(20 * mm, h - 62 * mm, "Ethiopia Hotel Booking"); canvas.drawString(20 * mm, h - 76 * mm, "Mobile App")
    canvas.setFont("Helvetica", 13)
    canvas.drawString(20 * mm, h - 92 * mm, "Android app for hotel discovery, comparison, reservation,")
    canvas.drawString(20 * mm, h - 99 * mm, "check-in/check-out and hotel management (PMS)")
    canvas.setFillColor(INK); canvas.setFont("Helvetica", 10.5)
    y = h * 0.45 - 30 * mm
    for line in ["Revised from the original web-based concept document (v1.0).",
                 "Adapted to mobile-first, with gaps corrected and new modules added.",
                 "", "Date: 15 November 2026 (Hidar 6, 2019 E.C.)",
                 "Companion build: Ethio Hotels Android app 1.0.0-demo",
                 "", "Status: draft for stakeholder review. Sample data only; not legal advice."]:
        canvas.drawString(20 * mm, y, line); y -= 6.5 * mm
    canvas.restoreState()

doc = BaseDocTemplate(OUT, pagesize=A4, leftMargin=18 * mm, rightMargin=18 * mm, topMargin=18 * mm, bottomMargin=18 * mm,
                      title="Ethiopia Hotel Booking App - Requirements v2.0", author="Ethio Hotels project")
frame = Frame(doc.leftMargin, doc.bottomMargin, doc.width, doc.height)
doc.addPageTemplates([PageTemplate(id="cover", frames=[frame], onPage=cover), PageTemplate(id="body", frames=[frame], onPage=deco)])
W = doc.width
s = []
from reportlab.platypus.doctemplate import NextPageTemplate
s += [NextPageTemplate("body"), PageBreak()]

# Contents
s += [P("Contents", H1)]
toc = ["1. Project overview (mobile-first)", "2. Objectives", "3. Users, roles and permissions", "4. User modes: Local and Tourist",
       "5. Languages and localization", "6. Home screen and Ethiopian landmarks", "7. Search, hotel details and comparison",
       "8. Booking workflow and status lifecycle", "9. Pricing, taxes and currency", "10. Cancellation and refund rules",
       "11. Payments", "12. Check-in, check-out and hotel management (PMS)", "13. Identity documents and verification",
       "14. Reviews and ratings", "15. Notifications", "16. Data protection and privacy", "17. Offline behaviour",
       "18. Non-functional requirements", "19. Architecture, technology and API", "20. Development phases",
       "21. Demo build: what is real and what is simulated", "22. Change log against the original document", "Appendix A. Screens from the demo build"]
s += [bullets(toc, SM), PageBreak()]

s += [P("1. Project overview (mobile-first)", H1),
      P("The Ethiopia Hotel Booking App is a native Android application that helps tourists, business travellers and residents find, compare "
        "and book hotels across Ethiopia, and gives hotels the tools to run reservations, check-in, check-out, rooms and housekeeping. "
        "The original concept (v1.0) described a responsive website. This version makes the <b>mobile app the primary product</b>: most "
        "travellers in Ethiopia book and pay from a phone with mobile money (Telebirr, CBE Birr), and hotel front desks benefit from a "
        "phone or tablet they can carry around the property."),
      P("The same backend (Django REST Framework and PostgreSQL) can later serve a web client for administrators and large hotels. "
        "The app uses one codebase with role-based workspaces: Client, Hotel Manager, Receptionist, Housekeeping and Administrator."),
      P("Implementation note retained from v1.0: hotel listings, star classifications, prices, availability and reviews must be verified and kept "
        "up to date. The platform must not imply that it lists every hotel in Ethiopia until onboarding and verification are sufficiently complete.", NOTE)]

s += [P("2. Objectives", H1), bullets([
    "Provide one trusted mobile platform to find hotels throughout Ethiopia, before and during travel.",
    "Help international visitors evaluate hotels and discover Ethiopian landmarks, in their own language and currency.",
    "Serve local travellers with Birr prices, mobile-money payments, Ethiopian calendar dates and resident rates.",
    "Let users compare hotels by price, location, quality, facilities and verified ratings.",
    "Provide a clear reservation process with an explicit status lifecycle and a QR code for fast check-in.",
    "Give hotels a lightweight property management system (PMS): front desk, rooms, housekeeping, rates, guests, staff and reports.",
    "Give administrators control over hotel approvals, users, identity verification and platform statistics.",
    "Promote Ethiopian hotels and tourism digitally, while protecting personal data under Ethiopian law."])]

s += [P("3. Users, roles and permissions", H1),
      P("v1.0 defined three roles. Front-desk operations need finer control, so the hotel side is split into three staff roles. "
        "All staff accounts belong to exactly one hotel and only see that hotel's data."),
      table([["Role", "Main responsibilities"],
             ["Client (traveller)", "Register with an ID document, search, compare, book, pay, cancel within policy, show the check-in QR, review after check-out, receive notifications."],
             ["Hotel Manager", "Everything a receptionist can do, plus hotel profile, room inventory, room types and rates, seasons, staff accounts and permissions, reports and invoices."],
             ["Receptionist (front desk)", "Today's arrivals, departures and in-house guests; confirm or reject requests; check-in (QR or reference, ID, room, deposit); folio and check-out; walk-ins; guest profiles; calendar; room status."],
             ["Housekeeping", "Task board (cleaning, inspection, maintenance, turndown); take, start and finish tasks; set rooms to Cleaning, Available or Maintenance."],
             ["Administrator", "Approve, reject, suspend or reinstate hotels; activate or deactivate users; review identity documents (verify or reject); view all reservations and platform statistics."]],
            [38 * mm, W - 38 * mm]),
      Spacer(1, 6), P("3.1 Permission matrix (staff)", H2),
      table([["Permission", "Manager", "Receptionist", "Housekeeping"],
             ["Front desk, check-in, check-out, walk-in", "Yes", "Yes", "-"],
             ["Confirm or reject reservations, mark no-show", "Yes", "Yes", "-"],
             ["Guest profiles and occupancy calendar", "Yes", "Yes", "-"],
             ["View rooms and update room status", "Yes", "Yes", "Yes"],
             ["Housekeeping task board", "Yes", "Yes (create, assign)", "Yes (own and unassigned tasks)"],
             ["Add or delete rooms, edit room types and rates, seasons", "Yes", "-", "-"],
             ["Hotel profile, staff accounts, reports", "Yes", "-", "-"]],
            [W - 3 * 30 * mm, 30 * mm, 30 * mm, 30 * mm])]

s += [P("4. User modes: Local and Tourist", H1),
      P("Travellers choose a mode during onboarding and can change it at any time in Profile. The mode only changes defaults and emphasis; "
        "every feature stays available in both modes."),
      table([["", "Local (Ethiopian resident)", "Tourist / international"],
             ["Currency", "Prices in ETB first.", "USD or EUR first, with ETB shown alongside. The conversion uses a labelled sample rate (NBE, 13 Nov 2026: 1 USD = 161.19 ETB, 1 EUR = 179.61 ETB). The charge is always settled in ETB."],
             ["Payment order", "Telebirr, CBE Birr, bank transfer, then cards and pay at hotel.", "Visa and Mastercard first, then mobile money, bank and pay at hotel."],
             ["Languages", "Amharic and Afaan Oromo suggested first (Tigrinya also offered).", "World languages first: English, French, Arabic, Chinese, Spanish."],
             ["Calendar", "Ethiopian calendar shown next to Gregorian dates by default.", "Gregorian only by default (Ethiopian dates optional)."],
             ["Phone format", "+251 9XX XXX XXX validation.", "International format with country code."],
             ["Identity", "Fayda national ID or kebele ID (front and back).", "Passport photo page, passport number, nationality and expiry."],
             ["Home emphasis", "Local perks (mobile money, resident rates), weekend getaways near Addis Ababa.", "Large landmark cards, a travel essentials card (e-Visa at evisa.gov.et, currency, power, Ethiopian time and calendar, health) and top-rated hotels."],
             ["Rates", "Resident rate where the hotel offers dual pricing.", "Foreigner (non-resident) rate where the hotel offers dual pricing."]],
            [24 * mm, (W - 24 * mm) / 2, (W - 24 * mm) / 2]),
      P("Resident rates require an Ethiopian ID at check-in. The front desk can switch the rate if the documents presented do not match.", NOTE)]

s += [P("5. Languages and localization", H1),
      P("All interface text comes from Android string resources, so new languages need no code changes. The language picker appears in "
        "onboarding and Profile and uses Android per-app language settings (Android 13+ system settings also list the app)."),
      table([["Language", "Script", "Notes"],
             ["English", "Latin", "Default and fallback."],
             ["Amharic (አማርኛ)", "Ethiopic", "Federal working language. The Noto Sans Ethiopic font is bundled so text renders on every device."],
             ["Afaan Oromo", "Latin (Qubee)", "Most widely spoken language in Ethiopia. Dates fall back to English month names where the OS lacks Oromo data."],
             ["Tigrinya (ትግርኛ)", "Ethiopic", "Core screens translated by hand; remaining strings machine-translated and flagged for native review."],
             ["Arabic", "Arabic", "Full right-to-left layout mirroring."],
             ["French, Chinese (Simplified), Spanish", "Latin / Han", "Machine-assisted translations; native review required before release."]],
            [44 * mm, 26 * mm, W - 70 * mm]),
      bullets(["Hotel-entered content (hotel names, descriptions, policies) is shown as entered. A later phase can store per-language descriptions.",
               "City, region and landmark names and descriptions are localized.",
               "Notifications are stored as a type plus arguments and rendered in the reader's current language.",
               "Amounts use locale-aware formatting. Ethiopian calendar dates use localized month names (Amharic, Tigrinya, Oromo, English).",
               "Translation quality gate: every release candidate must be reviewed by native Amharic and Afaan Oromo speakers."])]

s += [P("6. Home screen and Ethiopian landmarks", H1),
      P("The home screen combines search with destination discovery:"),
      bullets(["Hero image (Lalibela for Local mode, Simien Mountains for Tourist mode) with a greeting and today's date in both calendars.",
               "Search card: city or region, check-in and check-out dates, number of guests.",
               "<b>Discover Ethiopia</b>: a horizontally scrolling carousel of 13 landmarks. Each card opens a detail page with a description, a UNESCO badge where applicable, and a 'Hotels near ...' shortcut.",
               "Mode-dependent card (Local perks or Travel essentials), popular cities with hotel counts, featured hotels, then weekend getaways or top-rated hotels.",
               "A compare bar appears once hotels are selected for comparison."]),
      table([["Landmark", "Nearest city", "UNESCO"],
             ["Rock-Hewn Churches, Lalibela", "Lalibela", "Yes"], ["Aksum Obelisks", "Axum", "Yes"], ["Fasil Ghebbi, Gondar", "Gondar", "Yes"],
             ["Simien Mountains", "Gondar", "Yes"], ["Harar Jugol", "Harar", "Yes"], ["Konso Cultural Landscape", "Arba Minch", "Yes"],
             ["Tiya Stelae", "Addis Ababa", "Yes"], ["Bale Mountains", "(Bale)", "Yes (2023)"], ["Lake Tana Monasteries", "Bahir Dar", "Biosphere reserve"],
             ["Blue Nile Falls (Tis Abay)", "Bahir Dar", "-"], ["Danakil Depression (Dallol)", "Mekelle", "-"], ["Erta Ale", "Mekelle", "-"], ["Addis Ababa", "Addis Ababa", "-"]],
            [70 * mm, 50 * mm, W - 120 * mm]),
      P("Photos come from Wikimedia Commons under their stated licences (CC BY, CC BY-SA or public domain). The in-app Credits screen lists author and licence for every image. "
        "A production version should use photos licensed or supplied by hotels and the tourism authorities.", SM)]

s += [P("7. Search, hotel details and comparison", H1), P("7.1 Search and filters", H2),
      bullets(["Free text (city, region, hotel name and common alternative city names, e.g. Finfinne), city or region, dates, guests.",
               "Price range per night, star classification, minimum guest rating (3.5+, 4.0+, 4.5+) and facilities (18, including coffee ceremony, airport shuttle and lake or mountain view).",
               "Sort by recommended, price (low or high), rating or stars. When dates are set, only hotels with a room free for the whole stay are shown, with 'Only N left' when inventory is low.",
               "Only hotels with status Approved are visible to clients."]),
      P("7.2 Hotel details", H2),
      bullets(["Swipeable photo gallery, stars, verified rating and review count, address, Open in Maps and Call buttons.",
               "Facilities with icons, room types with capacity, bed type, size, price per night, resident and foreigner rates, and live availability for the chosen dates.",
               "Seasonal pricing notes, policies (check-in and check-out times, free-cancellation window, children and other policies).",
               "Verified reviews only, with a 'Verified stay' badge."]),
      P("7.3 Comparison", H2),
      P("Up to three hotels can be compared side by side: city, distance from the city centre, stars, rating and reviews, lowest price (best price highlighted), "
        "free-cancellation window, room types and key facilities. The comparison list persists while browsing.")]

s += [P("8. Booking workflow and status lifecycle", H1), P("8.1 Client booking steps (mobile wizard)", H2),
      bullets(["<b>Room</b>: choose room type and dates; live availability and nightly price.",
               "<b>Guest</b>: name, phone (+251 format for locals), email, nationality, ID type and number (pre-filled from the ID on file), number of guests, special requests, resident rate where applicable.",
               "<b>Review</b>: price breakdown (room nights, seasonal adjustments, 10% service charge, 15% VAT), total in ETB with an approximate USD or EUR amount, cancellation policy.",
               "<b>Payment</b>: pay now (Telebirr, CBE Birr, Visa, Mastercard), bank transfer (pending verification) or pay at hotel.",
               "Confirmation: booking reference (EHB-XXXXXX), status, payment status and the check-in QR code."]),
      P("8.2 Status lifecycle (v1.0 only said 'confirmation or status notification')", H2),
      table([["Status", "Meaning", "Set by", "Next states"],
             ["Pending", "Request submitted; inventory is held.", "Client", "Confirmed, Rejected, Cancelled"],
             ["Confirmed", "Hotel accepted; room assigned automatically or at check-in.", "Manager or receptionist", "Checked-in, Cancelled, No-show"],
             ["Checked-in", "Guest arrived; ID verified; room occupied; folio open.", "Front desk", "Checked-out"],
             ["Checked-out / Completed", "Folio settled and invoice issued; review unlocked.", "Front desk", "(final)"],
             ["Cancelled", "Cancelled by the guest within policy, or by the hotel.", "Client or hotel", "(final)"],
             ["Rejected", "Hotel declined the request (e.g. overbooked).", "Manager or receptionist", "(final)"],
             ["No-show", "Guest did not arrive by the end of the arrival day.", "Front desk", "(final)"]],
            [30 * mm, 62 * mm, 34 * mm, W - 126 * mm]),
      bullets(["Pending, Confirmed and Checked-in bookings block inventory. Availability is calculated per night as the peak overlapping count against total rooms of that type.",
               "Pending requests should be answered within a service level (proposal: 12 hours), after which the client is notified and may rebook.",
               "Every transition creates a notification for the guest and, where relevant, the hotel staff."], SM)]

s += [P("9. Pricing, taxes and currency", H1),
      table([["Element", "Rule"],
             ["Base rate", "Per room type per night. With dual pricing enabled, a separate foreigner (non-resident) rate applies; otherwise one rate for all."],
             ["Seasons", "Date ranges (e.g. Timket, Meskel, Christmas and New Year peaks, rainy-season discounts) with a percentage adjustment applied per night."],
             ["Service charge", "10% of room charges (configurable per hotel in production)."],
             ["VAT", "15% applied on room charges plus service charge, per Ethiopian VAT."],
             ["Currency", "All bookings, folios and invoices are settled in ETB. USD and EUR are shown for information using a dated, labelled rate; production should fetch daily NBE rates."],
             ["Extras", "Minibar, restaurant, laundry, spa, transport and other charges posted to the folio during the stay."]],
            [32 * mm, W - 32 * mm])]

s += [P("10. Cancellation and refund rules", H1),
      bullets(["Each hotel sets a free-cancellation window in days before check-in (default 2 days).",
               "Cancelled on or before the window: no fee; full refund of any amount paid.",
               "Cancelled inside the window: a fee equal to the first night (including service charge and VAT); the rest is refunded.",
               "The app shows the fee and refund before the guest confirms the cancellation.",
               "Refunds go back to the original payment method (wallet, card or bank). Payment status becomes Refunded or Partly refunded.",
               "No-shows are charged under the late-cancellation rule. Checked-in and completed stays cannot be cancelled in the app.",
               "A hotel cancelling a confirmed booking must refund in full and should help with rebooking."])]

s += [P("11. Payments", H1),
      table([["Method", "Flow in production", "Demo build"],
             ["Telebirr", "Merchant API (H5 or USSD push): the server creates the order, the user approves on the phone, the server confirms via callback.", "Simulated approval dialog."],
             ["CBE Birr", "CBE Birr merchant integration with a push or redirect flow and server callback.", "Simulated."],
             ["Bank transfer", "Guest transfers with the booking reference; the hotel or finance team verifies; status Pending verification, then Paid.", "Manual verify button for staff."],
             ["Visa / Mastercard", "Licensed payment gateway with hosted card fields or tokenization and 3-D Secure. The app never sees or stores full card numbers.", "Card brand detected from the number (Visa 4..., Mastercard 51-55 / 2221-2720), Luhn check, expiry and CVV validation; only brand and last 4 digits are kept."],
             ["Cash / pay at hotel", "Recorded by the front desk at check-in or check-out.", "Recorded."]],
            [30 * mm, 86 * mm, W - 116 * mm]),
      bullets(["PCI DSS: use the gateway's SDK or hosted page so card data never touches app storage or our servers (SAQ A scope).",
               "The client can never mark a booking as paid: only server-side webhooks change payment status.",
               "Store transaction IDs, amounts, method, time and reconciliation status; produce daily settlement reports per hotel.",
               "Card payments in foreign currency settle in ETB under National Bank of Ethiopia rules; show the ETB amount charged."], SM)]

s += [P("12. Check-in, check-out and hotel management (PMS)", H1),
      P("v1.0 covered only reservation confirmation. Hotels also need daily operations, so a PMS module is added for the Manager, Receptionist and Housekeeping roles."),
      P("12.1 Front desk", H2), bullets(["Dashboard with arrivals, departures, in-house guests, occupancy now, pending requests and overdue arrivals.",
                                          "Quick actions: find booking (QR scan or reference), walk-in, tasks, calendar, guests, rates, reports, staff, hotel profile."]),
      P("12.2 Check-in", H2), bullets(["Find the booking by scanning the guest's QR code (payload EHB-CHECKIN:&lt;reference&gt;) or typing the reference.",
                                        "Verify identity: ID type and number are pre-filled from the ID the guest uploaded at registration; the receptionist compares it with the physical document.",
                                        "Assign a free room number (the system suggests one), take a deposit (cash, Telebirr, CBE Birr, card) and complete check-in. The room becomes Occupied.",
                                        "Check-in is allowed from the arrival date. Earlier check-in needs a date change."]),
      P("12.3 Stay and check-out", H2), bullets(["Folio: room charges, service charge, VAT and posted extras (minibar, restaurant, laundry, spa, transport, other) with add and remove.",
                                                 "Check-out: review the folio, see the balance (or refund due) in ETB and USD, settle with any method, add optional guest feedback.",
                                                 "On completion: an invoice PDF is generated and shareable, the room changes to Cleaning, a housekeeping task is created, and the guest is asked to review."]),
      P("12.4 Walk-ins", H2), P("Create a booking and check in at once: room type, nights, free room, guest details and ID, resident rate, payment. Source is recorded as Walk-in."),
      P("12.5 Rooms and housekeeping", H2),
      table([["Room status", "Meaning"], ["Available", "Clean and free."], ["Reserved", "Assigned to a confirmed arrival today (derived)."],
             ["Occupied", "Guest checked in. Released only by check-out."], ["Cleaning", "Departure or service clean in progress."], ["Maintenance", "Out of order (e.g. AC repair)."]],
            [32 * mm, W - 32 * mm]),
      bullets(["Housekeeping board with To do, In progress and Done columns, priority flag, assignee and notes. Housekeepers can take unassigned tasks.",
               "Finishing a cleaning or inspection task sets the room back to Available."], SM),
      P("12.6 Rates, guests, staff, calendar and reports", H2),
      bullets(["Rates: edit room-type prices and foreigner rates, open or close a room type for sale, toggle dual pricing, add or remove seasons.",
               "Guest profiles: grouped stay history, nights, total spent and a repeat-guest badge.",
               "Staff: create receptionist and housekeeping accounts with a temporary password; activate or deactivate; permission matrix shown.",
               "Occupancy calendar: rooms × 14-day grid coloured by status; tap a block to open the booking.",
               "Reports for 7, 30, 90 or 180 days: occupancy, ADR, RevPAR, room revenue, extras revenue, room nights, occupancy trend, revenue by month, bookings by source and status."]),
      P("KPI definitions: Occupancy = room nights sold ÷ room nights available. ADR = room revenue ÷ room nights sold. RevPAR = room revenue ÷ room nights available (= ADR × occupancy).", NOTE)]

s += [P("13. Identity documents and verification", H1),
      table([["", "Tourist", "Local resident"],
             ["Document", "Passport photo page", "Fayda national ID or kebele ID (driving licence accepted at the desk)"],
             ["Fields", "Passport number, nationality, expiry date (must be in the future)", "ID number"],
             ["Images", "Front (photo page)", "Front and back"]],
            [24 * mm, (W - 24 * mm) / 2, (W - 24 * mm) / 2]),
      bullets(["Capture with the camera or pick from the gallery, with preview and retake.",
               "An explicit consent checkbox explains why the ID is needed (identity verification and guest registration) and links to the privacy notice. Registration cannot finish without consent. The consent time is stored.",
               "Status: Not submitted, Pending review, Verified or Rejected. Administrators review the images and details and verify or reject; the user is notified and can re-upload.",
               "The ID on file pre-fills the booking and check-in steps; the front desk still checks the physical document.",
               "Images are stored only in app-private storage (never the shared gallery) and are excluded from cloud backup and device transfer. Numbers are masked in the interface except the last four characters.",
               "Production: upload over TLS to encrypted object storage in Ethiopia, with access limited to administrators and the booked hotel's front desk and every access logged. Delete or archive images after a defined retention period (proposal: 12 months after the last stay, unless the law requires longer). Consider Fayda's official verification services instead of storing images where possible."])]

s += [P("14. Reviews and ratings", H1),
      bullets(["Only the guest of a booking with status Checked-out can review it, once per booking (enforced on the server).",
               "Rating 1-5 with title and comment; shown with a 'Verified stay' badge. The hotel's average rating and count update automatically.",
               "Reviews link to the booking ID for auditability. Moderation tools (report abuse, hotel reply) are planned for a later phase."])]

s += [P("15. Notifications", H1),
      table([["Event", "Recipient"],
             ["Request submitted, confirmed, rejected, cancelled, no-show", "Guest (and hotel staff for new requests and cancellations)"],
             ["Payment received or verified", "Guest"], ["Checked in (with room number), checked out (invoice ready), review reminder", "Guest"],
             ["New hotel registration, ID submitted for review", "Administrators"], ["Hotel approved or suspended", "Hotel manager"],
             ["ID verified or rejected", "User"], ["Welcome", "New user"]],
            [W * 0.6, W * 0.4]),
      P("In the demo, notifications are in-app with an unread badge. Production adds Firebase Cloud Messaging push and SMS for critical events "
        "(confirmation and cancellation), because many users rely on SMS. Users can control non-critical notifications.", SM)]

s += [P("16. Data protection and privacy", H1),
      P("Ethiopia's <b>Personal Data Protection Proclamation No. 1321/2024</b> was adopted in 2024 and is in force. The Ethiopian Communications Authority (ECA) is the "
        "regulator. Requirements relevant to this platform:"),
      bullets(["<b>Registration</b>: the operator (data controller) and processors must register with the ECA before processing personal data.",
               "<b>Lawful basis and consent</b>: explicit, informed consent for identity documents; clear purpose (identity verification, guest registration, fraud prevention).",
               "<b>Data minimisation and retention</b>: collect only what is needed, define retention periods and delete afterwards.",
               "<b>Data subject rights</b>: access, correction, deletion, objection and withdrawal of consent, available from Profile and through support.",
               "<b>Local storage and transfers</b>: keep personal data collected in Ethiopia on servers in Ethiopia; cross-border transfers only under the conditions the law allows.",
               "<b>Security</b>: encryption in transit and at rest, role-based access, audit logs, staff confidentiality.",
               "<b>Breach notification</b>: notify the ECA within 72 hours of becoming aware of a breach, and inform affected users where required.",
               "<b>Sensitive data</b>: ID images and numbers get stricter access controls, and are never used for marketing.",
               "<b>Payments</b>: card data is handled only by the PCI DSS-compliant gateway."]),
      P("How the demo applies this: ID images in private storage, backups excluded, masked numbers, consent recorded with time, passwords hashed with PBKDF2-HMAC-SHA256 "
        "(120,000 iterations, per-user salt), no card numbers stored, and all data stays on the device. Legal counsel must confirm final obligations before launch.", NOTE)]

s += [P("17. Offline behaviour", H1),
      bullets(["Mobile networks can be slow or interrupted, especially outside major cities. The app keeps a local Room (SQLite) database.",
               "Browsing cached hotels, landmarks, saved bookings, the check-in QR code and booking details works offline.",
               "Actions that change state (book, pay, cancel, check-in, check-out) need a connection in production. The app shows a clear offline state and retries safely (idempotency keys) instead of creating duplicates.",
               "Front-desk mode should tolerate short outages: queue folio charges and housekeeping updates locally and sync when back online, with server-side conflict checks.",
               "The demo build runs fully offline with seeded sample data and the same repository interfaces that the REST backend will use."])]

s += [P("18. Non-functional requirements", H1),
      table([["Area", "Requirement"],
             ["Platform", "Android 8.0 (API 26) and newer; phones first, tablets supported. iOS can follow using the same API."],
             ["Performance", "Home and search usable within 2 s on a mid-range phone over 3G; compressed WebP images; paged lists."],
             ["Usability", "Material 3 design with an Ethiopian palette (green, gold, red) and tibeb pattern accents; light and dark themes; empty, loading and error states; large touch targets."],
             ["Accessibility", "Content descriptions, scalable fonts, contrast-checked colours, RTL support."],
             ["Security", "Hashed passwords, JWT with refresh tokens over TLS, role and hotel scoping on every API call, rate limiting, audit trail for staff actions."],
             ["Reliability", "Availability checks in a database transaction to prevent double booking; idempotent payment webhooks."],
             ["Scalability", "Stateless API behind Nginx, PostgreSQL with read replicas, object storage for images."],
             ["Availability", "Daily encrypted backups stored in Ethiopia; recovery tested quarterly."]],
            [30 * mm, W - 30 * mm])]

s += [P("19. Architecture, technology and API", H1),
      table([["Layer", "Technology"],
             ["Android app", "Kotlin, Jetpack Compose, Material 3, MVVM, Navigation Compose, Room, AppCompat per-app languages, ZXing (QR), Android PdfDocument (invoices)"],
             ["Backend / REST API", "Django REST Framework (JWT auth, role permissions), Celery for notifications and payment reconciliation"],
             ["Database", "PostgreSQL"], ["Storage", "S3-compatible object storage hosted in Ethiopia, encrypted"],
             ["Maps", "Map intent from the app (Google Maps or OpenStreetMap apps); in-app map in a later phase"],
             ["Payments", "Telebirr and CBE Birr merchant APIs; licensed card gateway (Visa and Mastercard)"],
             ["Push / SMS", "Firebase Cloud Messaging; local SMS gateway"], ["Deployment", "Linux, Docker, Nginx; CI building signed Android App Bundles"]],
            [36 * mm, W - 36 * mm]),
      P("Main endpoints (v1)", H2),
      table([["Area", "Endpoints"],
             ["Auth", "POST auth/register, POST auth/token, POST auth/token/refresh, GET/PATCH me, POST me/id-document (multipart)"],
             ["Catalogue", "GET hotels (filters, sort), GET hotels/{id}, GET hotels/{id}/room-types?check_in&amp;check_out, GET hotels/{id}/reviews, GET landmarks, GET cities"],
             ["Bookings", "POST bookings/quote, POST bookings, GET bookings/mine, GET bookings/{ref}, POST bookings/{ref}/cancel"],
             ["Payments", "POST payments/intent, POST payments/webhook/{provider}"],
             ["Reviews and notifications", "POST reviews, GET notifications, POST notifications/read-all, POST devices"],
             ["PMS", "GET pms/front-desk, POST pms/bookings/{id}/confirm|reject|no-show|check-in|check-out, POST pms/walk-ins, GET/POST pms/bookings/{id}/folio, GET pms/bookings/{id}/invoice.pdf, CRUD pms/rooms, room-types, seasons, housekeeping-tasks, staff, GET pms/guests, GET pms/reports"],
             ["Admin", "GET admin/stats, GET/PATCH admin/hotels/{id}, GET/PATCH admin/users/{id}, GET admin/bookings"]],
            [34 * mm, W - 34 * mm])]

s += [P("20. Development phases (revised)", H1),
      table([["Phase", "Scope"],
             ["1. MVP (mobile)", "Accounts with ID upload and consent, user modes, languages (EN, AM, OM first), hotel search and details, landmarks, room listings, admin hotel approval."],
             ["2. Booking", "Availability, booking wizard, status lifecycle, cancellation rules, notifications (in-app and push), pay at hotel and bank transfer."],
             ["3. Hotel operations", "PMS: front desk, QR check-in, folio and check-out, invoices, rooms and housekeeping, staff roles, calendar, reports."],
             ["4. Payments and trust", "Telebirr, CBE Birr and card gateway; verified reviews and moderation; ID verification workflow; ECA registration and privacy tooling."],
             ["5. Growth", "Web admin console, iOS app, per-language hotel descriptions, in-app maps, loyalty, channel manager integration."]],
            [36 * mm, W - 36 * mm])]

s += [P("21. Demo build: what is real and what is simulated", H1),
      table([["Real (works in the APK)", "Simulated or sample"],
             ["All screens and workflows for the five roles; booking lifecycle; availability calculation; cancellation fee and refund maths; pricing with seasons, service charge and VAT; resident and foreigner rates; QR generation and scanning; check-in, folio, check-out and PDF invoices; housekeeping; reports; ID capture with private storage, masking and admin review; 8 languages with RTL; Ethiopian calendar; dark mode.",
              "Hotels (20 fictional), prices, reviews and booking history are sample data. Payments are simulated: no money moves and no gateway is contacted. Exchange rates are a fixed, dated sample. Hotel photos are generic illustrative images, not the fictional hotels. Data stays on the device (no server). Push and SMS are not sent."]],
            [W / 2, W / 2])]

s += [P("22. Change log against the original document (v1.0)", H1),
      table([["#", "Area", "v1.0", "v2.0 change"],
             ["1", "Platform", "Web application (React) responsive on mobile", "Native Android app is the primary product; the web admin console comes later on the same API."],
             ["2", "Roles", "Admin, Hotel Manager, Client", "Added Receptionist and Housekeeping staff roles with a permission matrix."],
             ["3", "User modes", "Not covered", "Local and Tourist modes changing currency, payment order, languages, calendar, phone format and home content."],
             ["4", "Languages", "Not covered", "8 languages (EN, AM, OM, TI, AR, FR, ZH, ES), Ethiopic font, RTL, localized notifications."],
             ["5", "Home and landmarks", "Not covered", "Discover Ethiopia carousel (13 landmarks), landmark pages, popular cities, travel essentials with e-Visa link."],
             ["6", "Booking status", "'Confirmation or status notification' only", "Full lifecycle: Pending, Confirmed, Checked-in, Checked-out, Cancelled, Rejected, No-show, with defined transitions."],
             ["7", "Cancellation", "'According to the hotel's policy'", "Concrete free-cancellation window, first-night fee, refund calculation and refund to the original method."],
             ["8", "Pricing", "Room prices only", "Seasonal adjustments, 10% service charge, 15% VAT, resident and foreigner rates, ETB settlement with dated FX display."],
             ["9", "Payments", "'May support' Telebirr, CBE Birr, bank, gateways", "Defined flows for Telebirr, CBE Birr, bank transfer, Visa and Mastercard (brand detection, tokenization, PCI DSS), cash; server-only paid status."],
             ["10", "Hotel operations", "Confirm or reject reservations, booking history", "PMS: front desk, QR check-in, ID check, room assignment, deposit, folio, check-out, invoices, walk-ins, room statuses, housekeeping, rates and seasons, guest profiles, staff, calendar, KPI reports."],
             ["11", "Identity", "Not covered", "ID upload at registration (passport, or Fayda or kebele ID), consent, private storage, masking, admin verification, pre-fill at check-in."],
             ["12", "Reviews", "Linked to completed reservations", "Enforced: only Checked-out bookings, one review per booking, 'Verified stay' badge."],
             ["13", "Notifications", "Module listed only", "Event and recipient table; in-app now, FCM push and SMS in production."],
             ["14", "Security and privacy", "'Protect personal information'", "Proclamation No. 1321/2024 obligations: ECA registration, consent, retention, data subject rights, local storage, 72-hour breach notification; password hashing details."],
             ["15", "Offline", "Not covered", "Local database, offline browsing, cached QR, safe retries, front-desk sync."],
             ["16", "Architecture", "React + DRF + PostgreSQL", "Kotlin and Compose app + DRF + PostgreSQL, documented REST endpoints including PMS and admin."],
             ["17", "Corrections", "Map 'Google Maps or OpenStreetMap'; generic availability", "Availability defined as the per-night peak against room count; map via intent first; listing-verification note retained and extended to ID and payment data."]],
            [7 * mm, 26 * mm, 46 * mm, W - 79 * mm])]

# Appendix screenshots
shots = [("01_home_landmarks.png", "Home with Discover Ethiopia"), ("02_hotel_details.png", "Hotel details (Tourist mode)"),
         ("03_compare_hotels.png", "Compare hotels"), ("04_booking_confirmation.png", "Booking confirmation with QR"),
         ("05_amharic_home.png", "Amharic interface"), ("06_manager_dashboard.png", "Manager dashboard (PMS)"),
         ("07_front_desk.png", "Front desk"), ("08_checkout_folio.png", "Check-out and folio"),
         ("09_reports.png", "Reports"), ("10_admin_id_review.png", "Admin ID review"), ("05c_arabic_rtl_home_tourist.png", "Arabic RTL, Tourist mode")]
avail = [(f, c) for f, c in shots if os.path.exists(os.path.join(SHOTS, f))]
if avail:
    s += [PageBreak(), P("Appendix A. Screens from the demo build", H1), P("Rendered from the app with Robolectric and Roborazzi. Sample data.", SM)]
    cells = []
    for f, c in avail:
        from PIL import Image as PImage
        import tempfile
        src = PImage.open(os.path.join(SHOTS, f)).convert("RGB"); src = src.crop((0, 0, src.width, min(src.height, int(src.width * 1782 / 822))))
        src.thumbnail((500, 1100)); tmp = os.path.join(tempfile.gettempdir(), f + ".jpg"); src.save(tmp, quality=80)
        img = Image(tmp, width=52 * mm, height=52 * mm * src.height / src.width)
        cells.append([img, P(c, SM)])
    rows = []
    for i in range(0, len(cells), 3):
        chunk = cells[i:i + 3]
        rows.append([Table([[x[0]], [x[1]]]) for x in chunk] + [""] * (3 - len(chunk)))
    t = Table(rows, colWidths=[W / 3] * 3); t.setStyle(TableStyle([("VALIGN", (0, 0), (-1, -1), "TOP"), ("ALIGN", (0, 0), (-1, -1), "CENTER")]))
    s.append(t)

doc.build(s)
print("wrote", OUT)
