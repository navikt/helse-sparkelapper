package no.nav.helse.sparkel.aareg.arbeidsforhold

import ch.qos.logback.classic.Level.ERROR
import ch.qos.logback.classic.Level.WARN
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.NotFound
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.azureTokenStub
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class ArbeidsforholdLøserV2Test {

    private val sendtMelding get() = rapid.inspektør.message(rapid.inspektør.size - 1)

    private val rapid = TestRapid()

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

    data class AaregSvar(val response: String, val status: HttpStatusCode)

    private fun settOppApp(aaregSvar: AaregSvar? = null) {
        val mockAaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            scope = "aareg-scope",
            tokenSupplier = azureTokenStub(),
            httpClient = if (aaregSvar != null) aaregMockClient(
                aaregSvar.response, aaregSvar.status
            ) else aaregMockClient()
        )
        ArbeidsforholdLøserV2(rapid, mockAaregClient)
    }

    private fun opprettLogglytter() = ListAppender<ILoggingEvent>().apply {
        (LoggerFactory.getLogger(ArbeidsforholdLøserV2::class.java) as Logger).addAppender(this)
        start()
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
