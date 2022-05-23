package no.nav.helse.sparkel.pleiepenger.k9

import com.fasterxml.jackson.databind.JsonNode
import java.net.URL
import java.time.LocalDate
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import no.nav.helse.sparkel.pleiepenger.SyktBarnKilde
import no.nav.helse.sparkel.pleiepenger.k9.HttpRequest.postJson
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

internal class AbakusClient(
    private val url: URL,
    private val accessTokenClient: AccessTokenClient
): SyktBarnKilde {

    override fun pleiepenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(requestBody(fnr, PleiepengerSyktBarn, fom, tom))

    override fun omsorgspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(requestBody(fnr, Omsorgspenger, fom, tom))

    override fun opplæringspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(requestBody(fnr, Opplæringspenger, fom, tom))

    private fun hent(requestBody: String): Set<Stønadsperiode> {
        var response: JsonNode? = null
        return try {
            response = url.postJson(requestBody, "Authorization" to "Bearer ${accessTokenClient.accessToken()}").second
            response.abakusResponseTilStønadsperioder()
        } catch (exception: Exception) {
            sikkerlogg.error("Feil ved henting fra Abakus. Response:\n$response", exception)
            emptySet()
        }
    }

    internal companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        private const val PleiepengerSyktBarn = "PSB"
        private const val Omsorgspenger = "OMP"
        private const val Opplæringspenger = "OLP"

        private val aktiveYtelseStatuser = setOf("LØPENDE", "AVSLUTTET")

        @Language("JSON")
        private fun requestBody(fnr: String, ytelse: String, fom: LocalDate, tom: LocalDate) = """
        {
            "person": {
                "identType": "FNR",
                "ident": "$fnr"
            },
            "periode": {
                "fom": "$fom",
                "tom": "$tom"
            },
            "ytelser": [{
                "kode": "$ytelse",
                "kodeverk": "FAGSAK_YTELSE_TYPE"
            }]
        }
        """

        internal fun JsonNode.abakusResponseTilStønadsperioder() = asSequence()
            .filter { it.get("ytelseStatus").asText() in aktiveYtelseStatuser }
            .map { it.get("anvist") }
            .flatten()
            .map { anvisning ->
                Stønadsperiode(
                    fom = LocalDate.parse(anvisning.get("periode").get("fom").asText()),
                    tom = LocalDate.parse(anvisning.get("periode").get("tom").asText()),
                    grad = anvisning.get("utbetalingsgrad").get("verdi").asDouble().roundToInt()
                )
            }.toSet()
    }
}
