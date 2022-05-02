package no.nav.helse.sparkel.oppgaveendret

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.oppgave.IdentType
import no.nav.helse.sparkel.oppgaveendret.oppgave.Oppgave
import no.nav.helse.sparkel.oppgaveendret.pdl.PdlClient
import org.slf4j.LoggerFactory

class GosysOppgaveSykEndretProducer(
    private val rapidsConnection: RapidsConnection,
    private val pdlClient: PdlClient
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val GOSYS = "FS22"

    fun onPacket(oppgave: Oppgave) {
        //if (oppgave.behandlesAvApplikasjon != GOSYS || oppgave.behandlingstema != "SYK") return
        // logger.info("Mottok endring på gosysoppgave på behandlingstema SYK")
        logger.info("tema: " +  oppgave.tema + " behandlesAvApplikasjon: " + oppgave.behandlesAvApplikasjon)
        if ( oppgave.tema != "SYK") return
        logger.info("Mottok endring på behandlingstema SYK")
        logger.info("behandlesAvApplikasjon: " + oppgave.behandlesAvApplikasjon)

        if (oppgave.ident == null) return

        if (!oppgave.ident.folkeregisterident.isNullOrEmpty() && oppgave.ident.identType == IdentType.AKTOERID) {
            logger.info("Har folkeregisterident og aktorId på oppgaven")
           // packetAndPublish(oppgave.ident.folkeregisterident, oppgave.ident.verdi)

        } else if (!oppgave.ident.folkeregisterident.isNullOrEmpty()) {
            logger.info("Har folkeregisterident på oppgaven")
            val hendelseId = UUID.randomUUID().toString()
            val identer = pdlClient.hentIdenter(oppgave.ident.folkeregisterident, hendelseId)
            logger.info("pdl kallet gikk fint!")

            //packetAndPublish(identer.fødselsnummer, identer.aktørId)

        } else if (oppgave.ident.identType == IdentType.AKTOERID) {
            logger.info("Har aktorId på oppgaven")
            val hendelseId = UUID.randomUUID().toString()
            val identer = pdlClient.hentIdenter(oppgave.ident.verdi, hendelseId)
            logger.info("pdl kallet gikk fint!")

            //packetAndPublish(identer.fødselsnummer, identer.aktørId)
        }

    }

    private fun packetAndPublish(fødselsnummer: String, aktørId: String) {
        val packet: JsonMessage = JsonMessage.newMessage(
            mapOf(
                "@event_name" to "gosys_syk_oppgave_endret",
                "@id" to UUID.randomUUID(),
                "@opprettet" to LocalDateTime.now(),
                "fødselsnummer" to fødselsnummer,
                "aktørId" to aktørId
            )
        )
        rapidsConnection.publish(fødselsnummer, packet.toJson())
    }

}
