package no.nav.helse.sparkel.stoppknapp

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal class StoppknappRiver(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireKey("sykmeldtFnr")
                it.requireKey("status")
                it.requireKey("opprettet")
            }
            validate { it.interestedIn("arsakList") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("Forstod ikke stoppknapp-melding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("Leser stoppknapp-melding:\n{}", packet.toJson())

        val fødselsnummer: String = packet["sykmeldtFnr"]["value"].asText()
        val opprettet: LocalDateTime = utcToLocalDateTime(packet["opprettet"].asText())
        val status: String = packet["status"].asText()
        val årsaker: List<String> = packet["arsakList"].map { it["type"].asText() }
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
