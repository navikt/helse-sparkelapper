package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate
import no.nav.helse.sparkel.infotrygd.api.Infotrygdutbetalinger.Companion.filtrer
import no.nav.helse.sparkel.infotrygd.api.Organisasjonsnummer.Companion.organisasjosnummerOrNull
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PeriodefilterTest {

    @Test
    fun `filtrerer bort irrelevante perioder`() {
        val januar = periode(2.januar, 28.januar)
        val februar = periode(5.februar, 25.februar)
        val mars = periode(10.mars, 28.mars)
        val perioder = listOf(januar, februar, mars)

        assertEquals(listOf(februar), perioder.filtrer(29.januar, 9.mars))
        assertEquals(listOf(februar, mars), perioder.filtrer(29.januar, 10.mars))
        assertEquals(perioder, perioder.filtrer(28.januar, 10.mars))
        assertEquals(emptyList<Infotrygdperiode>(), perioder.filtrer(1.januar, 1.januar))
        assertEquals(emptyList<Infotrygdperiode>(), perioder.filtrer(29.mars, 29.mars))
    }

    private companion object {
        private fun periode(fom: LocalDate, tom: LocalDate) = Infotrygdperiode(
            personidentifikator = Personidentifikator("11111111111"),
            organisasjonsnummer = "999999999".organisasjosnummerOrNull,
            fom = fom,
            tom = tom,
            grad = 100
        )
        val Int.januar get() = LocalDate.of(2018, 1, this)
        val Int.februar get() = LocalDate.of(2018, 2, this)
        val Int.mars get() = LocalDate.of(2018, 3, this)
    }
}