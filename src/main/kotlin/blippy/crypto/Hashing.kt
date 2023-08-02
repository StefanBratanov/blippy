package blippy.crypto

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Hashing {
    private val SHA1_MESSAGE_DIGEST_THREAD_LOCAL =
        ThreadLocal.withInitial { getMessageDigest("SHA-1") }

    @JvmStatic
    fun sha1(input: ByteArray?): ByteArray {
        val md = SHA1_MESSAGE_DIGEST_THREAD_LOCAL.get()
        return md.digest(input)
    }

    private fun getMessageDigest(algorithm: String): MessageDigest {
        return try {
            MessageDigest.getInstance(algorithm)
        } catch (ex: NoSuchAlgorithmException) {
            throw IllegalStateException("$algorithm algorithm not available", ex)
        }
    }
}
