package io.legado.shared.utils

import java.util.Base64

actual fun encodeBase64Impl(data: ByteArray): String {
    return Base64.getEncoder().withoutPadding().encodeToString(data)
}

actual fun decodeBase64Impl(str: String): ByteArray {
    return Base64.getDecoder().decode(str)
}
