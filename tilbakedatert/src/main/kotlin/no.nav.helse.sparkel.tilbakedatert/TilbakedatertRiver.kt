package no.nav.helse.sparkel.tilbakedatert

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.isMissingOrNull
import org.slf4j.LoggerFactory

internal class TilbakedatertRiver(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {

    companion object {
        private val objectMapper = ObjectMapper()
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(TilbakedatertRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandKey("sykmelding")
                it.demandKey("personNrPasient")
                it.demandKey("sykmelding.signaturDato")
                it.requireKey("sykmelding.syketilfelleStartDato")
                it.interestedIn("merknader")
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke sykmelding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info("Leser melding ${packet["sykmelding"]["id"]}")
        håndter(packet, context)
    }

    private fun håndter(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("Leser melding ${packet.toJson()}")
        val fødselsnummer = packet["personNrPasient"].asText()
        val sykmeldingId = packet["sykmelding"]["id"].asText()
        val syketilfelleStartDato = packet["sykmelding"]["syketilfelleStartDato"].asLocalDate()
        val signaturDato = packet["sykmelding"]["signaturDato"].asLocalDateTime()
        val erTilbakedatert = syketilfelleStartDato < LocalDate.from(signaturDato.minusDays(3))
        val erUnderManuellBehandling = packet["merknader"].takeUnless { it.isMissingOrNull() }?.find {
            it["type"].asText() == "UNDER_BEHANDLING"
        } ?: false

        if (erTilbakedatert && erUnderManuellBehandling == false) {
            val returEvent = objectMapper.createObjectNode()
                .put("@event_name", "tilbakedatering_behandlet")
                .put("@id", "${UUID.randomUUID()}")
                .put("@opprettet", "${LocalDateTime.now()}")
                .put("fødselsnummer", fødselsnummer)
                .put("sykmeldingId", sykmeldingId)
                .put("syketilfelleStartDato", "$syketilfelleStartDato")

            context.publish(returEvent.toString()).also {
                sikkerlogg.info(
                    "sender tilbakedatering_behandlet for {}: {}",
                    StructuredArguments.keyValue("sykmeldingId", sykmeldingId),
                    packet.toJson()
                )
            }
        }
    }
}
