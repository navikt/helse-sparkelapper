package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
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
        aktørId: String,
        ikkeEldreEnn: LocalDate,
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
            response.loggDatoer(ikkeEldreEnn)
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
            GjelderverdierSomIkkeSkalTriggeVarsel.inneholder(
                oppgave.finnVerdi("behandlingstype"),
                oppgave.finnVerdi("behandlingstema"),
            )
        }.size

    private fun JsonNode.loggDatoer(ikkeEldreEnn: LocalDate) {
        val opprinneligAntall = antallRelevanteOppgaver()
        if (path("antallTreffTotalt").intValue() < 1 || opprinneligAntall == 0) return

        val oppgaverOgOpprettetTidspunkt = get("oppgaver").filterNot { oppgave ->
            GjelderverdierSomIkkeSkalTriggeVarsel.inneholder(
                oppgave.finnVerdi("behandlingstype"),
                oppgave.finnVerdi("behandlingstema"),
            )
        }.filterNot {
            it.finnVerdi("opprettetTidspunkt") == null
        }.map { oppgave ->
            val textValue = oppgave.finnVerdi("opprettetTidspunkt")
            Triple(
                LocalDateTime.parse(textValue, ISO_ZONED_DATE_TIME).toLocalDate(),
                oppgave.finnVerdi("behandlingstype"),
                oppgave.finnVerdi("behandlingstema")
            )
        }
        val (forGamle, relevante) = oppgaverOgOpprettetTidspunkt.partition { it.first.isBefore(ikkeEldreEnn) }
        if (oppgaverOgOpprettetTidspunkt.size != opprinneligAntall) log.debug("{}, {}, {}",
            kv("opprinneligAntall", opprinneligAntall),
            kv("forGamle", forGamle),
            kv("relevante", relevante),
        )
        if (opprinneligAntall == forGamle.size) {
            log.debug(
                "Kandidat for automatisering pga. alle åpne og aktuelle Gosys-oppgaver er eldre enn 12 måneder. {}\n{}",
                kv("ikkeEldreEnn", ikkeEldreEnn),
                kv("behandlingstype og -tema for forGamle oppgaver", forGamle.map { it.second to it.third })
            )
        } else {
            log.debug(
                "Ikke kandidat for automatisering pga. aktuelle Gosys-oppgaver er ikke eldre enn 12 måneder. {}\n{}\n{}",
                kv("ikkeEldreEnn", ikkeEldreEnn),
                kv("behandlingstype og -tema for forGamle oppgaver", forGamle.map { it.second to it.third }),
                kv("opprettet, behandlingstype, og -tema for relevante oppgaver", relevante),
            )
        }
    }

    private fun JsonNode.finnVerdi(key: String): String? = path(key).textValue()
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
