package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

fun String.loadFromResources() = Thread.currentThread().contextClassLoader.getResource(this)!!.readText()

internal class PdlOversetterIdenterTest {

    @Test
    fun `henter identer`() {
        val response = PdlOversetter.oversetterIdenter(ObjectMapper().readValue("identer/pdl-hentIdenter.json".loadFromResources()))
        val expected = Identer(fødselsnummer = "12345678901", aktørId = "1234567890123")
        assertEquals(expected, response)
    }

    @Test
    fun `Skal kaste exception hvis vi får feil fra PDL`() {
        val thrown = assertThrows<RuntimeException> {
            PdlOversetter.oversetterIdenter(ObjectMapper().readValue("dødsdato/pdl-error-response.json".loadFromResources()))
        }
        assertEquals("error message", thrown.message)
    }

    @Test
    fun `Foreløpig støtter vi ikke mennesker uten fødselsnummer`() {
        assertTrue(PdlOversetter.oversetterIdenter(ObjectMapper().readValue("identer/pdl-uten-fnr.json".loadFromResources())) is FantIkkeIdenter)
    }
}
