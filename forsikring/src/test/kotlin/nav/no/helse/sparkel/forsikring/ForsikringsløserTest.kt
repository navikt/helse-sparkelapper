package nav.no.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.sparkel.forsikring.Forsikringsløser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class ForsikringsløserTest {

    private val rapid = TestRapid()

    private val sisteSendtMelding get() = rapid.inspektør.message(rapid.inspektør.size.minus(1))

    @BeforeAll
    fun setup() {

        rapid.apply {
            Forsikringsløser(this)
        }
    }

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
    }

    @Test
    fun `enkel forsikringsløser test`() {
        rapid.sendTestMessage(
            """
            {
                "@behov": ["Forsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345"
            }
            """.trimIndent()
        )

        // Får ikke noe løsning tilbake siden løseren ikke er implementert
        assertEquals(0, rapid.inspektør.size)
    }
}
