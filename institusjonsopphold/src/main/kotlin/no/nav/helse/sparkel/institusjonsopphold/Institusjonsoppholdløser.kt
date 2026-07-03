package no.nav.helse.sparkel.institusjonsopphold

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.institusjonsopphold.Institusjonsoppholdperiode.Companion.filtrer
import org.slf4j.LoggerFactory
import tools.jackson.databind.JsonNode

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
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.institusjonsoppholdFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.institusjonsoppholdTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val fom = packet["$behov.institusjonsoppholdFom"].asLocalDate()
        val tom = packet["$behov.institusjonsoppholdTom"].asLocalDate()
        institusjonsoppholdService.løsningForBehov(
            packet["@id"].asString(),
            packet["vedtaksperiodeId"].asString(),
            packet["fødselsnummer"].asString()
        ).let { løsning ->
            packet["@løsning"] = mapOf(
                behov to (løsning?.toList()?.map { Institusjonsoppholdperiode(it) }?.filtrer(fom, tom) ?: emptyList())
            )
            context.publish(packet.toJson().also { json ->
                sikkerlogg.info(
                    "sender svar {} for {}:\n\t{}",
                    keyValue("id", packet["@id"].asString()),
                    keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asString()),
                    json
                )
            })
        }
    }
}
