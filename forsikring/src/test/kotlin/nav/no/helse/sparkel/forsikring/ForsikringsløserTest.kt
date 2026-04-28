package nav.no.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.sparkel.forsikring.Forsikringsløser
import no.nav.helse.sparkel.forsikring.ReplikabaseForsikringDao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class ForsikringsløserTest {
    private val rapid = TestRapid().apply {
        Forsikringsløser(
            rapidsConnection = this,
            forsikringDao = ReplikabaseForsikringDao(TestcontainersDatabase.dataSource)
        )
    }

    @BeforeEach
    fun beforeEach() {
        TestcontainersDatabase.clear()
    }

    @Test
    fun `Får svar når forsikringen er godkjent, i riktig periode og av gyldig type`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20240101,
            forstom = 20241231,
            godkj = "J",
            type = "1",
            premgrl = 816000
        )

        rapid.sendTestMessage(
            """
            {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )

        assertEquals(1, rapid.inspektør.size)
        val melding = rapid.inspektør.message(0)

        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("ÅttiProsentFraDagEn", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2024-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("2024-12-31", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }

    @Test
    fun `Tomt svar når forsikringen ikke dekker skjæringstidspunkt`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20230101,
            forstom = 20231231,
            godkj = "J",
            type = "2",
            premgrl = 816000
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), rapid.inspektør.message(0)["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Tomt svar når forsikringen ikke er godkjent`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 0,
            forstom = 20241231,
            godkj = "N",
            type = " ",
            premgrl = 0
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), rapid.inspektør.message(0)["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Tomt svar når forsikringen ikke er interessant`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20240101,
            forstom = 20241231,
            godkj = "J",
            type = "5",
            premgrl = 816000
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        assertEquals(emptyList<Any>(), rapid.inspektør.message(0)["@løsning"]?.get("SelvstendigForsikring")?.toList())
    }

    @Test
    fun `Bare ett svar når bare en forsikring er gyldig`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20240101,
            forstom = 20241231,
            godkj = "J",
            type = "2",
            premgrl = 816000
        )

        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20240101,
            forstom = 20241231,
            godkj = "J",
            type = "5",
            premgrl = 816000
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )
        val melding = rapid.inspektør.message(0)

        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("HundreProsentFraDagSytten", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2024-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("2024-12-31", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }

    @Test
    fun `virkdato er 0, siden den er løpende`() {
        TestcontainersDatabase.insertVedfrivt(
            agnrFnr = "03020112345",
            virkdato = 20250101,
            forstom = 0,
            godkj = "J",
            type = "2",
            premgrl = 816000
        )

        rapid.sendTestMessage(
            """
             {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "01020312345",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2025-05-01"
                }
            }
            """.trimIndent()
        )
        val melding = rapid.inspektør.message(0)
        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals("HundreProsentFraDagSytten", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2025-01-01", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("null", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }
}
