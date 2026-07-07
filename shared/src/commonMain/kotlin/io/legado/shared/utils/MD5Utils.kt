package io.legado.shared.utils

import java.security.MessageDigest

object MD5Utils {
    private val md5Digest = ThreadLocal<MessageDigest>()

    private fun getDigest(): MessageDigest {
        return md5Digest.get() ?: MessageDigest.getInstance("MD5").also { md5Digest.set(it) }
    }

    fun md5Encode(str: String?): String {
        if (str == null) return ""
        val digest = getDigest()
        digest.reset()
        val bytes = digest.digest(str.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun md5Encode16(str: String): String {
        val full = md5Encode(str)
        return if (full.length >= 24) full.substring(8, 24) else full
    }
}
