package no.nav.helse.sparkel.stoppknapp

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
        sikkerlogg.error("Forstod ikke stoppknapp-melding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        sikkerlogg.info("Leser stoppknapp-melding:\n{}", packet.toJson())

        val fødselsnummer: String = packet["sykmeldtFnr"]["value"].asText()
        val status: String = packet["status"].asText()
        val årsaker: List<String> = packet["arsakList"].map { it["type"].asText() }
        val opprettet: LocalDateTime = utcToLocalDateTime(packet["opprettet"].asText())
        val originalMelding: String = packet.toJson()

        val returEvent =
            JsonMessage.newMessage(
                eventName = "stans_automatisk_behandling",
                map =
                    mapOf(
                        "fødselsnummer" to fødselsnummer,
                        "status" to status,
                        "årsaker" to årsaker,
                        "opprettet" to opprettet,
                        "originalMelding" to originalMelding,
                    ),
            )

        context.publish(fødselsnummer, returEvent.toJson()).also {
            sikkerlogg.info("sender stans_automatisk_behandling:\n{}", returEvent.toJson())
        }
    }

    private fun utcToLocalDateTime(dateTimeString: String): LocalDateTime =
        OffsetDateTime.parse(dateTimeString).atZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()
}
