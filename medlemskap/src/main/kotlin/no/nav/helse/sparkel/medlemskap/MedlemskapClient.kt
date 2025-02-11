package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDate

internal class MedlemskapClient(
    private val baseUrl: URI,
    private val azureClient: AzureTokenProvider,
    private val scope: String
) {

    private fun request(requestBody: String): Pair<Int, String> {
        return with(URI("$baseUrl/speilvurdering").toURL().openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            val bearerToken = azureClient.bearerToken(scope).getOrThrow()
            setRequestProperty("Authorization", "Bearer ${bearerToken.token}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 10_000
            readTimeout = 30_000
            doOutput = true
            outputStream.use {
                it.bufferedWriter().apply {
                    write(requestBody)
                    flush()
                }
            }

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to (stream?.bufferedReader()?.readText() ?: "null")
        }
    }

    internal fun hentMedlemskapsvurdering(fnr: String, fom: LocalDate, tom: LocalDate): JsonNode {
        val svar = Tolk(fnr, fom, tom, ::request).tolk()
        return objectMapper.createObjectNode().set("resultat", objectMapper.createObjectNode().put("svar", svar))
    }

    internal companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}
