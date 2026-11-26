package et.hotelbooking.app.data.seed

data class ImageCredit(val drawable: String, val subject: String, val fileTitle: String, val author: String, val license: String, val url: String)

/** Attribution for every bundled photo (all from Wikimedia Commons under the listed free licences). Generated from assets-src/credits.json. */
object Credits {
    val images = listOf(
        ImageCredit("lm_lalibela", "Bete Giyorgis, Lalibela", "Bete Giyorgis 03.jpg", "Bernard Gagnon", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:Bete_Giyorgis_03.jpg"),
        ImageCredit("lm_axum", "Aksum stelae", "Obelisk of Aksum Remains2.jpg", "Allamiro", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Obelisk_of_Aksum_Remains2.jpg"),
        ImageCredit("lm_gondar", "Fasil Ghebbi, Gondar", "Fasil Ghebbi (6821473537).jpg", "Martijn.Munneke from Netherlands", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Fasil_Ghebbi_(6821473537).jpg"),
        ImageCredit("lm_simien", "Simien Mountains", "Simien Mountains National Park 08.jpg", "Bernard Gagnon", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:Simien_Mountains_National_Park_08.jpg"),
        ImageCredit("lm_danakil", "Dallol, Danakil Depression", "Dallol, Danakil Depression.jpg", "Barrowbob", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Dallol,_Danakil_Depression.jpg"),
        ImageCredit("lm_blue_nile", "Blue Nile Falls", "Blue Nile Falls-03, by CT Snow.jpg", "CT Snow from Hsinchu, Taiwan", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Blue_Nile_Falls-03,_by_CT_Snow.jpg"),
        ImageCredit("lm_harar", "Harar Jugol", "Scene outside Erer Gate (Where Richard Burton Entered Harar) - Old City (Jugal) - Harar - Ethiopia (8749432475).jpg", "Adam Jones from Kelowna, BC, Canada", "CC BY-SA 2.0", "https://commons.wikimedia.org/wiki/File:Scene_outside_Erer_Gate_(Where_Richard_Burton_Entered_Harar)_-_Old_City_(Jugal)_-_Harar_-_Ethiopia_(8749432475).jpg"),
        ImageCredit("lm_bale", "Ethiopian wolf, Bale Mountains", "Ethiopian wolf, Sanetti Plateau, Bale Mountains National Park (9) (28667276343).jpg", "Richard Mortel from Riyadh, Saudi Arabia", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Ethiopian_wolf,_Sanetti_Plateau,_Bale_Mountains_National_Park_(9)_(28667276343).jpg"),
        ImageCredit("lm_addis", "Addis Ababa", "AddisView.jpg", "DaneyWiki", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:AddisView.jpg"),
        ImageCredit("lm_tiya", "Tiya stelae", "Tiya Stelae Field, ca. 1300 (11) (29095993146).jpg", "Richard Mortel from Riyadh, Saudi Arabia", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Tiya_Stelae_Field,_ca._1300_(11)_(29095993146).jpg"),
        ImageCredit("lm_konso", "Konso terraces", "Agricultural terraces in the Konso Cultural Landscape.jpg", "JosepMGracia", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Agricultural_terraces_in_the_Konso_Cultural_Landscape.jpg"),
        ImageCredit("lm_lake_tana", "Lake Tana", "Fishermen, Lake Tana (2260748777).jpg", "A. Davey from Where I Live Now: Pacific Northwest", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Fishermen,_Lake_Tana_(2260748777).jpg"),
        ImageCredit("lm_erta_ale", "Erta Ale", "Erta-ale lac-de-lave 2001.jpg", "Hervé Sthioul", "CC BY 2.5", "https://commons.wikimedia.org/wiki/File:Erta-ale_lac-de-lave_2001.jpg"),
        ImageCredit("city_addis", "Addis Ababa skyline", "Addis Ababa skyline.jpg", "Simfan34", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:Addis_Ababa_skyline.jpg"),
        ImageCredit("city_bahirdar", "Blue Nile, Bahir Dar", "Blue Nile Bahir Dar.jpg", "Geertchaos", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Blue_Nile_Bahir_Dar.jpg"),
        ImageCredit("city_gondar", "Gondar", "Fasil Ghebbi (6821476713).jpg", "Martijn.Munneke from Netherlands", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Fasil_Ghebbi_(6821476713).jpg"),
        ImageCredit("city_lalibela", "Lalibela", "Ethiopia - sunset at Church of Saint George, Lalibela 01.jpg", "Thomas Fuhrmann", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Ethiopia_-_sunset_at_Church_of_Saint_George,_Lalibela_01.jpg"),
        ImageCredit("city_hawassa", "Lake Hawassa", "Hawassa lake, Ethiopia.jpg", "Tamirat hussen", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Hawassa_lake,_Ethiopia.jpg"),
        ImageCredit("city_arbaminch", "Nechisar, Arba Minch", "Nechisar National Park 01.jpg", "Bernard Gagnon", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:Nechisar_National_Park_01.jpg"),
        ImageCredit("city_diredawa", "Dire Dawa", "MosqueDireDawa.JPG", "Arne Hückelheim", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:MosqueDireDawa.JPG"),
        ImageCredit("city_axum", "Aksum", "Obelisk of Aksum Remains.jpg", "Allamiro", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Obelisk_of_Aksum_Remains.jpg"),
        ImageCredit("city_harar", "Harar", "Grand mosque Jami Mosque or Juma Mesjid, in Harar Built in 12th century.jpg", "Elias Tesfaye (Shagiz Adonay)", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Grand_mosque_Jami_Mosque_or_Juma_Mesjid,_in_Harar_Built_in_12th_century.jpg"),
        ImageCredit("city_adama", "Adama", "Kidus Gebriel Cathedral, Adama.jpg", "mjr74", "CC BY-SA 3.0", "https://commons.wikimedia.org/wiki/File:Kidus_Gebriel_Cathedral,_Adama.jpg"),
        ImageCredit("city_bishoftu", "Bishoftu", "Bishoftu Lake, Ethiopia (51644767671).jpg", "Nina R from Africa", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Bishoftu_Lake,_Ethiopia_(51644767671).jpg"),
        ImageCredit("city_mekelle", "Mekelle", "Mekelle Cross.jpg", "FikreT", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Mekelle_Cross.jpg"),
        ImageCredit("hotel_room1", "Hotel room (illustrative)", "Bed at hotel room at Hotel Palace Tallinn.jpg", "JIP", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Bed_at_hotel_room_at_Hotel_Palace_Tallinn.jpg"),
        ImageCredit("hotel_room2", "Hotel room (illustrative)", "Cozy hotel room with modern furnishings and natural light flowing through the curtains.jpg", "Shixart1985", "CC BY 2.0", "https://commons.wikimedia.org/wiki/File:Cozy_hotel_room_with_modern_furnishings_and_natural_light_flowing_through_the_curtains.jpg"),
        ImageCredit("hotel_suite", "Suite (illustrative)", "Canopy bed of Amantaka Suite in Amantaka luxury Resort & Hotel in Luang Prabang Laos 5x4.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Canopy_bed_of_Amantaka_Suite_in_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos_5x4.jpg"),
        ImageCredit("hotel_lobby1", "Lobby (illustrative)", "Lobby lounge of Amantaka Suite Amantaka luxury Resort & Hotel Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Lobby_lounge_of_Amantaka_Suite_Amantaka_luxury_Resort_%26_Hotel_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_lobby2", "Lobby (illustrative)", "Entrance lobby of Amantaka luxury Resort & Hotel at blue hour in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Entrance_lobby_of_Amantaka_luxury_Resort_%26_Hotel_at_blue_hour_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_lobby3", "Lobby (illustrative)", "Lobby hall with couches and chandelier at The Fullerton Bay Hotel Singapore.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Lobby_hall_with_couches_and_chandelier_at_The_Fullerton_Bay_Hotel_Singapore.jpg"),
        ImageCredit("hotel_pool1", "Pool (illustrative)", "Swimming pool of Khan Pool Suite in Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Swimming_pool_of_Khan_Pool_Suite_in_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_pool2", "Pool (illustrative)", "Swimming pool and terrasse of Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Swimming_pool_and_terrasse_of_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_pool3", "Pool (illustrative)", "Swimming pool and main building of Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Swimming_pool_and_main_building_of_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_restaurant1", "Restaurant (illustrative)", "Restaurant room of Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Restaurant_room_of_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_restaurant2", "Restaurant (illustrative)", "Restaurant table at Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Restaurant_table_at_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_bath", "Bathroom (illustrative)", "Bathroom of Khan Pool Suite in Amantaka luxury Resort & Hotel in Luang Prabang Laos.jpg", "Basile Morin", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:Bathroom_of_Khan_Pool_Suite_in_Amantaka_luxury_Resort_%26_Hotel_in_Luang_Prabang_Laos.jpg"),
        ImageCredit("hotel_resort1", "Lakeside resort (illustrative)", "Langano Lake - African Vacation Resort Club - panoramio.jpg", "Brouk Anteneh", "CC BY 3.0", "https://commons.wikimedia.org/wiki/File:Langano_Lake_-_African_Vacation_Resort_Club_-_panoramio.jpg"),
        ImageCredit("hotel_resort2", "Lakeside (illustrative)", "Lake Kuriftu.jpg", "Mjr74", "CC BY 4.0", "https://commons.wikimedia.org/wiki/File:Lake_Kuriftu.jpg"),
        ImageCredit("hotel_building", "Hotel building (illustrative)", "Kühlungsborn, Morada Resort -- 2024 -- 4930.jpg", "Dietmar Rabich", "CC BY-SA 4.0", "https://commons.wikimedia.org/wiki/File:K%C3%BChlungsborn,_Morada_Resort_--_2024_--_4930.jpg"),
    )
    val fonts = listOf(
        "Noto Sans Ethiopic — Google / Noto project — SIL Open Font License 1.1",
        "Playfair Display — Claus Eggers Sørensen — SIL Open Font License 1.1",
    )
    val libraries = listOf(
        "AndroidX, Jetpack Compose, Material 3, Room, Navigation — Apache License 2.0",
        "ZXing (QR codes) and zxing-android-embedded — Apache License 2.0",
        "Kotlin & kotlinx.coroutines — Apache License 2.0",
    )
}
