package no.nav.helse.sparkel.medlemskapmock

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

internal class Medlemskap(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private const val behov = "Medlemskap"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.medlemskapPeriodeFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.medlemskapPeriodeTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        packet["@løsning"] = mapOf<String, Any>(
            behov to mapOf(
                "resultat" to mapOf(
                    "svar" to "JA"
                )
            )
        )
        context.publish(packet.toJson())
        sikkerlogg.info("Sender hardkodet svar for behov {}:\n{}", keyValue("id", packet["@id"].asText()), packet.toJson())
    }
}
