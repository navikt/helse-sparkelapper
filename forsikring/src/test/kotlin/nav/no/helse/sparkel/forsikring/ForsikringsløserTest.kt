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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20240101,
            IF10_FORSTOM = 20241231,
            IF10_GODKJ = 'J',
            IF10_TYPE = '1',
            IF10_PREMGRL = 816000
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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20230101,
            IF10_FORSTOM = 20231231,
            IF10_GODKJ = 'J',
            IF10_TYPE = '2',
            IF10_PREMGRL = 816000
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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 0,
            IF10_FORSTOM = 20241231,
            IF10_GODKJ = 'N',
            IF10_TYPE = ' ',
            IF10_PREMGRL = 0
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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20240101,
            IF10_FORSTOM = 20241231,
            IF10_GODKJ = 'J',
            IF10_TYPE = '5',
            IF10_PREMGRL = 816000
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
    fun `Tomt svar når det ikke finnes noen forsikring`() {
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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20240101,
            IF10_FORSTOM = 20241231,
            IF10_GODKJ = 'J',
            IF10_TYPE = '2',
            IF10_PREMGRL = 816000
        )

        TestcontainersDatabase.insertVedfrivt(
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20240101,
            IF10_FORSTOM = 20241231,
            IF10_GODKJ = 'J',
            IF10_TYPE = '5',
            IF10_PREMGRL = 816000
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
            IF01_AGNR_FNR = 3020112345L,
            IF10_VIRKDATO = 20250101,
            IF10_FORSTOM = 0,
            IF10_GODKJ = 'J',
            IF10_TYPE = '2',
            IF10_PREMGRL = 816000
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
