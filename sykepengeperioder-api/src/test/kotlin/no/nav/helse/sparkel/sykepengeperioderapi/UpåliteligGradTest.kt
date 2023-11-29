package no.nav.helse.sparkel.sykepengeperioderapi

import java.time.LocalDate
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Organisasjonsnummer.Companion.organisasjosnummerOrNull
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class UpåliteligGradTest {

    @Test
    fun `når det bare er én periode, stoler vi på graden`() {
        val liste = listOf(periode("123456789"))
        val listeSomErUndersøkt = liste.markerUsikreGrader()
        assertTrue(listeSomErUndersøkt.all { it.pålitelig })
    }

    @Test
    fun `to perioder med forskjellige perioder har antagelig pålitelige grader`() {
        val liste = listOf(
            periode("123456789", fom = 1.januar, tom = 10.januar),
            periode("123456789", fom = 12.januar, tom = 20.januar)
        )
        val listeSomErUndersøkt = liste.markerUsikreGrader()
        assertTrue(listeSomErUndersøkt.all { it.pålitelig })
    }

    @Test
    fun `to overlappende perioder med grad ulik 100 % er antagelig pålitelig`() {
        val liste = listOf(
            periode("100000000", fom = 1.januar, tom = 10.januar, grad = 30),
            periode("100000001", fom = 3.januar, tom = 13.januar, grad = 70)
        )
        val listeSomErUndersøkt = liste.markerUsikreGrader()
        assertTrue(listeSomErUndersøkt.all { it.pålitelig })
    }

    @Disabled
    @Test
    fun `to overlappende perioder med grad lik 100 % er suspekt`() {
        val liste = listOf(
            periode("100000000", fom = 1.januar, tom = 10.januar, grad = 100),
            periode("100000001", fom = 3.januar, tom = 13.januar, grad = 100)
        )
        val listeSomErUndersøkt = liste.markerUsikreGrader()
        assertTrue(listeSomErUndersøkt.none { it.pålitelig })
    }
}

private fun periode(orgnr: String, fom: LocalDate = LocalDate.now(), tom: LocalDate = LocalDate.now(), grad: Int = 100) =
    Infotrygdperiode(Personidentifikator("12345612345"), orgnr.organisasjosnummerOrNull, fom, tom, grad)

fun Int.januar(year: Int = 2018) = LocalDate.of(year, 1, this)
val Int.januar get() = this.januar()