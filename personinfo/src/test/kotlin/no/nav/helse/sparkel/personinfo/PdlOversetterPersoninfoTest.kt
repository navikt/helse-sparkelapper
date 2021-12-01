package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PdlOversetterPersoninfoTest {

    @Test
    fun `hente ugradert person`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-ugradert.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Mann",
              "adressebeskyttelse": "Ugradert"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente person uten gradering`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-uten-gradering.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": null,
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1976-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ugradert"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente fortrolig person`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-fortrolig.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": "STOR",
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1980-04-09",
              "kjønn": "Kvinne",
              "adressebeskyttelse": "Fortrolig"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente strengt fortrolig person`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-strengt-fortrolig.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": "STOR",
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1980-04-09",
              "kjønn": "Kvinne",
              "adressebeskyttelse": "StrengtFortrolig"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente strengt fortrolig utland person`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-strengt-fortrolig-utland.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": "STOR",
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1980-04-09",
              "kjønn": "Kvinne",
              "adressebeskyttelse": "StrengtFortroligUtland"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente person med ukjent gradering`() {
        val response = PdlOversetter.oversettPersoninfo(objectMapper.readValue("personinfo/pdl-hentPerson-ukjent-gradering.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fornavn": "LITEN",
              "mellomnavn": "STOR",
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1980-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ukjent"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    private companion object {
        private val objectMapper = ObjectMapper()
        private fun String.toJsonNode() = objectMapper.readTree(this)
    }
}
