package et.hotelbooking.app.util

import android.util.Base64
import java.security.SecureRandom
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** PBKDF2-HMAC-SHA256 password hashing with a per-user random salt. Plain passwords are never stored. */
object PasswordHasher {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256

    fun newSalt(): String {
        val b = ByteArray(16); SecureRandom().nextBytes(b)
        return Base64.encodeToString(b, Base64.NO_WRAP)
    }

    fun hash(password: String, salt: String, iterations: Int = ITERATIONS): String {
        val spec = PBEKeySpec(password.toCharArray(), Base64.decode(salt, Base64.NO_WRAP), iterations, KEY_LENGTH)
        val key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        spec.clearPassword()
        return "pbkdf2_sha256$" + iterations + "$" + Base64.encodeToString(key, Base64.NO_WRAP)
    }

    fun verify(password: String, salt: String, stored: String): Boolean {
        val iterations = stored.split('$').getOrNull(1)?.toIntOrNull() ?: ITERATIONS
        val candidate = hash(password, salt, iterations)
        return MessageDigest.isEqual(candidate.toByteArray(), stored.toByteArray())
    }
}
