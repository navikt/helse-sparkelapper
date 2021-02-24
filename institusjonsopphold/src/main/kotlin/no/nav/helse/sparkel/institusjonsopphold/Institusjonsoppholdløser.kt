package no.nav.helse.sparkel.institusjonsopphold

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.sparkel.institusjonsopphold.Institusjonsoppholdperiode.Companion.filtrer
import org.slf4j.LoggerFactory

internal class Institusjonsoppholdløser(
    rapidsConnection: RapidsConnection,
    private val institusjonsoppholdService: InstitusjonsoppholdService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Institusjonsopphold"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.institusjonsoppholdFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.institusjonsoppholdTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val fom = packet["$behov.institusjonsoppholdFom"].asLocalDate()
        val tom = packet["$behov.institusjonsoppholdTom"].asLocalDate()
        institusjonsoppholdService.løsningForBehov(
            packet["@id"].asText(),
            packet["vedtaksperiodeId"].asText(),
            packet["fødselsnummer"].asText()
        ).let { løsning ->
            packet["@løsning"] = mapOf(
                behov to (løsning?.map { Institusjonsoppholdperiode(it) }?.filtrer(fom, tom) ?: emptyList())
            )
            context.publish(packet.toJson().also { json ->
                sikkerlogg.info(
                    "sender svar {} for {}:\n\t{}",
                    keyValue("id", packet["@id"].asText()),
                    keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                    json
                )
            })
        }
    }
}
