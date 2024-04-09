package no.nav.helse.sparkel.stoppknapp.kafka

import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.stoppknapp.Mediator
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.automatiseringStoppetAvVeilederBehov
import org.junit.jupiter.api.Test

internal class AutomatiseringStoppetAvVeilederLøserTest {
    private val testRapid = TestRapid()
    private val mediatorMock = mockk<Mediator>(relaxed = true)

    init {
        AutomatiseringStoppetAvVeilederLøser(testRapid, mediatorMock)
    }

    @Test
    fun `Kan lese inn AutomatiseringStoppetAvVeileder-behov`() {
        testRapid.sendTestMessage(automatiseringStoppetAvVeilederBehov())
        verify(exactly = 1) { mediatorMock.erAutomatiseringStoppet(any()) }
    }
}
