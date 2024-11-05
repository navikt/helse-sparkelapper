package no.nav.helse.sparkel.egenansatt

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

internal class EgenAnsattLøser(
    rapidsConnection: RapidsConnection,
    private val skjermedePersoner: SkjermedePersoner,
) : River.PacketListener {

    companion object {
        internal const val behov = "EgenAnsatt"
    }

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val meldingId = packet["@id"].asText()

        val personErSkjermet = try {
            skjermedePersoner.erSkjermetPerson(packet["fødselsnummer"].asText(), meldingId)
        } catch (err: Exception) {
            loggFeil(err, packet)
            return
        }

        packet.setLøsning(behov, personErSkjermet)

        log.info("løser behov {}", keyValue("id", meldingId))
        sikkerlogg.info("løser behov {}", keyValue("id", meldingId))

        context.publish(packet.toJson())
    }

    private fun loggFeil(err: Exception, packet: JsonMessage) {
        val idArgument = keyValue("id", packet["@id"].asText())
        log.error("feil ved henting av egen ansatt: ${err.message} for behov {}", idArgument, err)
        sikkerlogg.error("feil ved henting av egen ansatt: ${err.message} for behov {}", idArgument, err)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {}

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }

}
