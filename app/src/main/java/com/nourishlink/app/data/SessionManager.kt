package com.nourishlink.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nourishlink.app.data.model.UserDto
import com.google.gson.Gson

/**
 * Remembers who is signed in, so the session survives app restarts. The cached profile is kept in
 * an encrypted preferences file (AES256-GCM via Jetpack Security) rather than plain text.
 */
class SessionManager(private val context: Context) {

    private val gson = Gson()

    private val prefs: SharedPreferences by lazy { createPrefs() }

    var currentUser: UserDto?
        get() = prefs.getString(KEY_USER, null)?.let {
            try {
                gson.fromJson(it, UserDto::class.java)
            } catch (e: Exception) {
                null
            }
        }
        set(value) = prefs.edit().putString(KEY_USER, value?.let { gson.toJson(it) }).apply()

    var languageCode: String
        get() = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    val isLoggedIn: Boolean get() = currentUser != null

    fun clear() {
        prefs.edit().remove(KEY_USER).apply()
    }

    /**
     * EncryptedSharedPreferences can throw if the Android Keystore key and the saved file get out of
     * sync (e.g. after a reinstall/restore, or on some emulators). Rather than crash on launch, start a
     * fresh encrypted file; as a last resort fall back to a normal private preferences file.
     */
    private fun createPrefs(): SharedPreferences {
        return try {
            createEncryptedPrefs()
        } catch (e: Exception) {
            try {
                context.deleteSharedPreferences(PREFS_FILE)
                createEncryptedPrefs()
            } catch (e2: Exception) {
                context.getSharedPreferences(FALLBACK_PREFS_FILE, Context.MODE_PRIVATE)
            }
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val PREFS_FILE = "nourishlink_secure_prefs"
        private const val FALLBACK_PREFS_FILE = "nourishlink_prefs_fallback"
        private const val KEY_USER = "session_user"
        private const val KEY_LANGUAGE = "language_code"
    }
}
