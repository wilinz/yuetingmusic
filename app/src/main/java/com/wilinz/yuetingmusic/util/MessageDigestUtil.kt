package com.wilinz.yuetingmusic.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MessageDigestUtil {
    @JvmStatic
    fun sumSha256(text: String): String {
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val encodedHash = digest.digest(text.toByteArray(StandardCharsets.UTF_8))
            return bytesToHex(encodedHash)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun bytesToHex(hash: ByteArray): String {
        val hexString = StringBuilder(2 * hash.size)
        for (b in hash) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                hexString.append('0')
            }
            hexString.append(hex)
        }
        return hexString.toString()
    }
}