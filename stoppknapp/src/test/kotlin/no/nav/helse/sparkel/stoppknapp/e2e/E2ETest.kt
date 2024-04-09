package no.nav.helse.sparkel.stoppknapp.e2e

import no.nav.helse.sparkel.stoppknapp.Testdata.ÅRSAK
import org.junit.jupiter.api.Test

internal class E2ETest : AbstractE2ETest() {
    @Test
    fun `Lagrer stoppknappmelding når iSyfo melding leses inn`() {
        sendStoppknappMelding()
        assertStoppknappMeldingLagret()
    }

    @Test
    fun `Løser AutomatiseringStoppetAvVeileder-behov - uten stoppknappmelding`() {
        sendAutomatiseringStoppetAvVeilederBehov()
        assertAutomatiseringStoppetAvVeilederBehovBesvart(false, emptySet())
    }

    @Test
    fun `Løser AutomatiseringStoppetAvVeileder-behov - med én stoppknappmelding`() {
        sendStoppknappMelding()
        sendAutomatiseringStoppetAvVeilederBehov()
        assertAutomatiseringStoppetAvVeilederBehovBesvart(true, setOf(ÅRSAK))
    }

    @Test
    fun `Løser AutomatiseringStoppetAvVeileder-behov - med flere stoppknappmeldinger`() {
        sendStoppknappMelding(årsak = "MEDISINSK_VILKAR")
        sendStoppknappMelding(årsak = "MANGLENDE_MEDVIRKING")
        sendAutomatiseringStoppetAvVeilederBehov()
        assertAutomatiseringStoppetAvVeilederBehovBesvart(true, setOf("MEDISINSK_VILKAR", "MANGLENDE_MEDVIRKING"))
    }

    @Test
    fun `Løser AutomatiseringStoppetAvVeileder-behov etter status har endret seg`() {
        sendStoppknappMelding()
        sendStoppknappMelding(status = "NORMAL")
        sendAutomatiseringStoppetAvVeilederBehov()
        assertAutomatiseringStoppetAvVeilederBehovBesvart(false, emptySet())
    }
}
