package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.every
import io.mockk.mockk
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class DokumentRiverTest {

    private lateinit var rapid: TestRapid
    private var søknadClient: SøknadClient = mockk(relaxed = true)
    private var inntektsmeldingClient: InntektsmeldingClient = mockk(relaxed = true)
    private val objectMapper = ObjectMapper()

    @BeforeAll
    fun setup() {
        rapid = TestRapid().apply {
            DokumentRiver(this, søknadClient, inntektsmeldingClient)
        }
    }

    @BeforeEach
    fun clear() {
        rapid.reset()
    }

    @Test
    fun `svarer ut behov for dokument type SØKNAD`() {
        every { søknadClient.hentDokument(any()) } returns objectMapper.readTree("""{"søknadId": "123"}""")
        rapid.sendTestMessage(behov("SØKNAD"))
        val svar = rapid.inspektør.message(0)
        val dokument = svar.dokumentLøsning()["dokument"]
        assertEquals(1, dokument?.size())
    }

    @Test
    fun `svarer ut behov for dokument type INNTEKTSMELDING`() {
        every { inntektsmeldingClient.hentDokument(any()) } returns objectMapper.readTree("""{"innteksmeldingId": "123"}""")
        rapid.sendTestMessage(behov("INNTEKTSMELDING"))
        val svar = rapid.inspektør.message(0)
        val dokument = svar.dokumentLøsning()["dokument"]
        assertEquals(1, dokument?.size())
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

    private fun JsonNode.dokumentLøsning() = this.path("@løsning")
}
