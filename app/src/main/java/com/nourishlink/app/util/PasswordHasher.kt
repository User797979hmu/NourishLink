package com.nourishlink.app.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Salted, slow password hashing (PBKDF2-HMAC-SHA256). Passwords are never stored in plain text.
 *
 * Stored format:  pbkdf2_sha256$<iterations>$<base64 salt>$<base64 hash>
 *
 * Built on javax.crypto.Mac so it works on every supported Android version (the
 * "PBKDF2WithHmacSHA256" SecretKeyFactory only exists from API 26 and minSdk here is 24).
 * This is slow on purpose (100k iterations) — always call it off the main thread.
 */
object PasswordHasher {

    private const val ALGORITHM_TAG = "pbkdf2_sha256"
    private const val SEPARATOR = "$"
    private const val ITERATIONS = 100_000
    private const val SALT_BYTES = 16

    fun hash(password: String): String {
        require(password.isNotEmpty()) { "Password must not be empty" }
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = derive(password, salt, ITERATIONS)
        return listOf(
            ALGORITHM_TAG,
            ITERATIONS.toString(),
            Base64.encodeToString(salt, Base64.NO_WRAP),
            Base64.encodeToString(derived, Base64.NO_WRAP)
        ).joinToString(SEPARATOR)
    }

    fun verify(password: String, stored: String?): Boolean {
        if (stored.isNullOrEmpty() || password.isEmpty()) return false
        val parts = stored.split(SEPARATOR)
        if (parts.size != 4 || parts[0] != ALGORITHM_TAG) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        if (iterations < 1) return false
        return try {
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expected = Base64.decode(parts[3], Base64.NO_WRAP)
            MessageDigest.isEqual(expected, derive(password, salt, iterations))
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /** PBKDF2 with a single 32-byte output block (output length == HMAC-SHA256 length). */
    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(password.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        mac.update(salt)
        mac.update(byteArrayOf(0, 0, 0, 1))
        var u = mac.doFinal()
        val result = u.copyOf()
        for (i in 1 until iterations) {
            u = mac.doFinal(u)
            for (j in result.indices) {
                result[j] = (result[j].toInt() xor u[j].toInt()).toByte()
            }
        }
        return result
    }
}
