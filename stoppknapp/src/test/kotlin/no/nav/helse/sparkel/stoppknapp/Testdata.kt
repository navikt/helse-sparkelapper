package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.sparkel.stoppknapp.Testmeldinger.stoppknappMelding
import no.nav.helse.sparkel.stoppknapp.db.StoppknappMeldingTilDatabase
import java.time.LocalDateTime

internal object Testdata {
    internal const val FØDSELSNUMMER: String = "12345678910"
    internal const val STATUS: String = "STOPP_AUTOMATIKK"
    internal const val ÅRSAK: String = "MEDISINSK_VILKAR"
    internal val TIDSSTEMPEL: LocalDateTime = LocalDateTime.now()

    internal fun stoppknappMeldingTilDatabase() =
        StoppknappMeldingTilDatabase(
            fødselsnummer = FØDSELSNUMMER,
            status = STATUS,
            årsaker = listOf(ÅRSAK, "MANGLENDE_MEDVIRKING"),
            tidsstempel = TIDSSTEMPEL,
            originalMelding = stoppknappMelding(),
        )
}
