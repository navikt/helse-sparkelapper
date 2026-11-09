package no.nav.helse.sparkel.medlemskap

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.IOException
import java.time.LocalDate

class TolkTest {
    @Test
    fun `speil response`() {
        assertEquals("JA", nyTolk(response = speilvurderingResponse("JA")).tolk().svar)
        assertEquals("NEI", nyTolk(response = speilvurderingResponse("NEI")).tolk().svar)
        assertEquals("UAVKLART", nyTolk(response = speilvurderingResponse("UAVKLART")).tolk().svar)
        assertEquals("UAVKLART_MED_BRUKERSPORSMAAL", nyTolk(response = speilvurderingResponse("UAVKLART_MED_BRUKERSPORSMAAL")).tolk().svar)
    }

    @Test
    fun `speil response uten aarsaker`() {
        assertNull(nyTolk(response = speilvurderingResponse("JA")).tolk().aarsaker)
    }

    @Test
    fun `speil response med aarsaker`() {
        val response =
            speilvurderingResponse(
                "UAVKLART",
                """[{"regelId": "REGEL_3", "forklaringPaaUavklart": "Ikke registrert arbeidsforhold"}]""",
            )
        assertEquals(
            MedlemskapSvar("UAVKLART", listOf(MedlemskapSvar.Årsak("REGEL_3", "Ikke registrert arbeidsforhold"))),
            nyTolk(response = response).tolk(),
        )
    }

    @Test
    fun `speil response med tom liste aarsaker`() {
        assertEquals(emptyList<MedlemskapSvar.Årsak>(), nyTolk(response = speilvurderingResponse("JA", "[]")).tolk().aarsaker)
    }

    @Test
    fun `speil response med ugyldige aarsaker er uventet`() {
        assertThrows<IllegalStateException> {
            nyTolk(response = speilvurderingResponse("UAVKLART", """[{"regelId": "REGEL_3"}]""")).tolk()
        }
    }

    @Test
    fun `graderte personer`() {
        assertEquals("UAVKLART", nyTolk(httpStatus = 503, response = "HVA_SOM_HELST_som_inneholder_GradertAdresseBørGiDette").tolk().svar)
    }

    @Test
    fun `uventet response`() {
        assertEquals(
            "Fikk uventet svar fra medlemskapstjenesten 3 ganger. Se sikkerlogg for detaljer",
            assertThrows<IllegalStateException> {
                nyTolk(response = crazyRespone).tolk()
            }.message,
        )
    }

    @Test
    fun `ingen response`() {
        assertEquals(
            "Fikk ikke kontakt med medlemskapstjenesten på 3 forsøk. Se sikkerlogg for detaljer",
            assertThrows<IllegalStateException> {
                exceptionTolk().tolk()
            }.message,
        )
    }

    private fun nyTolk(
        httpStatus: Int = 200,
        response: String,
    ) = Tolk("11111111111", LocalDate.EPOCH, LocalDate.EPOCH) {
        httpStatus to response
    }

    private fun exceptionTolk() =
        Tolk("11111111111", LocalDate.EPOCH, LocalDate.EPOCH) {
            throw IOException("Noe nettverkskrøll")
        }

    @Language("JSON")
    private val crazyRespone = """{"nøkkel":"verdi"}""".trimIndent()

    @Language("JSON")
    private fun speilvurderingResponse(
        svar: String,
        @Language("JSON") aarsaker: String? = null,
    ) =
        """
        {
          "soknadId" : "ed0286f6-6107-3d75-8266-e50d5736f403",
          "fnr" : "11111111111",
          "speilSvar" : "$svar"${aarsaker?.let { """, "aarsaker": $it""" } ?: ""}
        }
        """.trimIndent()
}
