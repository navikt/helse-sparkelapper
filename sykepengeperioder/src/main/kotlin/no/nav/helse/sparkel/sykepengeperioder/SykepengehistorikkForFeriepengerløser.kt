package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

internal class SykepengehistorikkForFeriepengerløser(
    rapidsConnection: RapidsConnection,
    private val infotrygdService: InfotrygdService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "SykepengehistorikkForFeriepenger"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("@opprettet") }
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
        infotrygdService.løsningForSykepengehistorikkForFeriepenger(
            packet["@id"].asText(),
            Fnr(packet["fødselsnummer"].asText()),
            packet["$behov.historikkFom"].asLocalDate(),
            packet["$behov.historikkTom"].asLocalDate()
        )
            .takeIf { it != null }
            ?.let { løsning ->
                packet["@løsning"] = mapOf(
                    behov to løsning
                )
                context.publish(packet.toJson().also { json ->
                    sikkerlogg.info(
                        "sender svar {}:\n\t{}",
                        keyValue("id", packet["@id"].asText()),
                        json
                    )
                })
            }
    }
}
