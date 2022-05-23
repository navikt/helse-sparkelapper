package no.nav.helse.sparkel.pleiepenger.k9

import com.fasterxml.jackson.databind.JsonNode
import java.net.URL
import java.time.LocalDate
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import no.nav.helse.sparkel.pleiepenger.SyktBarnKilde
import no.nav.helse.sparkel.pleiepenger.k9.HttpRequest.postJson
import org.intellij.lang.annotations.Language
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

    private fun hent(requestBody: String) =
        url.postJson(requestBody, "Authorization" to "Bearer ${accessTokenClient.accessToken()}")
            .second
            .abakusResponseTilStønadsperioder()

    internal companion object {
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
            .filter { it.path("ytelseStatus").asText() in aktiveYtelseStatuser }
            .map { it.path("anvist") }
            .flatten()
            .map { anvisning ->
                Stønadsperiode(
                    fom = LocalDate.parse(anvisning.path("periode").path("fom").asText()),
                    tom = LocalDate.parse(anvisning.path("periode").path("tom").asText()),
                    grad = anvisning.path("utbetalingsgrad").path("verdi").asDouble().roundToInt()
                )
            }.toSet()
    }
}
