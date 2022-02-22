package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
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
            loggHvisResponsenIkkeErSomForventet(response)
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

    // Dette tror vi ikke skal kunne skje i virkeligheten, men hvem vet?
    private fun loggHvisResponsenIkkeErSomForventet(response: JsonNode) {
        if (response.path("oppgaver") == null) {
            sikkerlogg.info("Forventet at verken responsen eller feltet oppgaver skulle være null eller mangle:\n$response")
            return
        }
    }

    private fun JsonNode.antallRelevanteOppgaver(): Int? =
        takeUnless { it.isMissingNode }?.let {
            it["oppgaver"].filterNot { oppgave ->
                inneholder(oppgave.finnVerdi("behandlingstype"), oppgave.finnVerdi("behandlingstema"))
            }.size
        }

    private fun JsonNode.finnVerdi(key: String): String? =
        if (hasNonNull(key)) get(key).textValue() else null
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
