package io.legado.shared.network

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.util.toMap

class KtorAndroidHttpHandler(private val client: io.ktor.client.HttpClient = HttpClientFactory.create()) : HttpHandler {

    override suspend fun execute(
        url: String, method: String, headers: Map<String, String>, body: String?, contentType: String?
    ): HttpResponse {
        return client.prepareRequest {
            this.method = HttpMethod.parse(method)
            url(url)
            headers.forEach { (k, v) -> header(k, v) }
            if (body != null) {
                setBody(body)
                if (contentType != null) {
                    this.contentType(io.ktor.http.ContentType.parse(contentType))
                }
            }
        }.execute()
    }

    override suspend fun getString(url: String, headers: Map<String, String>): StringResponse {
        val response = client.get(url) {
            headers.forEach { (k, v) -> header(k, v) }
        }
        return StringResponse(
            code = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.toMap()
        )
    }

    override suspend fun postForm(url: String, form: Map<String, String>, headers: Map<String, String>): StringResponse {
        val response = client.submitForm(
            url = url,
            formParameters = Parameters.build {
                form.forEach { (k, v) -> append(k, v) }
            }
        )
        return StringResponse(
            code = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.toMap()
        )
    }

    override suspend fun postJson(url: String, json: String, headers: Map<String, String>): StringResponse {
        val response = client.post(url) {
            headers.forEach { (k, v) -> header(k, v) }
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(json)
        }
        return StringResponse(
            code = response.status.value,
            body = response.bodyAsText(),
            headers = response.headers.toMap()
        )
    }

    override fun close() {
        client.close()
    }
}

actual fun createHttpHandler(): HttpHandler = KtorAndroidHttpHandler()
