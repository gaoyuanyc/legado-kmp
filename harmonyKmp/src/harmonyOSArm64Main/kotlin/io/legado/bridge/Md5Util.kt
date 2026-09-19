package io.legado.bridge

import kotlin.random.Random

/**
 * Pure-Kotlin MD5 implementation (no java.security dependency),
 * so it compiles for the native/harmonyOSArm64 target.
 */
internal object Md5Util {

    private val shift = intArrayOf(7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22, 7, 12, 17, 22,
        5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20, 5, 9, 14, 20,
        4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23, 4, 11, 16, 23,
        6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21, 6, 10, 15, 21)

    private val k = IntArray(64)
    private val hexDigits = "0123456789abcdef".toCharArray()

    init {
        for (i in 0 until 64) {
            k[i] = (kotlin.math.abs(kotlin.math.sin((i + 1).toDouble())) * 4294967296.0).toLong().toInt()
        }
    }

    fun md5Hex(input: ByteArray): String {
        val digest = digest(input)
        val sb = StringBuilder()
        for (b in digest) {
            val v = b.toInt() and 0xff
            sb.append(hexDigits[v ushr 4]).append(hexDigits[v and 0x0f])
        }
        return sb.toString()
    }

    private fun digest(input: ByteArray): ByteArray {
        val msg = pad(input)
        var a0 = 0x67452301
        var b0 = -0x10325477
        var c0 = -0x67452302
        var d0 = 0x10325476

        var offset = 0
        while (offset < msg.size) {
            val m = IntArray(16)
            for (i in 0 until 16) {
                m[i] = (msg[offset + i * 4].toInt() and 0xff) or
                    ((msg[offset + i * 4 + 1].toInt() and 0xff) shl 8) or
                    ((msg[offset + i * 4 + 2].toInt() and 0xff) shl 16) or
                    ((msg[offset + i * 4 + 3].toInt() and 0xff) shl 24)
            }
            offset += 64

            var a = a0
            var b = b0
            var c = c0
            var d = d0

            for (i in 0 until 64) {
                var f: Int
                var g: Int
                when {
                    i < 16 -> { f = (b and c) or ((b.inv()) and d); g = i }
                    i < 32 -> { f = (d and b) or ((d.inv()) and c); g = (5 * i + 1) % 16 }
                    i < 48 -> { f = b xor c xor d; g = (3 * i + 5) % 16 }
                    else -> { f = c xor (b or (d.inv())); g = (7 * i) % 16 }
                }
                val temp = d
                d = c
                c = b
                b = b + rotl(a + f + k[i] + m[g], shift[i])
                a = temp
            }

            a0 += a
            b0 += b
            c0 += c
            d0 += d
        }

        val out = ByteArray(16)
        writeInt(out, 0, a0)
        writeInt(out, 4, b0)
        writeInt(out, 8, c0)
        writeInt(out, 12, d0)
        return out
    }

    private fun pad(input: ByteArray): ByteArray {
        val bitLen = (input.size.toLong() * 8)
        val newLen = ((input.size + 8) / 64 + 1) * 64
        val msg = ByteArray(newLen)
        input.copyInto(msg, 0)
        msg[input.size] = 0x80.toByte()
        msg[newLen - 8] = (bitLen and 0xff).toByte()
        msg[newLen - 7] = ((bitLen ushr 8) and 0xff).toByte()
        msg[newLen - 6] = ((bitLen ushr 16) and 0xff).toByte()
        msg[newLen - 5] = ((bitLen ushr 24) and 0xff).toByte()
        msg[newLen - 4] = ((bitLen ushr 32) and 0xff).toByte()
        msg[newLen - 3] = ((bitLen ushr 40) and 0xff).toByte()
        msg[newLen - 2] = ((bitLen ushr 48) and 0xff).toByte()
        msg[newLen - 1] = ((bitLen ushr 56) and 0xff).toByte()
        return msg
    }

    private fun writeInt(out: ByteArray, pos: Int, value: Int) {
        out[pos] = (value and 0xff).toByte()
        out[pos + 1] = ((value ushr 8) and 0xff).toByte()
        out[pos + 2] = ((value ushr 16) and 0xff).toByte()
        out[pos + 3] = ((value ushr 24) and 0xff).toByte()
    }
}