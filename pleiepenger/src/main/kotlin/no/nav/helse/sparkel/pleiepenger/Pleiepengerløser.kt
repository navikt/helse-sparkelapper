package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdService
import no.nav.helse.sparkel.pleiepenger.infotrygd.Stønadsperiode
import org.slf4j.LoggerFactory

internal class Pleiepengerløser(
    rapidsConnection: RapidsConnection,
    private val infotrygdService: InfotrygdService
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        const val behov = "Pleiepenger"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.pleiepengerFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.pleiepengerTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        if (packet["vedtaksperiodeId"].asText() == "2fa6c5cb-b43b-424f-8da3-11bae421b678") {
            log.warn(
                "Svarer med hardkodet respons for {} for {}",
                kv("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                kv("fnr", packet["fødselsnummer"].asText())
            )
            packet["@løsning"] = mapOf(
                behov to emptyList<Stønadsperiode>()
            )
            context.publish(packet.toJson().also { json ->
                sikkerlogg.info(
                    "sender svar {} for {}:\n\t{}",
                    keyValue("id", packet["@id"].asText()),
                    keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                    json
                )
            })
            return
        }
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        infotrygdService.løsningForBehov(
            Stønadsperiode.Stønadstype.PLEIEPENGER,
            packet["@id"].asText(),
            packet["vedtaksperiodeId"].asText(),
            packet["fødselsnummer"].asText(),
            packet["$behov.pleiepengerFom"].asLocalDate(),
            packet["$behov.pleiepengerTom"].asLocalDate()
        )?.let { løsning ->
            packet["@løsning"] = mapOf(
                behov to løsning
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
