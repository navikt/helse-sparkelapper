package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class DokumentRiverTest {

    private val søknadClient: SøknadClient = mockk(relaxed = true)
    private val inntektsmeldingClient: InntektsmeldingClient = mockk(relaxed = true)
    private val rapid = TestRapid().apply {
        DokumentRiver(this, søknadClient, inntektsmeldingClient)
    }
    private val objectMapper = ObjectMapper()

    @Test
    fun `svarer ut behov for dokument type SØKNAD`() {
        every { søknadClient.hentDokument(any()) } returns Result.success(objectMapper.readTree("""{"søknadId": "123"}"""))
        rapid.sendTestMessage(behov("SØKNAD"))
        val svar = rapid.inspektør.message(0)
        val dokument = svar.løsning()["dokument"]
        assertEquals(1, dokument.size())
        assertEquals("123", dokument["søknadId"].asText())
    }

    @Test
    fun `svarer ut behov for dokument type INNTEKTSMELDING`() {
        every { inntektsmeldingClient.hentDokument(any()) } returns Result.success(objectMapper.readTree("""{"inntektsmeldingId": "123"}"""))
        rapid.sendTestMessage(behov("INNTEKTSMELDING"))
        val svar = rapid.inspektør.message(0)
        val dokument = svar.løsning()["dokument"]
        assertEquals(1, dokument.size())
        assertEquals("123", dokument["inntektsmeldingId"].asText())
    }

    @Test
    fun `svarer ikke på behov om kallet feiler selv etter retries`() {
        every { inntektsmeldingClient.hentDokument(any()) } returns Result.failure(RuntimeException("Alle retries brukt opp"))
        rapid.sendTestMessage(behov("INNTEKTSMELDING"))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `svarer med feilrespons ved 404`() {
        every { inntektsmeldingClient.hentDokument(any()) } returns Result.success(JsonNodeFactory.instance.objectNode().put("error", 404))
        rapid.sendTestMessage(behov("INNTEKTSMELDING"))
        val svar = rapid.inspektør.message(0)
        val expected = objectMapper.readTree(""" { "dokument": { "error": 404 } } """)
        assertEquals(expected, svar.løsning())
    }

    @Language("JSON")
    fun behov(dokumenttype: String) = """
        {
            "@event_name" : "hent-dokument",
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "fødselsnummer" : "fnr",
            "dokumentId" : "${UUID.randomUUID()}",
            "dokumentType" : "$dokumenttype"
        }
        """.trimIndent()

    private fun JsonNode.løsning() = this.path("@løsning")
}
