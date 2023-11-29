package no.nav.helse.sparkel.sykepengeperioderapi

import java.time.LocalDate
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Organisasjonsnummer.Companion.organisasjosnummerOrNull
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class UsikkerGradTest {

    @Test
    fun `når det bare er én periode, stoler vi på graden`() {
        val liste = listOf(periode("123456789"))
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all{ it is InfotrygdperiodeMedSikkerGrad })
    }

    @Test
    fun `to perioder med forskjellige perioder har antagelig pålitelige grader`() {
        val liste = listOf(
            periode("123456789", fom = 1.januar, tom = 10.januar),
            periode("123456789", fom = 12.januar, tom = 20.januar)
        )
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all{ it is InfotrygdperiodeMedSikkerGrad })
    }

    @Test
    fun `to overlappende perioder med grad ulik 100 prosent er antagelig pålitelig`() {
        val liste = listOf(
            periode("100000000", fom = 1.januar, tom = 10.januar, grad = 30),
            periode("100000001", fom = 3.januar, tom = 13.januar, grad = 70)
        )
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all{ it is InfotrygdperiodeMedSikkerGrad })
    }

    @Test
    fun `to overlappende perioder med grad lik 100 prosent er suspekt`() {
        val liste = listOf(
            periode("100000000", fom = 1.januar, tom = 10.januar, grad = 100),
            periode("100000001", fom = 3.januar, tom = 13.januar, grad = 100)
        )
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all { it is InfotrygdperiodeMedUsikkerGrad })
    }

    @Test
    fun `to overlappende perioder med grad lik 100 prosent på samme arbeidsgiver er rart, men ikke usikker grad`() {
        val liste = listOf(
            periode("100000000", fom = 1.januar, tom = 10.januar, grad = 100),
            periode("100000000", fom = 3.januar, tom = 13.januar, grad = 100)
        )
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all { it is InfotrygdperiodeMedSikkerGrad })
    }

    @Test
    fun `sikker grad når en eller begge ikke er knyttet til en arbeidsgiver`() {
        val liste = listOf(
            periode(orgnr = null, fom = 1.januar, tom = 1.januar, grad = 100),
            periode(orgnr = null, fom = 1.januar, tom = 1.januar, grad = 100),
            periode(orgnr = "100000000", fom = 1.januar, tom = 1.januar, grad = 100),
        )
        val listeSomErUndersøkt = liste.markerUsikkerGrad()
        assertTrue(listeSomErUndersøkt.all{ it is InfotrygdperiodeMedSikkerGrad })
    }

    @Test
    fun `blanding av perioder med og uten sikker grad`() {
        val periode1 = periode("100000000", fom = 1.januar, tom = 1.januar, grad = 100)
        val periode2 = periode("100000000", fom = 2.januar, tom = 2.januar, grad = 100)
        val periode3 = periode("100000001", fom = 2.januar, tom = 2.januar, grad = 100)
        val periode4 = periode("100000002", fom = 3.januar, tom = 3.januar, grad = 100)
        val periode5 = periode("100000003", fom = 4.januar, tom = 4.januar, grad = 99)
        val periode6 = periode("100000004", fom = 4.januar, tom = 4.januar, grad = 100)
        val periode7 = periode("100000005", fom = 5.januar, tom = 6.januar, grad = 100)
        val periode8 = periode("100000006", fom = 6.januar, tom = 7.januar, grad = 100)
        val periode9 = periode("100000007", fom = 7.januar, tom = 8.januar, grad = 100)

        val infotrygdperioder = listOf(
            periode1, periode2, periode3, periode4, periode5, periode6, periode7, periode8, periode9
        )

        assertEquals(listOf(
            InfotrygdperiodeMedSikkerGrad(periode1),
            InfotrygdperiodeMedUsikkerGrad(periode2),
            InfotrygdperiodeMedUsikkerGrad(periode3),
            InfotrygdperiodeMedSikkerGrad(periode4),
            InfotrygdperiodeMedSikkerGrad(periode5),
            InfotrygdperiodeMedSikkerGrad(periode6),
            InfotrygdperiodeMedUsikkerGrad(periode7),
            InfotrygdperiodeMedUsikkerGrad(periode8),
            InfotrygdperiodeMedUsikkerGrad(periode9)
        ), infotrygdperioder.markerUsikkerGrad())
    }

    @Test
    fun `perioder med usikker grad får UsikkerGrad-tag`() {
        val periode1 = periode("100000000", fom = 1.januar, tom = 1.januar, grad = 100)
        val periode2 = periode("100000001", fom = 1.januar, tom = 1.januar, grad = 100)
        val periode3 = periode("100000003", fom = 2.januar, tom = 2.januar, grad = 100)
        val infotrygdperioder = listOf(periode1, periode2, periode3).markerUsikkerGrad()
        assertEquals(setOf("UsikkerGrad"), infotrygdperioder[0].tags)
        assertEquals(setOf("UsikkerGrad"), infotrygdperioder[1].tags)
        assertEquals(emptySet<String>(), infotrygdperioder[2].tags)
    }
}

private fun periode(orgnr: String?, fom: LocalDate = LocalDate.now(), tom: LocalDate = LocalDate.now(), grad: Int = 100) =
    Infotrygdperiode(Personidentifikator("12345612345"), orgnr?.organisasjosnummerOrNull, fom, tom, grad)

fun Int.januar(year: Int = 2018) = LocalDate.of(year, 1, this)
val Int.januar get() = this.januar()