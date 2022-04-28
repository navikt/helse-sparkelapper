package no.nav.helse.sparkel.oppgaveendret

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.pdl.PdlClient
import org.slf4j.LoggerFactory

class OppgaveEndretProducer(
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

        if (oppgave.ident.folkeregisterident!!.isNotEmpty()) {
            val fnr = oppgave.ident.folkeregisterident
            publish(fnr)
        }
        else {
            logger.info("Mangler folkeregisterident gjør kall mot pdl")
            val hendelseId = UUID.randomUUID().toString()
            val identer = pdlClient.hentIdenter(oppgave.ident.verdi, hendelseId)
            publish(identer.fødselsnummer)
        }
    }

    fun publish(fødselsnummer: String) {
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
