package io.legado.shared.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.util.toMap

/**
 * Platform-agnostic HTTP request handler.
 * Abstraction over OkHttp (Android) / Ktor Curl (HarmonyOS native).
 */
interface HttpHandler {

    /**
     * Execute an HTTP request and return response.
     */
    suspend fun execute(
        url: String,
        method: String = "GET",
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        contentType: String? = null
    ): HttpResponse

    /**
     * Get response body as string.
     */
    suspend fun getString(
        url: String,
        headers: Map<String, String> = emptyMap()
    ): StringResponse

    /**
     * Post form data.
     */
    suspend fun postForm(
        url: String,
        form: Map<String, String>,
        headers: Map<String, String> = emptyMap()
    ): StringResponse

    /**
     * Post JSON body.
     */
    suspend fun postJson(
        url: String,
        json: String,
        headers: Map<String, String> = emptyMap()
    ): StringResponse

    /**
     * Close the HTTP client.
     */
    fun close()
}

data class StringResponse(
    val code: Int,
    val body: String,
    val headers: Map<String, List<String>>,
    val cookies: Map<String, String> = emptyMap()
)

/**
 * Factory to create platform-specific HttpHandler.
 */
expect fun createHttpHandler(): HttpHandler
