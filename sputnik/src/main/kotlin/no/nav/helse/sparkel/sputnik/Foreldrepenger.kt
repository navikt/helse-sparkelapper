package no.nav.helse.sparkel.sputnik

import java.time.LocalDate.MAX
import java.time.LocalDate.MIN
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
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
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke Foreldrepenger:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        try {
            runBlocking { foreldrepengerløser.hent(packet["aktørId"].asText(), MIN, MAX) }.also {
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

