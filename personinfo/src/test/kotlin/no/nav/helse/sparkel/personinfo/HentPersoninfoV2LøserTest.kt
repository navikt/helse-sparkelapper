package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.PersonResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.LocalDate
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class HentPersoninfoV2LøserTest {
    private val personinfoService = mockk<PersoninfoService>(relaxed = true)
    private val objectMapper = jacksonObjectMapper()
    private val rapid = TestRapid().apply {
        HentPersoninfoV2Løser(this, personinfoService, objectMapper)
    }


    @Test
    fun `besvarer behov`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "${UUID.randomUUID()}",
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


        every { personinfoService.løsningForPersoninfo(any(), any()) } returns PersonResponse(
            fødselsdato = LocalDate.of(1976, 4, 9),
            dødsdato = null,
            fornavn = "LITEN",
            mellomnavn = null,
            etternavn = "TRANFLASKE",
            adressebeskyttelse = PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = PersonResponse.Kjønn.UKJENT
        ).ok()
        rapid.sendTestMessage(behov)

        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
    }

    @Test
    fun `besvarer komposittbehov`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentNoeAnnetOgså", "HentPersoninfoV2" ],
            "@id" : "${UUID.randomUUID()}",
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


        every { personinfoService.løsningForPersoninfo(any(), any()) } returns PersonResponse(
            fødselsdato = LocalDate.of(1976, 4, 9),
            dødsdato = null,
            fornavn = "LITEN",
            mellomnavn = null,
            etternavn = "TRANFLASKE",
            adressebeskyttelse = PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = PersonResponse.Kjønn.UKJENT
        ).ok()
        rapid.sendTestMessage(behov)

        assertEquals(løsning, rapid.inspektør.message(0)["@løsning"]["HentPersoninfoV2"])
    }

    @Test
    fun `besvarer behov med egen ident`() {
        val behov = """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentPersoninfoV2" ],
            "@id" : "${UUID.randomUUID()}",
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

        every { personinfoService.løsningForPersoninfo(any(), any()) } returns PersonResponse(
            fødselsdato = LocalDate.of(1976, 4, 9),
            dødsdato = null,
            fornavn = "LITEN",
            mellomnavn = null,
            etternavn = "TRANFLASKE",
            adressebeskyttelse = PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = PersonResponse.Kjønn.UKJENT
        ).ok()
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
            "@id" : "${UUID.randomUUID()}",
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

        every { personinfoService.løsningForPersoninfo(any(), eq("ident1")) } returns PersonResponse(
            fødselsdato = LocalDate.of(1976, 4, 9),
            dødsdato = null,
            fornavn = "LITEN",
            mellomnavn = null,
            etternavn = "TRANFLASKE",
            adressebeskyttelse = PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = PersonResponse.Kjønn.UKJENT
        ).ok()
        every { personinfoService.løsningForPersoninfo(any(), eq("ident2")) } returns PersonResponse(
            fødselsdato = LocalDate.of(1980, 1, 1),
            dødsdato = null,
            fornavn = "STOR",
            mellomnavn = null,
            etternavn = "HYGGE",
            adressebeskyttelse = PersonResponse.Adressebeskyttelse.UGRADERT,
            kjønn = PersonResponse.Kjønn.MANN
        ).ok()
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
            "@id" : "${UUID.randomUUID()}",
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