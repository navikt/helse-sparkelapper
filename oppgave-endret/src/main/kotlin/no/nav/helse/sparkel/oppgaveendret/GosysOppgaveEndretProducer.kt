package no.nav.helse.sparkel.oppgaveendret

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.time.LocalDateTime
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.oppgaveendret.oppgave.Oppgave
import org.slf4j.LoggerFactory

class GosysOppgaveEndretProducer(
    private val rapidsConnection: RapidsConnection
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    private val identer: MutableSet<String> = mutableSetOf()

    fun onPacket(oppgave: Oppgave) {
        logger.info("Har folkeregisterident for oppgave med id: ${oppgave.id}")
        val førsteGang = identer.add(oppgave.ident)
        if (!førsteGang) sikkerlogg.info("Sender ikke duplikat melding for ${oppgave.ident}")
    }

    private fun packetAndPublish(fødselsnummer: String) {
        val meldingId = UUID.randomUUID()
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "gosys_oppgave_endret",
                "@id" to meldingId,
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fødselsnummer
            )
        )
        sikkerlogg.info(
            "Publiserer gosys_oppgave_endret for {}, {}",
            keyValue("@id", meldingId),
            keyValue("fødselsnummer", fødselsnummer)
        )
        rapidsConnection.publish(fødselsnummer, packet.toJson())
    }

    fun shipIt() {
        identer.onEach { ident -> packetAndPublish(ident) }.clear()
    }

}
