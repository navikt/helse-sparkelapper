package no.nav.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

internal class InfotrygdSykepengeforsikringerRiver(
    rapidsConnection: RapidsConnection,
    private val forsikringDao: ForsikringDao
) : River.PacketListener {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "InfotrygdSykepengeforsikringer"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id", "fødselsnummer") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("Forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("Fikk behov om forsikring: ${packet.toJson()}")

        val fødselsnummer = packet["fødselsnummer"].asText()

        packet["@løsning"] = mapOf(
            behov to forsikringDao.hentFullstendigeForsikringer(fødselsnummer)
        )

        context.publish(
            packet.toJson().also { json ->
                sikkerlogg.info(
                    "sender svar om forsikring {}:\n\t{}",
                    keyValue("id", packet["@id"].asText()),
                    json
                )
            }
        )
    }
}

