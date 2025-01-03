package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
        coEvery { representasjonClient.hentFullmakt(any()) } returns Result.success(objectMapper.readTree(fullmaktJson))
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        val løsning = svar.fullmaktløsning()
        assertEquals(1, løsning?.size())
    }

    @Test
    fun `kaster exception ved feil mot repr-api`() {
        // Later som at kallet ikke går ok til tross for retries (exception-typen er ikke helt prod-realistisk)
        coEvery { representasjonClient.hentFullmakt(any()) } returns Result.failure(IllegalStateException())
        assertThrows<IllegalStateException> { rapid.sendTestMessage(behov()) }
        assertEquals(0, rapid.inspektør.size)
        coVerify(exactly = 1) { representasjonClient.hentFullmakt("fnr") }
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

    private fun JsonNode.fullmaktløsning() = this.path("@løsning")["Fullmakt"]
}
