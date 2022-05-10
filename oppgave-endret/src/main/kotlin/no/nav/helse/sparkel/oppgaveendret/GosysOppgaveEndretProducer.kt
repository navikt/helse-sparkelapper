package no.nav.helse.sparkel.oppgaveendret

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.oppgave.IdentType
import no.nav.helse.sparkel.oppgaveendret.oppgave.Oppgave
import org.slf4j.LoggerFactory

class GosysOppgaveEndretProducer(
    private val rapidsConnection: RapidsConnection
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    private var forrigeFnr: String? = null
    private var forrigeOppdatering = LocalDateTime.MIN
    private val throttleDuration = Duration.ofSeconds(30)

    private val GOSYS = "FS22"

    fun onPacket(oppgave: Oppgave) {
        if (oppgave.behandlesAvApplikasjon != GOSYS || oppgave.tema != "SYK") return
        logger.info("Mottok endring på gosysoppgave på tema SYK oppgaven " + oppgave.id)
        if (oppgave.ident == null) return
        if (!oppgave.ident.folkeregisterident.isNullOrEmpty() && oppgave.ident.identType == IdentType.AKTOERID) {
            if (throttle(oppgave.ident.folkeregisterident)) return
            logger.info("Har folkeregisterident og aktorId på oppgaven " + oppgave.id)
            packetAndPublish(oppgave.ident.folkeregisterident, oppgave.ident.verdi)
        } else {
            sikkerlogg.info("Oppgave: " + oppgave)
            logger.error("Mangler folkeregisterident og/eller aktorId på oppgaven " + oppgave.id)
        }
    }

    private fun packetAndPublish(fødselsnummer: String, aktørId: String) {
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "gosys_oppgave_endret",
                "@id" to UUID.randomUUID(),
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fødselsnummer,
                "aktørId" to aktørId
            )
        )
        rapidsConnection.publish(fødselsnummer, packet.toJson())
    }

    private fun throttle(fnr: String): Boolean {
        if (fnr == forrigeFnr && forrigeOppdatering.plusNanos(throttleDuration.toNanos()) > LocalDateTime.now()) return true
        forrigeFnr = fnr
        forrigeOppdatering = LocalDateTime.now()
        return false
    }

}
