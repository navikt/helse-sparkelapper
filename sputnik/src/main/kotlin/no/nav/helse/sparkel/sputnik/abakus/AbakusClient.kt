package no.nav.helse.sparkel.sputnik.abakus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.sputnik.abakus.HttpRequest.postJson
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

internal interface AbakusClient {
    fun hent(fødselsnummer: String, fom: LocalDate, tom: LocalDate, vararg ytelser: Ytelse): Set<Stønadsperiode>
}

internal class RestAbakusClient(
    private val url: URL,
    private val accessTokenClient: AccessTokenClient
) : AbakusClient {
    override fun hent(fødselsnummer: String, fom: LocalDate, tom: LocalDate, vararg ytelser: Ytelse): Set<Stønadsperiode> {
        check(fom <= tom) { "fom $fom må være før eller lik tom $tom" }
        val callId = "${UUID.randomUUID()}"
        val requestBody = requestBody(fødselsnummer, fom, tom, *ytelser)

        val response = try {
            url.postJson(requestBody,
                "Authorization" to "Bearer ${accessTokenClient.accessToken()}",
                "Nav-Consumer-Id" to "Sykepenger",
                "Nav-Callid" to callId
            ).second
        } catch (exception: Exception) {
            sikkerlogg.error("Feil ved henting av ${ytelser.toSet()} fra Abakus for {} med {}.",
                keyValue("fødselsnummer", fødselsnummer), keyValue("callId", callId), exception)
            throw IllegalStateException("Feil ved henting fra Abakus")
        }

        sikkerlogg.info("Hentet ${ytelser.toSet()} fra Abakus for {} med {}. Response:\n\t$response",
            keyValue("fødselsnummer", fødselsnummer), keyValue("callId", callId))

        return try {
            response.abakusResponseTilStønadsperioder(fom, tom, *ytelser)
        } catch (exception: Exception) {
            sikkerlogg.error("Feil ved mapping av ${ytelser.toSet()} fra Abakus-response for {} med {}.",
                keyValue("fødselsnummer", fødselsnummer), keyValue("callId", callId), exception)
            throw throw IllegalStateException("Feil ved mapping av response fra Abakus")
        }
    }

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        private val aktiveYtelseStatuser = setOf("LØPENDE", "AVSLUTTET", "UNDER_BEHANDLING")

        @Language("JSON")
        private fun requestBody(fødselsnummer: String, fom: LocalDate, tom: LocalDate, vararg ytelser: Ytelse) = """
        {
            "ident": {
                "verdi": "$fødselsnummer"
            },
            "periode": {
                "fom": "$fom",
                "tom": "$tom"
            },
            "ytelser": ${ytelser.map { "\"$it\"" }}
        }
        """

        private fun JsonNode.abakusResponseTilStønadsperioder(fom: LocalDate, tom: LocalDate, vararg ytelser: Ytelse) = asSequence()
            .filter { it.get("ytelseStatus").asText() in aktiveYtelseStatuser }
            .map { ytelse -> ytelse.get("anvist").onEach {
                it as ObjectNode
                it.put("@ytelse", ytelse.path("ytelse").asText())
                it.put("@vedtattTidspunkt", ytelse.path("vedtattTidspunkt").asText())
            }}
            .flatten()
            .map { anvisning ->
                Stønadsperiode(
                    fom = LocalDate.parse(anvisning.get("periode").get("fom").asText()),
                    tom = LocalDate.parse(anvisning.get("periode").get("tom").asText()),
                    grad = anvisning.path("utbetalingsgrad").path("verdi")
                        .takeUnless { it.isMissingOrNull() }
                        ?.asDouble()
                        ?.roundToInt() ?: 100,
                    ytelse = Ytelse(anvisning.get("@ytelse").asText()),
                    vedtatt = anvisning.get("@vedtattTidspunkt").asLocalDateTime()
                )
            }
            .filter { it.ytelse in ytelser }
            .filterNot { it.fom > tom } // Filtrerer bort perioder som starter etter tom
            .filterNot { it.tom < fom } // Filtrerer bort perioder som slutter før fom
            .toSet()
    }
}
