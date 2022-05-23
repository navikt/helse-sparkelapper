package no.nav.helse.sparkel.pleiepenger.infotrygd

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

class InfotrygdClient(
    private val baseUrl: String,
    private val accesstokenScope: String,
    private val azureClient: AzureClient
) {

    private companion object {
        private val objectMapper = ObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(InfotrygdClient::class.java)
    }

    internal fun hent(
        stønadstype: Stønadsperiode.Stønadstype,
        fnr: String,
        fom: LocalDate,
        tom: LocalDate
    ): JsonNode? {
        val requestBody = objectMapper.createObjectNode().apply {
            put("identitetsnummer", fnr)
            put("fom", fom.toString())
            put("tom", tom.toString())
        }

        val url = "$baseUrl${stønadstype.url}"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", "Bearer ${azureClient.getToken(accesstokenScope).accessToken}")
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json")

            doOutput = true
            objectMapper.writeValue(outputStream, requestBody)

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        if (responseCode >= 300 || responseBody == null) {
            sikkerlogg.error("Kunne ikke hente pleiepenger responseCode=$responseCode, url=$url:\nBody:\n$responseBody", keyValue("fødselsnummer", fnr))
            log.error("Kunne ikke hente pleiepenger responseCode=$responseCode, url=$url (se sikkerlogg for detaljer)")
            return null
        }

        return objectMapper.readTree(responseBody)
    }
}
