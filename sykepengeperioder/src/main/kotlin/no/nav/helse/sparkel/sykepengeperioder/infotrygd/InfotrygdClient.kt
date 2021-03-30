package no.nav.helse.sparkel.sykepengeperioder.infotrygd

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.sykepengeperioder.Fnr
import no.nav.helse.sparkel.sykepengeperioder.Sykepenger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InfotrygdClient(
    private val baseUrl: String,
    private val accesstokenScope: String,
    private val azureClient: AzureClient
) {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(JavaTimeModule())
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun hentHistorikk(
        behovId: String,
        fnr: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): Sykepenger {
        val url =
            "${baseUrl}/v1/hentSykepengerListe?fnr=$fnr&fraDato=${fom.format(DateTimeFormatter.ISO_DATE)}&tilDato=${
                tom.format(
                    DateTimeFormatter.ISO_DATE
                )
            }"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 100000
            setRequestProperty("Authorization", "Bearer ${azureClient.getToken(accesstokenScope).accessToken}")
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }

        tjenestekallLog.info(
            "svar fra Infotrygd: url=$url responseCode=$responseCode responseBody=$responseBody",
            keyValue("id", behovId)
        )

        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from Infotrygd")
        }

        val jsonNode = objectMapper.readValue<Sykepenger>(responseBody)

        try {
            MDC.put("id", behovId)
            return jsonNode
        } finally {
            MDC.remove("id")
        }
    }
}
