package io.legado.shared.utils

import okio.ByteString

actual fun encodeBase64Impl(data: ByteArray): String {
    return ByteString.of(*data).base64()
}

actual fun decodeBase64Impl(str: String): ByteArray {
    return ByteString.decodeBase64(str)?.toByteArray() ?: ByteArray(0)
}
