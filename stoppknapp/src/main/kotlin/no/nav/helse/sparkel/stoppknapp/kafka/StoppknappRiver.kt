package no.nav.helse.sparkel.stoppknapp.kafka

import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal class StoppknappRiver(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    private val logg = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandKey("sykmeldtFnr")
                it.demandKey("status")
                it.interestedIn("arsakList")
                it.demandKey("opprettet")
            }
        }.register(this)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        logg.error("Forstod ikke stoppknapp-melding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        sikkerlogg.info("Leser stoppknapp-melding: ${packet.toJson()}")
        håndter(packet, context)
    }

    private fun håndter(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val fødselsnummer: String = packet["sykmeldtFnr"]["value"].asText()
        val status: String = packet["status"].asText()
        val årsaker: List<String> = packet["arsakList"].map { it["type"].asText() }
        val tidsstempel: LocalDateTime = utcToLocalDateTime(packet["opprettet"].asText())
        val originalMelding: String = packet.toJson()

        val returEvent =
            JsonMessage.newMessage(
                eventName = "stans_automatisk_behandling",
                map =
                    mapOf(
                        "fødselsnummer" to fødselsnummer,
                        "status" to status,
                        "årsaker" to årsaker,
                        "tidsstempel" to tidsstempel,
                        "originalMelding" to originalMelding,
                    ),
            )

        context.publish(returEvent.toJson()).also {
            sikkerlogg.info(
                "sender stans_automatisk_behandling: {}",
                kv("stans_automatisk_behandling", returEvent),
            )
        }
    }

    private fun utcToLocalDateTime(dateTimeString: String): LocalDateTime =
        OffsetDateTime.parse(dateTimeString).atZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}
