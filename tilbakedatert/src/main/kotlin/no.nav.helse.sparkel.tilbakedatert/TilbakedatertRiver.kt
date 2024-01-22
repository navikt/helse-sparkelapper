package no.nav.helse.sparkel.tilbakedatert

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
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
            validate { it.demandKey("sykmelding") }
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
        sikkerlogg.info("Leser melding ${objectMapper.readTree(packet.toJson())}")
        return;
//        val fnr = packet["fødselsnummer"].asText()
//        val id = packet["@id"].asText()
//        val sykmeldingId = packet["sykmeldingId"].asText()
//        val erTilbakedatertSøknadHåndtert = false;
//
//        if (erTilbakedatertSøknadHåndtert) {
//            val returEvent = objectMapper.createObjectNode()
//                .put("@event_name", "tilbakedatering_behandlet")
//                .put("fødselsnummer", fnr)
//                .put("godkjent", true)
//                .put("sykmeldingId", sykmeldingId)
//
//                context.publish(returEvent.toString()).also {
//                    sikkerlogg.info(
//                        "sender {} som {}",
//                        StructuredArguments.keyValue("id", id),
//                        packet.toJson()
//                    )
//                }
//        }

    }
}
