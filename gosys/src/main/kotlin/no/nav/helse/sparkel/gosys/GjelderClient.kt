package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


/**
 * Hente liste over alle mulige såkalte gjelder-verdier
 */
internal class GjelderClient(
    private val baseUrl: String,
    private val stsClient: StsRestClient
) {

    companion object {
        private val objectMapper = ObjectMapper()
        private val log = LoggerFactory.getLogger("GjelderClient")
    }

    fun hentGjelderverdier() {
        val url = "${baseUrl}/api/v1/kodeverk/gjelder/SYK"

        val correlationId = UUID.randomUUID().toString()
        log.info("Gjør kall for å hente gjelder-verdier - correlationId: $correlationId")
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${stsClient.token()}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("X-Correlation-ID", correlationId)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            log.warn("Kunne ikke hente gjelder-lista, correlationId: $correlationId. responseCode=$responseCode, responseBody=$responseBody")
        }

        log.info("Her er alle gjelder-kombinasjoner for SYK:\n${objectMapper.readTree(responseBody)}")
    }
}
