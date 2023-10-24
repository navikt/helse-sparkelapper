package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

internal class SparkelUtbetalingsperioderMockRiver(
    rapidsConnection: RapidsConnection,
    private val svar: Map<String, List<Utbetalingsperiode>>
) : River.PacketListener {

    private val log = LoggerFactory.getLogger("SparkelUtbetalingsperioderMockRiver")
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "HentInfotrygdutbetalinger"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.require("$behov.historikkFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.historikkTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        log.info("besvarer behov for infotrygdutbetalingshistorikk på id: ${packet["@id"].textValue()}")
        val fødselsnummer = packet["fødselsnummer"].asText()
        val utbetalingsperioder = svar[fødselsnummer]
            ?: run {
                log.info("Fant ikke forhåndskonfigurert infotrygdutbetalingshistorikk blant ${svar.size} forhåndskonfigurerte. Defaulter til en som er tom")
                emptyList()
            }

        packet["@løsning"] = mapOf(
            behov to objectMapper.convertValue(utbetalingsperioder, ArrayNode::class.java)
        )
        context.publish(packet.toJson())
    }
}
