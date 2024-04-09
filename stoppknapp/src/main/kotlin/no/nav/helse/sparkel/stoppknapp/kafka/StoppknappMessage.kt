package no.nav.helse.sparkel.stoppknapp.kafka

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingTilDatabase
import java.time.LocalDateTime

internal class StoppknappMessage(packet: JsonMessage) {
    private val fødselsnummer: String = packet["sykmeldtFnr"]["value"].asText()
    private val status: String = packet["status"].asText()
    private val årsaker: List<String> = packet["arsakList"].map { it["type"].asText() }
    private val tidsstempel: LocalDateTime = packet["opprettet"].asLocalDateTime()
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
}
