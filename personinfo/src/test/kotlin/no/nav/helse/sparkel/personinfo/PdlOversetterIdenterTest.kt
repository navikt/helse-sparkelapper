package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PdlOversetterIdenterTest {

    @Test
    fun `henter identer`() {
        val response = pdlOversetter.interpretIdenter(objectMapper.readValue("identer/pdl-hentIdenter.json".loadFromResources()))
        @Language("JSON")
        val expected = """
            {
              "fødselsnummer": "12345678901",
              "aktørId": "1234567890123"
            }
        """
        assertEquals(expected.toJsonNode(), response)
    }

    @Test
    fun `Skal kaste exception hvis vi får feil fra PDL`() {
        val thrown = assertThrows<RuntimeException> {
            pdlOversetter.interpretIdenter(objectMapper.readValue("dødsdato/pdl-error-response.json".loadFromResources()))
        }
        assertEquals("error message", thrown.message)
    }



    private companion object {
        private val pdlOversetter = PdlOversetter()
        private val objectMapper = ObjectMapper()
        private fun String.toJsonNode() = objectMapper.readTree(this)
    }
}
