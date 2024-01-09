package no.nav.helse.sparkel.institusjonsopphold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

internal class InstitusjonsoppholdClient(
    private val baseUrl: String,
    private val scope: String?,
    private val stsClient: StsRestClient?,
    private val azureClient: AzureTokenProvider?
) {

    companion object {
        private val objectMapper = ObjectMapper()
    }

    internal fun hentInstitusjonsopphold(
        fødselsnummer: String,
        behovId: String
    ): JsonNode {
        val url = "${baseUrl}/api/v1/person/institusjonsopphold"

        val (responseCode, responseBody) = with(URI(url).toURL().openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${stsClient?.token() ?: azureClient?.bearerToken(scope ?: "")?.token}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Nav-Call-Id", behovId)
            System.getenv("NAIS_APP_NAME")?.also { setRequestProperty("Nav-Consumer-Id", it) }
            setRequestProperty("Nav-Personident", fødselsnummer)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from Inst2")
        }

        return objectMapper.readTree(responseBody)
    }
}
