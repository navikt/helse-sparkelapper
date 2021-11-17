package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HentPersoninfoV2LøserTest {
    private val rapid = TestRapid()
    private val personinfoService = mockk<PersoninfoService>(relaxed = true)
    private val hentPersoninfoV2Løser = HentPersoninfoV2Løser(rapid, personinfoService)
    private val objectMapper = jacksonObjectMapper()


    @Test
    fun `besvarer behov`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "id",
            "@opprettet" : "2021-11-17",
            "spleisBehovId" : "spleisBehovId",
            "fødselsnummer" : "fnr"
        }
        """

        val løsning = objectMapper.readTree(
            """
            {
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )


        every { personinfoService.løsningForPersoninfo(any(), any(), any()) } returns løsning
        rapid.sendTestMessage(behov)

        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
    }
}