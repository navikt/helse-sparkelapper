package no.nav.helse.sparkel.personinfo

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

internal class Personinfoløser(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "Dødsinfo"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke behov $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val behovId = packet["@id"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText()
        val fnr = packet["fødselsnummer"].asText()
        try {
            personinfoService.løsningForBehov(behovId, vedtaksperiodeId, fnr).let {
                packet["@løsning"] = mapOf(behov to it)
            }
            packet.toJson().let { løsningJson ->
                context.publish(løsningJson)
                sikkerlogg.info(
                    "sender svar {} for {}:\n\t{}",
                    keyValue("id", behovId),
                    keyValue("vedtaksperiodeId", vedtaksperiodeId),
                    løsningJson
                )
            }
        } catch (e: Exception) {
            log.warn(
                "Feil under løsing av personinfo-behov for {}: ${e.message}",
                keyValue("vedtaksperiodeId", vedtaksperiodeId),
                e
            )
        }
    }
}
