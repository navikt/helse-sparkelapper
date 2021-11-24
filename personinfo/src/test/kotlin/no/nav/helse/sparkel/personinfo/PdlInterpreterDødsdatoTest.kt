package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class PdlOversetterTest {

    val pdlOversetter = PdlOversetter()
    val objectMapper = ObjectMapper()

    @Test
    fun `Skal kaste exception hvis vi får feil fra PDL`() {
        val thrown = assertThrows(RuntimeException::class.java) {
            pdlOversetter.interpretDødsdato(objectMapper.readValue("dødsdato/pdl-error-response.json".loadFromResources()))
        }
        assertEquals("error message", thrown.message)
    }

    @Test
    fun `Happy case levende person`() {
        val response = pdlOversetter.interpretDødsdato(objectMapper.readValue("dødsdato/pdl-hentPerson-levende.json".loadFromResources()))
        assertEquals(responseNode(null as String?), response)
    }

    @Test
    fun `Teknisk sett happy case avdødd person`() {
        val response = pdlOversetter.interpretDødsdato(objectMapper.readValue("dødsdato/pdl-hentPerson-dødsdato-satt.json".loadFromResources()))
        assertEquals(responseNode("1962-07-08"), response)
    }

    @Test
    fun `To masterdataoppføringer, som er enige`() {
        val response = pdlOversetter.interpretDødsdato(objectMapper.readValue("dødsdato/pdl-hentPerson-to-like-response.json".loadFromResources()))
        assertEquals(responseNode("1962-07-08"), response)
    }

    @Test
    fun `Velger PDL hvis to masterdataoppføringer er uenige`() {
        val response = pdlOversetter.interpretDødsdato(objectMapper.readValue("dødsdato/pdl-hentPerson-to-ulike-response.json".loadFromResources()))
        assertEquals(responseNode("1962-07-09"), response)
    }

    private fun responseNode(result: String?) = ObjectMapper().createObjectNode().put("dødsdato", result)
}

fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()
