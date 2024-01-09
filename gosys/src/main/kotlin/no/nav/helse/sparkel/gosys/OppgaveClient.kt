package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import org.slf4j.LoggerFactory

internal class OppgaveClient(
    private val baseUrl: String,
    private val scope: String?,
    private val stsClient: StsRestClient?,
    private val azureClient: AzureTokenProvider?
) : Oppgavehenter {

    companion object {
        private val objectMapper = ObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    override fun hentÅpneOppgaver(
        aktørId: String,
        behovId: String
    ): JsonNode {
        val url = "${baseUrl}/api/v1/oppgaver?statuskategori=AAPEN&tema=SYK&aktoerId=${aktørId}"

        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${stsClient?.token() ?: azureClient?.bearerToken(scope ?: "")}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Correlation-ID", behovId)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        sikkerlogg.info("GET $url ${if (stsClient != null) " med STS" else if (azureClient != null) " med azure" else "med ingenting" }:\n$responseBody")

        if (responseCode >= 300) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from oppgave")
        } else if (responseBody == "" || responseBody == null) {
            throw RuntimeException("Fikk ikke noe innhold tilbake fra oppgaveoppslaget, gav responseCode=$responseCode")
        }

        return objectMapper.readTree(responseBody)
    }
}
