package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RepresentasjonRiverTest {
    private val representasjonClient: RepresentasjonClient = mockk(relaxed = true)
    private val rapid: TestRapid = TestRapid().apply {
        RepresentasjonRiver(this, representasjonClient)
    }
    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun clear() {
        rapid.reset()
    }

    @Test
    fun `svarer ut behov for fullmakt`() {
        every { representasjonClient.hentFullmakt(any()) } returns objectMapper.readTree("""{"fullmakt": "yes"}""")
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        val løsning = svar.fullmaktløsning()
        assertEquals(1, løsning?.size())
    }

    @Test
    fun `svarer med oppslagFeilet ved feil mot fullmakt-api`() {
        every { representasjonClient.hentFullmakt(any()) } returns null
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        val løsning = svar.fullmaktløsning()
        assertEquals(1, løsning?.size())
        assertTrue(løsning["oppslagFeilet"].asBoolean())
    }

    @Language("JSON")
    fun behov() = """
        {
            "@event_name" : "fullmakt",
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "fødselsnummer" : "fnr"
        }
        """.trimIndent()

    private fun JsonNode.fullmaktløsning() = this.path("@løsning")["fullmakt"]
}
