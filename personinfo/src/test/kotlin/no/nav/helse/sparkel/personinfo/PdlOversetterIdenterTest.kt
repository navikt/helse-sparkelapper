package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PdlOversetterIdenterTest {

    @Test
    fun `henter identer`() {
        val response = PdlOversetter.interpretIdenter(ObjectMapper().readValue("identer/pdl-hentIdenter.json".loadFromResources()))
        val expected = PdlOversetter.Identer(fødselsnummer = "12345678901", aktørId = "1234567890123")
        assertEquals(expected, response)
    }

    @Test
    fun `Skal kaste exception hvis vi får feil fra PDL`() {
        val thrown = assertThrows<RuntimeException> {
            PdlOversetter.interpretIdenter(ObjectMapper().readValue("dødsdato/pdl-error-response.json".loadFromResources()))
        }
        assertEquals("error message", thrown.message)
    }
}
