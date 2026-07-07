package io.legado.shared.network

/**
 * Native stub for HarmonyOS - will be replaced with KNOI bridge
 */
class NativeHttpHandler : HttpHandler {
    override suspend fun execute(
        url: String, method: String, headers: Map<String, String>, body: String?, contentType: String?
    ): HttpResponse = throw UnsupportedOperationException("Use KNOI bridge on HarmonyOS")

    override suspend fun getString(url: String, headers: Map<String, String>): StringResponse =
        throw UnsupportedOperationException("Use KNOI bridge on HarmonyOS")

    override suspend fun postForm(url: String, form: Map<String, String>, headers: Map<String, String>): StringResponse =
        throw UnsupportedOperationException("Use KNOI bridge on HarmonyOS")

    override suspend fun postJson(url: String, json: String, headers: Map<String, String>): StringResponse =
        throw UnsupportedOperationException("Use KNOI bridge on HarmonyOS")

    override fun close() {}
}
