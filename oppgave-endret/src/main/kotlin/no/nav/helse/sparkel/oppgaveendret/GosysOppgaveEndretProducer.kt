package no.nav.helse.sparkel.oppgaveendret

import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.keyValue
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

    fun onPacket(oppgave: Oppgave) {
        if (oppgave.ident == null) {
            logger.info("Oppgave uten ident, {}", keyValue("oppgaveId", oppgave.id))
            return
        }
        val folkeregisterident = oppgave.ident.folkeregisterident
        if (!folkeregisterident.isNullOrEmpty() && oppgave.ident.identType == IdentType.AKTOERID) {
            if (harNettoppSendtMeldingForIdent(folkeregisterident)) {
                sikkerlogg.info("Sender ikke duplikat melding for $folkeregisterident")
                return
            }
            logger.info("Har folkeregisterident og aktorId for oppgave med id: ${oppgave.id}")
            packetAndPublish(folkeregisterident, oppgave.ident.verdi)
        } else {
            sikkerlogg.info("Oppgave: $oppgave")
            logger.error("Mangler folkeregisterident og/eller aktorId for oppgave med id: ${oppgave.id}")
        }
    }

    private fun packetAndPublish(fødselsnummer: String, aktørId: String) {
        val meldingId = UUID.randomUUID()
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "gosys_oppgave_endret",
                "@id" to meldingId,
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fødselsnummer,
                "aktørId" to aktørId
            )
        )
        sikkerlogg.info(
            "Publiserer gosys_oppgave_endret for {}, {}, {}",
            keyValue("@id", meldingId),
            keyValue("fødselsnummer", fødselsnummer),
            keyValue("aktørId", aktørId)
        )
        rapidsConnection.publish(fødselsnummer, packet.toJson())
    }

    private fun harNettoppSendtMeldingForIdent(fnr: String): Boolean {
        if (fnr == forrigeFnr && forrigeOppdatering.plusNanos(throttleDuration.toNanos()) > LocalDateTime.now()) return true
        forrigeFnr = fnr // burde kanskje ha vært en collection (som tømmer seg selv over tid)
        forrigeOppdatering = LocalDateTime.now()
        return false
    }

}
