package no.nav.helse.sparkel.oppgaveendret

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory

class OppgaveEndretProducer(
    private val rapidsConnection: RapidsConnection
) {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    private val GOSYS = "FS22"

    fun onPacket(oppgave: Oppgave) {
        if (oppgave.behandlesAvApplikasjon != GOSYS || oppgave.behandlingstema != "SYK") return
        sikkerlogg.info("mottok endring på gosysoppgave")

        if (oppgave.ident == null) return
        val identType = oppgave.ident.identType
        val aktorId = oppgave.ident.verdi

        val packet: JsonMessage = JsonMessage.newMessage(mapOf(
            "@event_name" to "oppgave_endret",
            "@id" to UUID.randomUUID(),
            "@opprettet" to LocalDateTime.now(),
            "aktørId" to aktorId
        ))

        val fnr = ""
        rapidsConnection.publish(fnr, packet.toJson())
    }
}
