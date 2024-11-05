package no.nav.helse.sparkel.egenansatt

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class EgenAnsattLøserTest {

    private val skjermedePersoner = mockk<SkjermedePersoner>()

    private val rapid = TestRapid()

    private val sendtMelding get() = rapid.inspektør.let {
        it.message(it.size - 1)
    }

    @BeforeEach
    fun reset() {
        rapid.reset()
        mockEgenAnsatt()
    }

    private fun mockEgenAnsatt(egenAnsatt: Boolean = false) {
        skjermedePersoner.apply {
            every { erSkjermetPerson(any(), any()) } answers {
                egenAnsatt
            }
        }
    }

    @Test
    internal fun `løser behov ikke egen ansatt`() {
        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["${EgenAnsattLøser.behov}"], "fødselsnummer": "fnr", "aktørId": "aktørId" }"""

        testBehov(behov)

        assertFalse(sendtMelding.løsning())
    }


    @Test
    internal fun `løser behov egen ansatt`() {
        mockEgenAnsatt(true)

        val behov = """{"@id": "${UUID.randomUUID()}", "@behov":["${EgenAnsattLøser.behov}"], "fødselsnummer": "fnr", "aktørId": "aktørId" }"""

        testBehov(behov)

        assertTrue(sendtMelding.løsning())
    }

    private fun JsonNode.løsning() = this.path("@løsning").path(EgenAnsattLøser.behov).booleanValue()

    private fun testBehov(behov: String) {
        EgenAnsattLøser(rapid, skjermedePersoner)
        rapid.sendTestMessage(behov)
    }
}
