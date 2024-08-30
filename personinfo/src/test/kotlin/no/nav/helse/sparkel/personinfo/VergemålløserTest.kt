package no.nav.helse.sparkel.personinfo

import PdlStubber
import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VergemålløserTest : PdlStubber() {

    private lateinit var rapid: TestRapid

    @BeforeAll
    fun setup() {
        rapid = TestRapid().apply {
            Vergemålløser(this, personinfoService)
        }
    }

    @BeforeEach
    fun clear() {
        rapid.reset()
    }

    @Test
    fun `svarer ut behov der det ikke foreligger vergemål eller fremtidsfullmakt`() {
        stubPdlRespons(utenVergemålEllerFremtidsfullmakt())
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(0, svar.vergemålLøsning()["vergemål"].size())
    }

    @Test
    fun `svarer ut behov der det foreligger vergemål`() {
        stubPdlRespons(medVergemål())
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(1, svar.vergemålLøsning()["vergemål"].size())
        val vergemål = svar.vergemålLøsning()["vergemål"][0]
        assertEquals("voksen", vergemål["type"].asText())
    }

    @Test
    fun `svarer ut behov der det foreligger fremtidsfullmakt`() {
        stubPdlRespons(medFremtidsfullmakt())
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(1, svar.vergemålLøsning()["fremtidsfullmakter"].size())
        val fremtidsfullmakt = svar.vergemålLøsning()["fremtidsfullmakter"][0]
        assertEquals(Vergemålløser.VergemålType.stadfestetFremtidsfullmakt.name, fremtidsfullmakt["type"].asText())
    }

    @Language("JSON")
    fun behov() = """
        {
            "@event_name" : "behov",
            "@behov" : [ "Vergemål" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "fødselsnummer" : "fnr"
        }
        """.trimIndent()

    private fun JsonNode.vergemålLøsning() = this.path("@løsning").path(Vergemålløser.behov)
}
