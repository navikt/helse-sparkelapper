package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr"
        }
        """

        val løsning = objectMapper.readTree(
            """
            {
              "ident": "fnr",
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )


        every { personinfoService.løsningForPersoninfo(any(), any()) } returns løsning
        rapid.sendTestMessage(behov)

        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
    }

    @Test
    fun `besvarer komposittbehov`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentNoeAnnetOgså", "HentPersoninfoV2" ],
            "@id" : "id",
            "@opprettet" : "2021-11-17",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr"
        }
        """

        val løsning = objectMapper.readTree(
            """
            {
              "ident": "fnr",
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )


        every { personinfoService.løsningForPersoninfo(any(), any()) } returns løsning
        rapid.sendTestMessage(behov)

        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
    }

    @Test
    fun `besvarer behov med egen ident`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "id",
            "@opprettet" : "2021-11-17",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "HentPersoninfoV2": {
                "ident": "bruker denne"
            }
        }
        """

        val løsning = objectMapper.readTree(
            """
            {
              "ident": "bruker denne",
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )

        every { personinfoService.løsningForPersoninfo(any(), any()) } returns løsning
        rapid.sendTestMessage(behov)
        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
        verify { personinfoService.løsningForPersoninfo(any(), eq("bruker denne")) }
    }

    @Test
    fun `besvarer behov med flere identer`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "id",
            "@opprettet" : "2021-11-17",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "HentPersoninfoV2": {
                "ident": ["ident1", "ident2"]
            }
        }
        """

        val løsning1 = objectMapper.readTree(
            """
            {
              "ident": "ident1",
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )

        val løsning2 = objectMapper.readTree(
            """
            {
              "ident": "ident2",
              "fornavn": "STOR",
              "mellomnavn": null,
              "etternavn": "HYGGE",
              "fødselsdato": "1980-01-01",
              "kjønn": "Mann",
              "adressebeskyttelse": "Ugradert"
            }
        """
        )

        every { personinfoService.løsningForPersoninfo(any(), eq("ident1")) } returns løsning1
        every { personinfoService.løsningForPersoninfo(any(), eq("ident2")) } returns løsning2
        rapid.sendTestMessage(behov)
        assertEquals(løsning1, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"][0])
        assertEquals(løsning2, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"][1])
    }

    @Test
    fun `besvarer behov med ingen identer`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "id",
            "@opprettet" : "2021-11-17",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "HentPersoninfoV2": {
                "ident": []
            }
        }
        """

        rapid.sendTestMessage(behov)
        val løsning = rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"]
        println(rapid.inspektør.message(0).toPrettyString())
        assertTrue(løsning.isArray)
        assertTrue(løsning.isEmpty)
    }
}