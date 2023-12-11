package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.gosys.GjelderverdierSomIkkeSkalTriggeVarsel.Companion.inneholder
import org.slf4j.LoggerFactory
import org.slf4j.MDC


internal fun interface Oppgavehenter {
    fun hentÅpneOppgaver(aktørId: String, behovId: String): JsonNode
}

internal class OppgaveService(private val oppgavehenter: Oppgavehenter) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForBehov(
        behovId: String,
        aktørId: String
    ): Int? = withMDC("id" to behovId) {
        try {
            val response = oppgavehenter.hentÅpneOppgaver(
                aktørId = aktørId,
                behovId = behovId
            )
            log.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            if (!response.path("oppgaver").isArray) {
                sikkerlogg.info("Forventet å finne et oppgaver-felt med et array:\n$response")
                return@withMDC null
            }
            sikkerlogg.info("Åpne oppgaver, respons: $response")
            response.loggDatoer()
            response.antallRelevanteOppgaver().also { antallEtterFiltrering ->
                if (antallEtterFiltrering == 0 && response["oppgaver"].size() > 0) {
                    log.info("Gosys-oppgaver ble filtrert ned til 0 slik at varsel ikke vil bli laget for $aktørId.")
                }
            }
        } catch (err: Exception) {
            log.warn(
                "feil ved henting av oppgave-data: ${err.message} for behov {}",
                keyValue("behovId", behovId),
                err
            )
            sikkerlogg.warn(
                "feil ved henting av oppgave-data: ${err.message} for behov {}",
                keyValue("behovId", behovId),
                err
            )
            null
        }
    }

    private fun JsonNode.antallRelevanteOppgaver(): Int =
        get("oppgaver").filterNot { oppgave ->
            inneholder(oppgave.finnVerdi("behandlingstype"), oppgave.finnVerdi("behandlingstema"))
        }.size

    private fun JsonNode.loggDatoer() {
        if (path("antallTreffTotalt").intValue() < 1) return

        val datoer = get("oppgaver").map { oppgave ->
            val textValue = oppgave.finnVerdi("opprettetTidspunkt") ?: return@map null
            LocalDateTime.parse(textValue, ISO_ZONED_DATE_TIME).toLocalDate()
        }
        log.debug("datoer på oppgavene: {}", datoer)
    }

    private fun JsonNode.finnVerdi(key: String): String? =
        path(key).textValue()
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
