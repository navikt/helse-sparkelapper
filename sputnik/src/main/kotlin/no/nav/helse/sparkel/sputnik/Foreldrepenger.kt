package no.nav.helse.sparkel.sputnik

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import org.slf4j.LoggerFactory

internal class Foreldrepenger(
    rapidsConnection: RapidsConnection,
    private val foreldrepengerløser: Foreldrepengerløser
) : River.PacketListener {

    private companion object {
        private val log = LoggerFactory.getLogger(Foreldrepenger::class.java)
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf("Foreldrepenger")) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("aktørId") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate {
                it.require("Foreldrepenger.foreldrepengerFom", JsonNode::asLocalDate)
                it.require("Foreldrepenger.foreldrepengerTom", JsonNode::asLocalDate)
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke Foreldrepenger:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        try {
            val aktørId = packet["aktørId"].asText()
            val fom = packet["Foreldrepenger.foreldrepengerFom"].asLocalDate()
            val tom = packet["Foreldrepenger.foreldrepengerTom"].asLocalDate()

            runBlocking { foreldrepengerløser.hent(aktørId, fom, tom) }.also {
                packet["@løsning"] = mapOf(
                    "Foreldrepenger" to it
                )
            }

            log.info(
                "løser behov={} for {}",
                keyValue("id", packet["@id"].asText()),
                keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText())
            )
            sikkerlogg.info(
                "løser behov={} for {} = ${packet.toJson()}",
                keyValue("id", packet["@id"].asText()),
                keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText())
            )

            context.publish(packet.toJson())
        } catch (err: Exception) {
            log.warn(
                "feil ved henting av foreldrepenger-data: ${err.message} for {}",
                keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                err
            )
            sikkerlogg.warn(
                "feil ved henting av foreldrepenger-data: ${err.message} for {}",
                keyValue("vedtaksperiodeId", packet["vedtaksperiodeId"].asText()),
                err
            )
        }
    }
}

