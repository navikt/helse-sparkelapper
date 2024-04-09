package no.nav.helse.sparkel.stoppknapp.kafka

import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.stoppknapp.Mediator
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.stoppknappMelding
import org.junit.jupiter.api.Test

internal class StoppknappRiverTest {
    private val testRapid = TestRapid()
    private val mediatorMock = mockk<Mediator>(relaxed = true)

    init {
        StoppknappRiver(testRapid, mediatorMock)
    }

    @Test
    fun `Kan lese inn stoppknappmeldinger fra iSyfo`() {
        testRapid.sendTestMessage(stoppknappMelding())
        verify(exactly = 1) { mediatorMock.lagre(any()) }
    }
}
