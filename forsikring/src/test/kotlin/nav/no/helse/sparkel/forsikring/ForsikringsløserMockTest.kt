package nav.no.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.sparkel.forsikring.Forsikringsløser
import no.nav.helse.sparkel.forsikring.MockForsikringDao
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class `ForsikringsløserMockTest` {
    private val rapid = TestRapid().apply {
        Forsikringsløser(
            rapidsConnection = this,
            forsikringDao = MockForsikringDao()
        )
    }

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
    }

    @ParameterizedTest
    @CsvSource(
        "29500053761, HundreProsentFraDagSytten",
        "16500094528, HundreProsentFraDagEn",
        "05420167468, ÅttiProsentFraDagEn",
        "24500092005, HundreProsentFraDagEn"
    )
    fun `Tester at mockad data i dev gir svar`(fnr: String, forsikringsType: String) {
        rapid.sendTestMessage(
            """
            {
                "@behov": ["SelvstendigForsikring"],
                "@id": "12345",
                "@opprettet": "2024-06-01T12:00:00",
                "fødselsnummer": "$fnr",
                "SelvstendigForsikring": {
                    "skjæringstidspunkt": "2024-05-01"
                }
            }
            """.trimIndent()
        )

        assertEquals(1, rapid.inspektør.size)
        val melding = rapid.inspektør.message(0)

        assertEquals(1, melding["@løsning"]?.get("SelvstendigForsikring")?.toList()?.size)
        assertEquals(forsikringsType, melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("forsikringstype")?.asText())
        assertEquals("2024-04-21", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("startdato")?.asText())
        assertEquals("null", melding["@løsning"]?.get("SelvstendigForsikring")?.firstOrNull()?.get("sluttdato")?.asText())
    }
}
