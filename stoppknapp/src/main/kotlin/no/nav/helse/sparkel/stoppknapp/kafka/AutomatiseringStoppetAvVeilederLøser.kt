package no.nav.helse.sparkel.stoppknapp.kafka

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.stoppknapp.Mediator
import org.slf4j.LoggerFactory

internal class AutomatiseringStoppetAvVeilederLøser(
    rapidsConnection: RapidsConnection,
    private val mediator: Mediator,
) :
    River.PacketListener {
    private companion object {
        private const val BEHOV = "AutomatiseringStoppetAvVeileder"
    }

    private val logg = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf(BEHOV))
                it.forbid("@løsning")
                it.requireKey("@id")
                it.requireKey("fødselsnummer")
            }
        }.register(this)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        logg.error("Forstod ikke AutomatiseringStoppetAvVeileder-behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val id = packet["@id"].asText()
        val fødselsnummer = packet["fødselsnummer"].asText()

        val løsning = mediator.erAutomatiseringStoppet(fødselsnummer)

        packet.setLøsning(BEHOV, løsning)

        logg.info("løser behov {}", keyValue("id", id))
        sikkerlogg.info("løser behov {}", keyValue("id", id))

        context.publish(packet.toJson())
    }

    private fun JsonMessage.setLøsning(
        nøkkel: String,
        data: Any,
    ) {
        this["@løsning"] =
            mapOf(
                nøkkel to data,
            )
    }
}
