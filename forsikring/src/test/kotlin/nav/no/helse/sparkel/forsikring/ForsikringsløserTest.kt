package nav.no.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.time.LocalDate
import no.nav.helse.sparkel.forsikring.Fnr
import no.nav.helse.sparkel.forsikring.ForsikringDao
import no.nav.helse.sparkel.forsikring.Forsikringsløser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class ForsikringsløserTest : H2Database() {

    private val rapid = TestRapid()

    private val sisteSendtMelding get() = rapid.inspektør.message(rapid.inspektør.size.minus(1))

    @BeforeAll
    fun setup() {

        val forsikringDao = ForsikringDao { dataSource }
        rapid.apply {
            Forsikringsløser(this, forsikringDao)
        }

    }

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        clear()
    }

    @Test
    fun `Får svar når forsikringen er godkjent, i riktig periode og av gyldig type`() {
        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 12, 31),
            godkjent = "J",
            forsikringstype = "1"
        )

        rapid.sendTestMessage(
            """
            {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )

        assertEquals(1, rapid.inspektør.size)
        val melding = sisteSendtMelding

        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("ÅttiProsentFraDagEn", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2024-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("2024-12-31", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }

    @Test
    fun `Tomt svar når forsikringen ikke dekker skjæringstidspunkt` () {

        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2023, 1, 1),
            tom = LocalDate.of(2023, 12, 31),
            godkjent = "J",
            forsikringstype = "2"
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), sisteSendtMelding["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Tomt svar når forsikringen ikke er godkjent` () {

        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = null,
            tom = LocalDate.of(2024, 12, 31),
            godkjent = "N",
            forsikringstype = " "
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), sisteSendtMelding["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Tomt svar når forsikringen ikke er interessant` () {

        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 12, 31),
            godkjent = "J",
            forsikringstype = "5"
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), sisteSendtMelding["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Bare et svar når bare en forsikring er gyldig` () {

        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 12, 31),
            godkjent = "J",
            forsikringstype = "2"
        )

        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2024, 1, 1),
            tom = LocalDate.of(2024, 12, 31),
            godkjent = "J",
            forsikringstype = "5"
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        val melding = sisteSendtMelding

        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("HundreProsentFraDagSytten", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2024-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("2024-12-31", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }

    @Test
    fun `virkdato er 0, siden den er løpende` () {
        opprettPeriode(
            fnr = Fnr("01010112345"),
            virkningsdato = LocalDate.of(2025, 1, 1),
            tom = null,
            godkjent = "J",
            forsikringstype = "2"
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01010112345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2025-05-01"
                }
            }
            """.trimIndent()
        )
        val melding = sisteSendtMelding
        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("HundreProsentFraDagSytten", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2025-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("null", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }
}
