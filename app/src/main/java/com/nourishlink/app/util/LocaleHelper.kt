package com.nourishlink.app.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Multi-language support (English / isiZulu / Xitsonga). Uses AndroidX's per-app
 * language API so switching takes effect immediately without restarting the app,
 * and persists the choice via AppCompatDelegate on API 33+ / a local preference below it.
 */
object LocaleHelper {

    const val ENGLISH = "en"
    const val ZULU = "zu"
    const val XITSONGA = "ts"

    private const val PREFS_NAME = "nourishlink_locale_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun setLocale(context: Context, languageCode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_LANGUAGE, languageCode).apply()

        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getSavedLanguage(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH

    /** Call once, as early as possible in Application.onCreate(). */
    fun applyPersistedLocale(context: Context) {
        val saved = getSavedLanguage(context)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(saved))
    }
}
