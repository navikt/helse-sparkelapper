package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory

internal class SparkelSykepengeperioderMockRiver(
    rapidsConnection: RapidsConnection,
    private val svar: Map<String, List<Sykepengehistorikk>>
) : River.PacketListener {

    private val log = LoggerFactory.getLogger("SparkelSykepengeperioderMockRiver")
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Sykepengehistorikk"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.require("$behov.historikkFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.historikkTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        log.info("besvarer behov for sykepengehistorikk på id: ${packet["@id"].textValue()}")
        val fødselsnummer = packet["fødselsnummer"].asText()
        val sykepengeperioder = svar[fødselsnummer]
            ?: run {
                log.info("Fant ikke forhåndskonfigurert sykepengehistorikk blant ${svar.size} forhåndskonfigurerte. Defaulter til en som er tom")
                emptyList()
            }
        packet["@løsning"] = mapOf(
            behov to objectMapper.convertValue(sykepengeperioder, ArrayNode::class.java)
        )
        context.publish(packet.toJson())
    }
}
