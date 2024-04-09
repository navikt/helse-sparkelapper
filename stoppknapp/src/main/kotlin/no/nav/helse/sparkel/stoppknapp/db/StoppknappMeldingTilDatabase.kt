package no.nav.helse.sparkel.stoppknapp.db

import java.time.LocalDateTime

internal data class StoppknappMeldingTilDatabase(
    val fødselsnummer: String,
    val status: String,
    val årsaker: List<String>,
    val tidsstempel: LocalDateTime,
    val originalMelding: String,
)
