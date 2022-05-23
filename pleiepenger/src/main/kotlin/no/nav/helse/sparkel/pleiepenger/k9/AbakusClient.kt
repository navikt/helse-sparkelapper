package no.nav.helse.sparkel.pleiepenger.k9

import com.fasterxml.jackson.databind.JsonNode
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import no.nav.helse.sparkel.pleiepenger.SyktBarnKilde
import no.nav.helse.sparkel.pleiepenger.k9.HttpRequest.postJson
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

internal class AbakusClient(
    private val url: URL,
    private val accessTokenClient: AccessTokenClient,
    private val enabled: Boolean
): SyktBarnKilde {

    override fun pleiepenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(fnr, fom, tom, PleiepengerSyktBarn).håndter("pleiepenger")

    override fun omsorgspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(fnr, fom, tom, Omsorgspenger).håndter("omsorgspenger")

    override fun opplæringspenger(fnr: String, fom: LocalDate, tom: LocalDate) =
        hent(fnr, fom, tom, Opplæringspenger).håndter("opplæringspenger")

    private fun hent(fnr: String, fom: LocalDate, tom: LocalDate, ytelse: String): Set<Stønadsperiode> {
        var response: JsonNode? = null
        val callId = "${UUID.randomUUID()}"
        val requestBody = requestBody(fnr, fom, tom, ytelse)
        return try {
            response = url.postJson(requestBody,
                "Authorization" to "Bearer ${accessTokenClient.accessToken()}",
                "Nav-Consumer-Id" to "Sykepenger",
                "Nav-Callid" to callId
            ).second
            response.abakusResponseTilStønadsperioder(fom, tom)
        } catch (exception: Exception) {
            sikkerlogg.error("Feil ved henting fra Abakus med {}. Response:\n$response", keyValue("callId", callId), exception)
            emptySet()
        }
    }

    private fun Set<Stønadsperiode>.håndter(ytelse: String): Set<Stønadsperiode> {
        if (enabled || isEmpty()) return this
        sikkerlogg.info("Hentet $size stønadsperioder for $ytelse fra Abakus. Blir ikke lagt til i løsningen på behovet enda:\n$this")
        return emptySet()
    }

    internal companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        private const val PleiepengerSyktBarn = "PSB"
        private const val Omsorgspenger = "OMP"
        private const val Opplæringspenger = "OLP"

        private val aktiveYtelseStatuser = setOf("LØPENDE", "AVSLUTTET")

        @Language("JSON")
        private fun requestBody(fnr: String, fom: LocalDate, tom: LocalDate, ytelse: String) = """
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

        internal fun JsonNode.abakusResponseTilStønadsperioder(fom: LocalDate, tom: LocalDate) = asSequence()
            .filter { it.get("ytelseStatus").asText() in aktiveYtelseStatuser }
            .map { it.get("anvist") }
            .flatten()
            .map { anvisning ->
                Stønadsperiode(
                    fom = LocalDate.parse(anvisning.get("periode").get("fom").asText()),
                    tom = LocalDate.parse(anvisning.get("periode").get("tom").asText()),
                    grad = anvisning.get("utbetalingsgrad").get("verdi").asDouble().roundToInt()
                )
            }
            .filterNot { it.fom > tom } // Filtrerer bort perioder som starter etter tom
            .filterNot { it.tom < fom } // Filtrerer bort perioder som slutter før fom
            .toSet()
    }
}
