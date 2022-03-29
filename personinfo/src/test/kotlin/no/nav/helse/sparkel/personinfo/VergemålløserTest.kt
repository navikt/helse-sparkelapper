package no.nav.helse.sparkel.personinfo

import PdlStubber
import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

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
    fun `svarer ut behov der det ikke foreligger vergemål eller fullmakt`() {
        stubPdlRespons(utenVergemålOgFullmakt())
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
    fun `svarer ut behov der det foreligger fullmakt`() {
        stubPdlRespons(medFullmakt())
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(1, svar.vergemålLøsning()["fullmakter"].size())
        val fullmakter = svar.vergemålLøsning()["fullmakter"][0]
        assertEquals(Vergemålløser.Område.Syk.name, fullmakter["områder"].first().asText())
        assertEquals(LocalDate.of(2021,12,1), fullmakter["gyldigFraOgMed"].asLocalDate())
        assertEquals(LocalDate.of(2022, 12,30), fullmakter["gyldigTilOgMed"].asLocalDate())
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
