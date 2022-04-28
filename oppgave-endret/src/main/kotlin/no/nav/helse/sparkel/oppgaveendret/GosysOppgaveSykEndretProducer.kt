package no.nav.helse.sparkel.oppgaveendret

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.pdl.PdlClient
import org.slf4j.LoggerFactory

class GosysOppgaveSykEndretProducer(
    private val rapidsConnection: RapidsConnection,
    private val pdlClient: PdlClient
) {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val GOSYS = "FS22"

    fun onPacket(oppgave: Oppgave) {
        if (oppgave.behandlesAvApplikasjon != GOSYS || oppgave.behandlingstema != "SYK") return
        sikkerlogg.info("mottok endring på gosysoppgave på behandlingstema SYK")

        if (oppgave.ident == null) return

        if (oppgave.ident.folkeregisterident != null && oppgave.ident.folkeregisterident.isNotEmpty()) {
            logger.info("Fant folkeregisterident(fødselsnummer)")
            val fnr = oppgave.ident.folkeregisterident
            //packetAndPublish(fnr)
        }
        else {
            logger.info("Mangler folkeregisterident gjør kall mot pdl for å finne fødselsnummer")
            val hendelseId = UUID.randomUUID().toString()
            val identer = pdlClient.hentIdenter(oppgave.ident.verdi, hendelseId)
            //packetAndPublish(identer.fødselsnummer)
        }
    }

    private fun packetAndPublish(fødselsnummer: String) {
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "oppgave_endret",
                "@id" to UUID.randomUUID(),
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fødselsnummer
            )
        )
        rapidsConnection.publish(fødselsnummer, packet.toJson())
    }

}