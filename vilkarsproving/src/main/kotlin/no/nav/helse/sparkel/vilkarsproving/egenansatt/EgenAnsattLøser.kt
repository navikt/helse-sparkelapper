package no.nav.helse.sparkel.vilkarsproving.egenansatt

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

internal class EgenAnsattLøser(
    rapidsConnection: RapidsConnection,
    private val skjermedePersoner: SkjermedePersoner,
) :
    River.PacketListener {

    companion object {
        internal const val behov = "EgenAnsatt"
    }

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.requireContains("@behov", behov) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        try {
            sikkerlogg.info("mottok melding: ${packet.toJson()}")
            skjermedePersoner.erSkjermetPerson(packet["fødselsnummer"].asText(), packet["@id"].asText()).also {
                packet.setLøsning(behov, it)
            }

            log.info(
                "løser behov {}",
                keyValue("id", packet["@id"].asText())
            )
            sikkerlogg.info(
                "løser behov {}",
                keyValue("id", packet["@id"].asText())
            )

            context.publish(packet.toJson())
        } catch (err: Exception) {
            log.error(
                "feil ved henting av egen ansatt: ${err.message} for behov {}",
                keyValue("id", packet["@id"].asText()),
                err
            )
            sikkerlogg.error(
                "feil ved henting av egen ansatt: ${err.message} for behov {}",
                keyValue("id", packet["@id"].asText()),
                err
            )
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {}

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }

}
