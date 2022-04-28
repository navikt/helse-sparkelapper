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

    private val GOSYS = "FS22"

    fun onPacket(oppgave: Oppgave) {
        if (oppgave.behandlesAvApplikasjon != GOSYS || oppgave.behandlingstema != "SYK") return
        sikkerlogg.info("mottok endring på gosysoppgave")

        if (oppgave.ident == null) return
        val hendelseId = UUID.randomUUID().toString()
        val identer = pdlClient.hentIdenter(oppgave.ident.verdi, hendelseId)

        val packet: JsonMessage = JsonMessage.newMessage(mapOf(
            "@event_name" to "oppgave_endret",
            "@id" to UUID.randomUUID(),
            "@opprettet" to LocalDateTime.now(),
            "fødselsnummer" to identer.fødselsnummer,
            "aktørId" to identer.aktørId
        ))
        rapidsConnection.publish(identer.fødselsnummer, packet.toJson())
    }
}
