package no.nav.helse.sparkel.stoppknapp.db

import no.nav.helse.sparkel.stoppknapp.Testdata.FØDSELSNUMMER
import no.nav.helse.sparkel.stoppknapp.Testdata.stoppknappMeldingTilDatabase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DaoTest : DatabaseIntegrationTest() {
    @Test
    fun `Kan lagre meldinger`() {
        dao.lagre(stoppknappMeldingTilDatabase())
        assertLagret()
    }

    @Test
    fun `Kan hente meldinger`() {
        dao.lagre(stoppknappMeldingTilDatabase(årsaker = listOf("MEDISINSK_VILKAR")))
        dao.lagre(stoppknappMeldingTilDatabase(årsaker = listOf("MANGLENDE_MEDVIRKING")))
        val meldinger = dao.hent(FØDSELSNUMMER)
        assertEquals(2, meldinger.size)
        assertEquals(listOf("MEDISINSK_VILKAR"), meldinger[0].årsaker)
        assertEquals(listOf("MANGLENDE_MEDVIRKING"), meldinger[1].årsaker)
    }
}
