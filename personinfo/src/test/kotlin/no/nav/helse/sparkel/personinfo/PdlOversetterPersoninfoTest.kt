package no.nav.helse.sparkel.personinfo

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class PdlOversetterPersoninfoTest {
    private val logCollector = ListAppender<ILoggingEvent>()

    init {
        (LoggerFactory.getLogger("pdl-oversetter") as Logger).addAppender(logCollector)
        logCollector.start()
    }

    @BeforeEach
    fun setUp() {
        logCollector.list.clear()
    }

    @Test
    fun `hente ugradert person`() {
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-ugradert.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "ident": "fnr",
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
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-uten-gradering.json".loadFromResources()))
        @Language("JSON")
        val expected = """
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
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `hente fortrolig person`() {
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-fortrolig.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "ident": "fnr",
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
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-strengt-fortrolig.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "ident": "fnr",
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
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-strengt-fortrolig-utland.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "ident": "fnr",
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
        val response = PdlOversetter.oversettPersoninfo("fnr", objectMapper.readValue("personinfo/pdl-hentPerson-ukjent-gradering.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "ident": "fnr",
              "fornavn": "LITEN",
              "mellomnavn": "STOR",
              "etternavn": "TRANFLASKE",
              "fødselsdato": "1980-04-09",
              "kjønn": "Ukjent",
              "adressebeskyttelse": "Ukjent"
            }
        """

        assertEquals(listOf("Mottok ukjent adressebeskyttelse: TemmeligHemmelig fra PDL"), logCollector.messages())
        assertEquals(expected.toJsonNode(), response)
    }

    private fun ListAppender<ILoggingEvent>.messages() = list.map(ILoggingEvent::getMessage)

    private companion object {
        private val objectMapper = ObjectMapper()
        private fun String.toJsonNode() = objectMapper.readTree(this)
    }
}
