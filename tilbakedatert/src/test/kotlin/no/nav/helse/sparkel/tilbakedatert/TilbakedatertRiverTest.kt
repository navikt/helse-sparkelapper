package no.nav.helse.sparkel.tilbakedatert

import java.time.LocalDate
import java.time.LocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import no.nav.helse.rapids_rivers.asLocalDate
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class TilbakedatertRiverTest {

    private lateinit var rapid: TestRapid

    @BeforeAll
    fun setup() {
        rapid = TestRapid().apply {
            TilbakedatertRiver(this)
        }
    }

    @BeforeEach
    fun clear() {
        rapid.reset()
    }

    private fun testEvent(behov: String) {
        TilbakedatertRiver(rapid)
        rapid.sendTestMessage(behov)
    }

    private fun enkeltEvent(fom: LocalDate? = LocalDate.now().minusDays(4), signaturDato: LocalDateTime? = LocalDateTime.now(), merknader: String? = null) =
        """
        {
             "sykmelding": {
                "id": "${UUID.randomUUID()}",
                "syketilfelleStartDato": "$fom",
                "signaturDato": "$signaturDato"
            },
            "personNrPasient": "12345678910",
            "merknader": $merknader
        }
        """

    @Test
    fun `Sender tilbakedatering_behandlet dersom sykmelding er tilbakedatert og godkjent`() {
        testEvent(enkeltEvent())

        val svar = rapid.inspektør.message(0)
        assertEquals(svar["@event_name"].asText(), "tilbakedatering_behandlet")
        assertEquals(svar["syketilfelleStartDato"].asLocalDate(), LocalDate.now().minusDays(4))
        assertEquals(svar["fødselsnummer"].asText(), "12345678910")
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom sykmelding ikke er tilbakedatert`() {
        testEvent(enkeltEvent(fom = LocalDate.now(), signaturDato = LocalDateTime.now()))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `Sender ikke tilbakedatering_behandlet dersom sykmelding er tilbakedatert men er UNDER_BEHANDLING`() {
        testEvent(enkeltEvent(merknader = "[{\"type\":\"UNDER_BEHANDLING\"}]"))
        assertEquals(0, rapid.inspektør.size)
    }
}
