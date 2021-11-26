package no.nav.helse.sparkel.norg

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.LocalDate


internal class HentPersoninfoRiverTest {

    private val rapid = TestRapid()
        .apply { HentPersoninfoRiver(this, mockk {
            coEvery { finnPerson(any(), any()) }.returns(
                Person("Test", null, "Testsen", LocalDate.of(1986, 2, 23), Kjønn.Mann, Adressebeskyttelse.UGRADERT)
            )
        }) }

    @Test
    fun `løser personoppslagbehov`() {
        rapid.sendTestMessage(behov)
        val løsning = rapid.inspektør.message(0)["@løsning"]["HentPersoninfo"]
        assertEquals("Test", løsning["fornavn"].asText())
        assertFalse(løsning.hasNonNull("mellomnavn"))
        assertEquals("Testsen", løsning["etternavn"].asText())
        assertEquals(LocalDate.of(1986, 2, 23), løsning["fødselsdato"].asText().let(LocalDate::parse))
        assertEquals(Kjønn.Mann.name, løsning["kjønn"].asText())
    }
}

@Language("JSON")
val behov = """{
  "@event_name": "behov",
  "@behov": [
    "HentPersoninfo",
    "HentEnhet"
  ],
  "@id": "2dad52b1-f58e-4c26-bb24-970705cdea67",
  "@opprettet": "2020-05-05T11:16:12.678539",
  "spleisBehovId": "c2d3ce2e-abeb-4c27-a7d3-e45f23ef26f7",
  "fødselsnummer": "12345",
  "orgnummer": "89123"
}
"""
