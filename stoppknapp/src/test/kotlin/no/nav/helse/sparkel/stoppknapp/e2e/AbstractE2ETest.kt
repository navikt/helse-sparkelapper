package no.nav.helse.sparkel.stoppknapp.e2e

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.stoppknapp.Mediator
import no.nav.helse.sparkel.stoppknapp.TestRapidHelpers.løsning
import no.nav.helse.sparkel.stoppknapp.Testdata.ÅRSAK
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.automatiseringStoppetAvVeilederBehov
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.stoppknappMelding
import no.nav.helse.sparkel.stoppknapp.db.DatabaseIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail

internal abstract class AbstractE2ETest : DatabaseIntegrationTest() {
    private val testRapid = TestRapid()
    private val mediator = Mediator(testRapid, dao)

    @BeforeEach
    internal fun resetTestSetup() {
        testRapid.reset()
    }

    protected fun sendStoppknappMelding(
        årsak: String = ÅRSAK,
        status: String = "STOPP_AUTOMATIKK",
    ) = testRapid.sendTestMessage(stoppknappMelding(årsak = årsak, status = status))

    protected fun sendAutomatiseringStoppetAvVeilederBehov() =
        testRapid.sendTestMessage(
            automatiseringStoppetAvVeilederBehov(),
        )

    protected fun assertStoppknappMeldingLagret() = assertLagret()

    protected fun assertAutomatiseringStoppetAvVeilederBehovBesvart(
        automatiseringStoppet: Boolean,
        årsaker: Set<String>,
    ) {
        val løsning =
            testRapid.inspektør.løsning("AutomatiseringStoppetAvVeileder")
                ?: fail("Forventet å finne svar på AutomatiseringStoppetAvVeileder-behov")
        assertTrue(løsning.path("automatiseringStoppet").isBoolean)
        assertEquals(automatiseringStoppet, løsning.path("automatiseringStoppet").booleanValue())
        assertEquals(årsaker, løsning.path("årsaker").map { it.asText() }.toSet())
    }
}
