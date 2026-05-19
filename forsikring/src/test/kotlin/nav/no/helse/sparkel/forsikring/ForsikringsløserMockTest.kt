package nav.no.helse.sparkel.forsikring

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import no.nav.helse.sparkel.forsikring.Forsikringsløser
import no.nav.helse.sparkel.forsikring.MockForsikringDao
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

private val objectMapper: ObjectMapper =
    jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
private val objectMapperWriter = objectMapper.writerWithDefaultPrettyPrinter()

internal class ForsikringsløserMockTest {
    private val rapid = TestRapid().apply {
        Forsikringsløser(
            rapidsConnection = this,
            forsikringDao = MockForsikringDao()
        )
    }

    @ParameterizedTest
    @CsvSource(
        "29500053761, HundreProsentFraDagSytten",
        "16500094528, HundreProsentFraDagEn",
        "05420167468, ÅttiProsentFraDagEn",
        "24500092005, HundreProsentFraDagEn"
    )
    fun `Tester at mockad data i dev gir svar`(fnr: String, forsikringsType: String) {
        // Given
        val behovInnhold = """
            "@behov": [ "SelvstendigForsikring" ],
            "fødselsnummer": "$fnr",
            "SelvstendigForsikring": {
                "skjæringstidspunkt": "2024-05-01"
            }
        """.trimIndent()

        // When
        rapid.sendTestMessage("""{ $behovInnhold }""")

        // Then
        assertEquals(1, rapid.inspektør.size)
        assertJsonEquals(
            """
                {
                    $behovInnhold,
                    "@løsning": {
                        "SelvstendigForsikring": [
                            {
                                "forsikringstype": "$forsikringsType",
                                "premiegrunnlag": 450000,
                                "startdato": "2024-04-21",
                                "sluttdato": null
                            }
                        ]
                    }
                }
            """.trimIndent(),
            rapid.inspektør.message(0),
        )
    }
}

fun assertJsonEquals(
    @Language("JSON") expectedJson: String,
    actualJsonNode: JsonNode,
) {
    val actualAsObjectNode =
        actualJsonNode.deepCopy<ObjectNode>().apply {
            listOf(
                "@id",
                "@opprettet",
                "system_read_count",
                "system_participating_services",
                "@forårsaket_av"
            ).forEach { remove(it) }
        }
    assertEquals(
        objectMapperWriter.writeValueAsString(objectMapper.readTree(expectedJson)),
        objectMapperWriter.writeValueAsString(actualAsObjectNode),
    )
}
