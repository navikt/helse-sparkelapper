package no.nav.helse.sparkel.abakus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import org.slf4j.LoggerFactory

internal object HttpRequest {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val objectMapper = jacksonObjectMapper()

    private fun URL.request(
        method: String,
        body: ((outputStream: OutputStream) -> Unit)?,
        vararg headers: Pair<String, String>
    ) = with(openConnection() as HttpURLConnection) {
        requestMethod = method
        connectTimeout = 10000
        readTimeout = 10000
        doOutput = true
        headers.forEach { (key, value) ->
            setRequestProperty(key, value)
        }
        body?.let { outputStream.use(it) }
        val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
        val responseBody = stream?.use { it.bufferedReader().readText() }
        if (responseBody == null || responseCode >= 300) {
            sikkerlogg.error("Mottok responseCode=$responseCode, url=$url:\nBody:\n$responseBody")
            throw IllegalStateException("Mottok responseCode=$responseCode, url=$url")
        }
        responseCode to objectMapper.readTree(responseBody)
    }

    internal fun URL.get(
        vararg headers: Pair<String, String>
    ) = request(method = "GET", body = null, *headers)

    internal fun URL.postJson(
        requestBody: String,
        vararg headers: Pair<String, String>
    ): Pair<Int, JsonNode> {
        val alleHeaders = headers.toList()
            .plus("Accept" to "application/json")
            .plus("Content-Type" to "application/json")

        return request(method = "POST", body = { outputStream ->
            objectMapper.writeValue(outputStream, objectMapper.readTree(requestBody))
        }, *alleHeaders.toTypedArray())
    }
}