package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
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
    private val objectMapper = ObjectMapper()

    @BeforeAll
    fun setup() {
        rapid = TestRapid().apply {
            DokumentRiver(this, søknadClient)
        }
    }

    @BeforeEach
    fun clear() {
        rapid.reset()
    }

    @Test
    fun `svarer ut behov for dokument`() {
        every { søknadClient.hentDokument(any()) } returns objectMapper.readTree("""{"søknadId": "123"}""")
        rapid.sendTestMessage(behov())
        val svar = rapid.inspektør.message(0)
        val dokument = svar.dokumentLøsning()["dokument"]
        assertEquals(1, dokument?.size())
    }


    @Language("JSON")
    fun behov() = """
        {
            "@event_name" : "behov",
            "@behov" : [ "hent-dokument" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "fødselsnummer" : "fnr",
            "dokumentid" : "${UUID.randomUUID()}",
            "dokumenttype" : "SØKNAD"
        }
        """.trimIndent()

    private fun JsonNode.dokumentLøsning() = this.path("@løsning")
}
