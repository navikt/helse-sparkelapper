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

internal class Forsikringsløser(
    rapidsConnection: RapidsConnection
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Forsikring"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id", "@opprettet", "fødselsnummer", "skjæringstidspunkt") }
        }.register(this)
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sikkerlogg.info("Du er i Severe")
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("Forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("Fikk behov om forsikring. Det er dumt, for det vet jeg ikke hvordan jeg skal løse enda. Sender tilbake tom løsning for nå.")

        packet["@løsning"] = mapOf(
            behov to emptyList<Any>()
        )

        context.publish(packet.toJson().also { json ->
            sikkerlogg.info(
                "sender svar om forsikring {}:\n\t{}",
                keyValue("id", packet["@id"].asText()),
                json
            )
        })
    }
}
