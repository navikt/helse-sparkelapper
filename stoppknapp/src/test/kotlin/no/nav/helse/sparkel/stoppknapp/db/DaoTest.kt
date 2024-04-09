package no.nav.helse.sparkel.stoppknapp.db

import no.nav.helse.sparkel.stoppknapp.Testdata.stoppknappMeldingTilDatabase
import org.junit.jupiter.api.Test

internal class DaoTest : DatabaseIntegrationTest() {
    @Test
    fun `kan lagre stoppknapmeldinger`() {
        dao.lagre(stoppknappMeldingTilDatabase())
        assertLagret()
    }
}
