package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tjeneste.pip.egen.ansatt.v1.EgenAnsattV1
import no.nav.tjeneste.pip.egen.ansatt.v1.WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class EgenAnsattLøserTest {

    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private val egenansattV1 = mockk<EgenAnsattV1>()

    private val nom = mockk<NOM>()

    private val meldinger = mutableListOf<JsonNode>()

    private val sendtMelding get() = meldinger.last()

    private val rapid = object : RapidsConnection() {
        fun sendTestMessage(message: String) = notifyMessage(message, this)

        override fun publish(message: String) {
            meldinger.add(objectMapper.readTree(message))
        }

        override fun publish(key: String, message: String) {}

        override fun start() {}

        override fun stop() {}
    }

    @BeforeEach
    fun reset() {
        meldinger.clear()
        mockEgenAnsatt()
    }

    private fun mockEgenAnsatt(egenAnsatt: Boolean = false) {
        egenansattV1.apply {
            every { hentErEgenAnsattEllerIFamilieMedEgenAnsatt(any()) } answers {
                WSHentErEgenAnsattEllerIFamilieMedEgenAnsattResponse()
                    .withEgenAnsatt(egenAnsatt)
            }
        }
    }

    @Test
    internal fun `løser behov ikke egen ansatt`() {
        val behov = """{"@id": "behovsid", "@behov":["${EgenAnsattLøser.behov}"], "fødselsnummer": "fnr" }"""

        testBehov(behov)

        assertFalse(sendtMelding.løsning())
    }


    @Test
    internal fun `løser behov egen ansatt`() {
        mockEgenAnsatt(true)

        val behov = """{"@id": "behovsid", "@behov":["${EgenAnsattLøser.behov}"], "fødselsnummer": "fnr" }"""

        testBehov(behov)

        assertTrue(sendtMelding.løsning())
    }

    private fun JsonNode.løsning() = this.path("@løsning").path(EgenAnsattLøser.behov).booleanValue()

    private fun testBehov(behov: String) {
        EgenAnsattLøser(rapid, egenansattV1, nom)
        rapid.sendTestMessage(behov)
    }
}
