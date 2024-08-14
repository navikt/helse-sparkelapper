package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RepresentasjonRiverTest {
    private val representasjonClient: RepresentasjonClient = mockk(relaxed = true)
    private val rapid: TestRapid = TestRapid().apply {
        RepresentasjonRiver(this, representasjonClient)
    }
    private val objectMapper = ObjectMapper()

    @Test
    fun `svarer ut behov for fullmakt`() {
        @Language("JSON")
        val fullmaktJson = """
               [
                    {
                        "omraade": [{"tema": "SYK"}], 
                        "gyldigFraOgMed": "2020-01-01", 
                        "gyldigTilOgMed": "2020-12-31"
                    }
                ]
            """.trimIndent()
        every { representasjonClient.hentFullmakt(any()) } returns Result.success(objectMapper.readTree(fullmaktJson))
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        val løsning = svar.fullmaktløsning()
        assertEquals(1, løsning?.size())
    }

    @Test
    fun `svarer med oppslagFeilet ved feil mot fullmakt-api`() {
        every { representasjonClient.hentFullmakt(any()) } returns Result.failure(RuntimeException())
        rapid.sendTestMessage(behov())
        assertEquals(0, rapid.inspektør.size)
        verify (exactly = 1) { representasjonClient.hentFullmakt("fnr") }
    }

    @Language("JSON")
    fun behov() = """
        {
            "@event_name" : "behov",
            "@behov" : ["Fullmakt"],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "fødselsnummer" : "fnr"
        }
        """.trimIndent()

    private fun JsonNode.fullmaktløsning() = this.path("@løsning")["fullmakt"]
}
