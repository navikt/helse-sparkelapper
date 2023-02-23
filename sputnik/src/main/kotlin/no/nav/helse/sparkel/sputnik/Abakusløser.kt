package no.nav.helse.sparkel.sputnik

import java.time.LocalDate
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.abakus.Ytelse
import no.nav.helse.sparkel.sputnik.abakus.Stønadsperiode

internal class Abakusløser(
    private val abakusClient: AbakusClient
): Foreldrepengerløser {

    override suspend fun hent(aktørId: String, fom: LocalDate, tom: LocalDate): Foreldrepengerløsning {
        val stønadsperioder = abakusClient.hent(aktørId, fom = fom, tom = tom, Foreldrepenger, Svangerskapspenger)
        return Foreldrepengerløsning(
            foreldrepengeytelse = stønadsperioder.filter { it.ytelse == Foreldrepenger }.ytelse(aktørId),
            svangerskapsytelse = stønadsperioder.filter { it.ytelse == Svangerskapspenger }.ytelse(aktørId)
        )
    }

    private companion object {
        private val Foreldrepenger = Ytelse("FORELDREPENGER")
        private val Svangerskapspenger = Ytelse("SVANGERSKAPSPENGER")

        private fun List<Stønadsperiode>.ytelse(aktørId: String): YtelseDto? {
            if (isEmpty()) return null
            val førsteFom = minOf { it.fom }
            val sisteTom = maxOf { it.tom }
            val vedtatt = first().vedtatt

            return YtelseDto(
                aktørId = aktørId,
                fom = førsteFom,
                tom = sisteTom,
                vedtatt = vedtatt,
                perioder = map { Periode(it.fom, it.tom) }
            )
        }
    }
}