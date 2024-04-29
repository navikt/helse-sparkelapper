package no.nav.helse.sparkel.stoppknapp.kafka

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.stoppknapp.Testdata.FØDSELSNUMMER
import no.nav.helse.sparkel.stoppknapp.Testdata.STATUS
import no.nav.helse.sparkel.stoppknapp.Testdata.ÅRSAK
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.opphevingAvStansMelding
import no.nav.helse.sparkel.stoppknapp.Testmeldinger.stoppknappMelding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StoppknappRiverTest {
    private val testRapid = TestRapid()

    init {
        StoppknappRiver(testRapid)
    }

    @Test
    fun `Kan lese inn stoppknappmeldinger fra iSyfo`() {
        testRapid.sendTestMessage(stoppknappMelding())
        val svar = testRapid.inspektør.message(0)

        assertEquals(svar["@event_name"].asText(), "stans_automatisk_behandling")
        assertEquals(svar["status"].asText(), STATUS)
        assertEquals(svar["årsaker"].map { it.asText() }, listOf(ÅRSAK))
        assertEquals(svar["fødselsnummer"].asText(), FØDSELSNUMMER)
    }

    @Test
    fun `Kan lese inn melding om oppheving av stans fra isyfo`() {
        testRapid.sendTestMessage(opphevingAvStansMelding())
        val svar = testRapid.inspektør.message(0)

        assertEquals(svar["@event_name"].asText(), "stans_automatisk_behandling")
        assertEquals(svar["status"].asText(), "NORMAL")
        assertEquals(svar["fødselsnummer"].asText(), FØDSELSNUMMER)
    }
}
