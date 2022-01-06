package no.nav.helse.sparkel.vilkarsproving.egenansatt

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.sparkel.vilkarsproving.logger
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest
import org.slf4j.LoggerFactory

internal class EgenAnsattLøser(
    rapidsConnection: RapidsConnection,
    private val egenAnsattService: EgenAnsattV1,
    private val nom: NOM,
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
            egenAnsattService.hentErEgenAnsattEllerIFamilieMedEgenAnsatt(
                WSHentErEgenAnsattEllerIFamilieMedEgenAnsattRequest().withIdent(packet["fødselsnummer"].asText())
            ).isEgenAnsatt.also {
                packet.setLøsning(behov, it)
            }

            try {
                nom.erEgenAnsatt(packet["fødselsnummer"].asText(), packet["@id"].asText())
            } catch (e: Exception) {
                logger.info("Exception ifm kall til NOM - ignoreres under utvikling", e)
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
