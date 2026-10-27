package no.nav.helse.sparkel.medlemskapmock

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class MedlemskapTest {
    private val rapid = TestRapid().apply(::Medlemskap)
    private val fødselsnummer = "01019012345"

    @BeforeEach
    fun reset() = rapid.reset()

    @Test
    fun `svarer JA uten årsaker som standard`() {
        rapid.sendTestMessage(behov())

        val løsning = løsning()
        assertEquals("JA", løsning.path("svar").asString())
        assertFalse(løsning.has("aarsaker"))
    }

    @Test
    fun `svarer med valgt svar og årsaker`() {
        rapid.sendTestMessage(
            avklaring(
                svar = "UAVKLART_MED_BRUKERSPORSMAAL",
                aarsaker =
                    """[
                      {"regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold"},
                      {"regelId": "REGEL_23", "forklaringPaaUavklart": "Ikke opphold i Norge"}
                    ]""",
            ),
        )
        rapid.sendTestMessage(behov())

        val løsning = løsning()
        assertEquals("UAVKLART_MED_BRUKERSPORSMAAL", løsning.path("svar").asString())
        assertEquals(listOf("REGEL_3", "REGEL_23"), løsning.path("aarsaker").values().map { it.path("regelId").asString() })
        assertEquals("Ikke opphold i Norge", løsning.path("aarsaker")[1].path("forklaringPaaUavklart").asString())
    }

    @Test
    fun `tar ikke med årsaker når svaret er JA`() {
        rapid.sendTestMessage(avklaring("JA", """[{"regelId": "REGEL_3", "forklaringPaaUavklart": "Skal ikke med"}]"""))
        rapid.sendTestMessage(behov())

        assertFalse(løsning().has("aarsaker"))
    }

    @Test
    fun `svarer likt på flere behov for samme person`() {
        rapid.sendTestMessage(avklaring("UAVKLART_MED_BRUKERSPORSMAAL", """[{"regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold"}]"""))
        rapid.sendTestMessage(behov())
        rapid.sendTestMessage(behov())

        assertEquals(2, rapid.inspektør.size)
        repeat(2) { indeks ->
            val løsning =
                rapid.inspektør
                    .message(indeks)
                    .path("@løsning")
                    .path("Medlemskap")
                    .path("resultat")
            assertEquals("UAVKLART_MED_BRUKERSPORSMAAL", løsning.path("svar").asString())
            assertEquals("REGEL_3", løsning.path("aarsaker")[0].path("regelId").asString())
        }
    }

    @Test
    fun `ny avklaring overskriver forrige`() {
        rapid.sendTestMessage(avklaring("NEI", null))
        rapid.sendTestMessage(avklaring("JA", null))
        rapid.sendTestMessage(behov())

        assertEquals("JA", løsning().path("svar").asString())
    }

    @Test
    fun `godtar avklaring uten årsaker`() {
        rapid.sendTestMessage(avklaring("NEI", null))
        rapid.sendTestMessage(behov())

        val løsning = løsning()
        assertEquals("NEI", løsning.path("svar").asString())
        assertTrue(løsning.path("aarsaker").isArray)
        assertTrue(løsning.path("aarsaker").isEmpty)
    }

    private fun løsning() =
        rapid.inspektør
            .message(rapid.inspektør.size - 1)
            .path("@løsning")
            .path("Medlemskap")
            .path("resultat")

    @Language("JSON")
    private fun avklaring(
        svar: String,
        aarsaker: String?,
    ) = """{
  "@event_name": "mock_medlemskap_avklaring",
  "ident": "$fødselsnummer",
  "medlemskapVerdi": "$svar"${aarsaker?.let { """, "aarsaker": $it""" } ?: ""}
}"""

    @Language("JSON")
    private fun behov() =
        """{
  "@event_name": "behov",
  "@id": "${UUID.randomUUID()}",
  "@behov": ["Medlemskap"],
  "fødselsnummer": "$fødselsnummer",
  "vedtaksperiodeId": "${UUID.randomUUID()}",
  "Medlemskap": {
    "medlemskapPeriodeFom": "2025-01-01",
    "medlemskapPeriodeTom": "2025-01-31"
  }
}"""
}
