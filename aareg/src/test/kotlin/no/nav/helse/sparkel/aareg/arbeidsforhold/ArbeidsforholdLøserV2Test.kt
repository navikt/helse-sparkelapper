package no.nav.helse.sparkel.aareg.arbeidsforhold

import ch.qos.logback.classic.Level.ERROR
import ch.qos.logback.classic.Level.WARN
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ArbeidsforholdLøserV2Test : AbstractAaregTest() {

    @Test
    internal fun `løser ArbeidsforholdV2`() {
        settOppApp()

        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["ArbeidsforholdV2"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        rapid.sendTestMessage(behov)

        assertNotNull(sendtMelding.løsning("AlleArbeidsforhold"))
    }

    @Test
    internal fun `løser også AlleArbeidsforhold`() {
        settOppApp()

        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["AlleArbeidsforhold"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        rapid.sendTestMessage(behov)

        val løsning = sendtMelding.løsning("AlleArbeidsforhold")
        assertTrue(løsning.isNotEmpty())
    }

    @Test
    internal fun `Ignorerer behov og logger error ved ukjent feil`() {
        settOppApp(AaregSvar("{}", InternalServerError))
        val logglytter = opprettLogglytter()

        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["AlleArbeidsforhold"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        rapid.sendTestMessage(behov)

        assertEquals(1, logglytter.list.filter { it.level == ERROR }.size)
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    internal fun `Ignorerer behov for personer som ikke finnes, uten å logge error - for dev`() {
        settOppApp(AaregSvar("""{"meldinger":["Ukjent ident"]}""", NotFound))
        val logglytter = opprettLogglytter()

        val behov =
            """{"@id": "${UUID.randomUUID()}", "@behov":["AlleArbeidsforhold"], "fødselsnummer": "fnr", "vedtaksperiodeId": "id" }"""
        rapid.sendTestMessage(behov)

        assertEquals(1, logglytter.list.filter { it.message.contains("personen finnes ikke") && it.level == WARN }.size)
        assertEquals(0, logglytter.list.filter { it.level == ERROR }.size)
        assertEquals(0, rapid.inspektør.size)
    }

    private fun JsonNode.løsning(behov: String): List<Arbeidsforhold> =
        this.path("@løsning")
            .path(behov)
            .map {
                Arbeidsforhold(
                    it["orgnummer"].asText(),
                    it["ansattSiden"].asLocalDate(),
                    it["ansattTil"].asOptionalLocalDate(),
                    Arbeidsforholdtype.valueOf(it["type"].asText())
                )
            }
}
