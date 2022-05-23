package no.nav.helse.sparkel.pleiepenger.infotrygd

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import no.nav.helse.sparkel.pleiepenger.SyktBarnKilde
import no.nav.helse.sparkel.pleiepenger.infotrygd.Stønadstype.OMSORGSPENGER
import no.nav.helse.sparkel.pleiepenger.infotrygd.Stønadstype.OPPLAERINGSPENGER
import no.nav.helse.sparkel.pleiepenger.infotrygd.Stønadstype.PLEIEPENGER
import org.slf4j.LoggerFactory

class InfotrygdClient(
    private val baseUrl: String,
    private val accesstokenScope: String,
    private val azureClient: AzureClient
): SyktBarnKilde {

    internal companion object {
        private val objectMapper = ObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(InfotrygdClient::class.java)

        private fun JsonNode.infotrygdVedtakSomStønadsperiode() = Stønadsperiode(
            fom = path("fom").textValue().let { LocalDate.parse(it) },
            tom = path("tom").textValue().let { LocalDate.parse(it) },
            grad = path("grad").intValue()
        )

        internal fun JsonNode.infotrygdResponseSomStønadsperioder() =
            get("vedtak").map { it.infotrygdVedtakSomStønadsperiode() }
    }

    internal fun hent(
        stønadstype: Stønadstype,
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

    override fun pleiepenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(PLEIEPENGER, fnr, fom, tom)?.infotrygdResponseSomStønadsperioder()?.toSet() ?: throw IllegalStateException("Feil ved henting av pleiepenger fra Infotrygd")

    override fun omsorgspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(OMSORGSPENGER, fnr, fom, tom)?.infotrygdResponseSomStønadsperioder()?.toSet() ?: throw IllegalStateException("Feil ved henting av omsorgspenger fra Infotrygd")

    override fun opplæringspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(OPPLAERINGSPENGER, fnr, fom, tom)?.infotrygdResponseSomStønadsperioder()?.toSet() ?: throw IllegalStateException("Feil ved henting av opplæringspenger fra Infotrygd")
}
