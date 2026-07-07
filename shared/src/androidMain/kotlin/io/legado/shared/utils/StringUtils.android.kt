package io.legado.shared.utils

/**
 * Android-specific string utility implementations.
 */
import android.util.Base64

actual fun encodeBase64Impl(data: ByteArray): String {
    return Base64.encodeToString(data, Base64.NO_WRAP)
}

actual fun decodeBase64Impl(str: String): ByteArray {
    return Base64.decode(str, Base64.NO_WRAP)
}
