package no.nav.helse.sparkel.stoppknapp.kafka

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingTilDatabase
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

internal class StoppknappMessage(packet: JsonMessage) {
    private val fødselsnummer: String = packet["sykmeldtFnr"]["value"].asText()
    private val status: String = packet["status"].asText()
    private val årsaker: List<String> = packet["arsakList"].map { it["type"].asText() }
    private val tidsstempel: LocalDateTime = utcToLocalDateTime(packet["opprettet"].asText())
    private val originalMelding: String = packet.toJson()

    internal companion object {
        internal fun StoppknappMessage.tilDatabase() =
            StoppknappMeldingTilDatabase(
                fødselsnummer = fødselsnummer,
                status = status,
                årsaker = årsaker,
                tidsstempel = tidsstempel,
                originalMelding = originalMelding,
            )
    }

    private fun utcToLocalDateTime(dateTimeString: String): LocalDateTime =
        OffsetDateTime.parse(dateTimeString).atZoneSameInstant(ZoneId.of("Europe/Oslo")).toLocalDateTime()

}
