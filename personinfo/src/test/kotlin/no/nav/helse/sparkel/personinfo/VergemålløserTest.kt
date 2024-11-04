package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.VergemålEllerFremtidsfullmaktResponse
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VergemålløserTest {

    private val personinfoService = mockk<PersoninfoService>()
    private val rapid = TestRapid().apply {
        Vergemålløser(this, personinfoService)
    }

    @BeforeEach
    fun setup() {
        clearMocks(personinfoService)
    }

    @Test
    fun `svarer ut behov der det ikke foreligger vergemål eller fremtidsfullmakt`() {
        every { personinfoService.løsningForVergemål(any(), any()) } returns VergemålEllerFremtidsfullmaktResponse(
            vergemålEllerFremtidsfullmakter = emptyList(),
            kilde = IdentResponse.KildeResponse.PDL
        ).ok()
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(0, svar.vergemålLøsning()["vergemål"].size())
    }

    @Test
    fun `svarer ut behov der det foreligger vergemål`() {
        every { personinfoService.løsningForVergemål(any(), any()) } returns VergemålEllerFremtidsfullmaktResponse(
            vergemålEllerFremtidsfullmakter = listOf(
                VergemålEllerFremtidsfullmaktResponse.Vergemål(
                    type = VergemålEllerFremtidsfullmaktResponse.Vergemåltype.VOKSEN
                )
            ),
            kilde = IdentResponse.KildeResponse.PDL
        ).ok()
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        assertEquals(1, svar.vergemålLøsning()["vergemål"].size())
        val vergemål = svar.vergemålLøsning()["vergemål"][0]
        assertEquals("voksen", vergemål["type"].asText())
    }

    @Test
    fun `svarer ut behov der det foreligger fremtidsfullmakt`() {
        every { personinfoService.løsningForVergemål(any(), any()) } returns VergemålEllerFremtidsfullmaktResponse(
            vergemålEllerFremtidsfullmakter = listOf(
                VergemålEllerFremtidsfullmaktResponse.Vergemål(
                    type = VergemålEllerFremtidsfullmaktResponse.Vergemåltype.STADFESTET_FREMTIDSFULLMAKT
                )
            ),
            kilde = IdentResponse.KildeResponse.PDL
        ).ok()
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
