package et.hotelbooking.app.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

data class AppLanguage(val tag: String, val nativeName: String, val englishName: String, val local: Boolean)

object Languages {
    val all = listOf(
        AppLanguage("am", "አማርኛ", "Amharic", true),
        AppLanguage("om", "Afaan Oromoo", "Afaan Oromo", true),
        AppLanguage("ti", "ትግርኛ", "Tigrinya", true),
        AppLanguage("en", "English", "English", false),
        AppLanguage("ar", "العربية", "Arabic", false),
        AppLanguage("fr", "Français", "French", false),
        AppLanguage("zh-CN", "简体中文", "Chinese (Simplified)", false),
        AppLanguage("es", "Español", "Spanish", false),
    )
    fun current(): String {
        val l = AppCompatDelegate.getApplicationLocales()
        return if (l.isEmpty) "" else l[0]!!.toLanguageTag()
    }
    fun isCurrent(tag: String): Boolean {
        val cur = current(); if (cur.isEmpty()) return false
        return cur.equals(tag, true) || cur.substringBefore('-').equals(tag.substringBefore('-'), true)
    }
    /** Uses AndroidX per-app language API (works on API 26+, maps to system per-app languages on Android 13+). */
    fun apply(tag: String) = AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
}
