package no.nav.helse.sparkel.medlemskap

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

class MedlemskapClientTest {
    @Test
    fun `løsning uten aarsaker`() {
        assertLøsning(
            """{"resultat": {"svar": "JA"}}""",
            MedlemskapSvar("JA", null),
        )
    }

    @Test
    fun `løsning med aarsaker`() {
        assertLøsning(
            """
            {
              "resultat": {
                "svar": "UAVKLART",
                "aarsaker": [
                  {"regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold"},
                  {"regelId": "REGEL_23", "forklaringPaaUavklart": "Ikke opphold i Norge"}
                ]
              }
            }
            """,
            MedlemskapSvar(
                "UAVKLART",
                listOf(
                    MedlemskapSvar.Årsak("REGEL_3", "Ikke registrert arbeidsforhold"),
                    MedlemskapSvar.Årsak("REGEL_23", "Ikke opphold i Norge"),
                ),
            ),
        )
    }

    @Test
    fun `løsning med tom liste aarsaker`() {
        assertLøsning(
            """{"resultat": {"svar": "JA", "aarsaker": []}}""",
            MedlemskapSvar("JA", emptyList()),
        )
    }

    private fun assertLøsning(
        @Language("JSON") forventet: String,
        medlemskapSvar: MedlemskapSvar,
    ) {
        assertEquals(objectMapper.readTree(forventet), MedlemskapClient.tilLøsning(medlemskapSvar))
    }

    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }
}
