package no.nav.helse.sparkel.sputnik

import java.time.LocalDate
import no.nav.helse.sparkel.abakus.AbakusClient
import no.nav.helse.sparkel.abakus.AktørId.Companion.aktørId
import no.nav.helse.sparkel.abakus.Stønadsperiode
import no.nav.helse.sparkel.abakus.Ytelse as AbakusYtelse

internal class Abakusløser(
    private val abakusClient: AbakusClient
): Foreldrepengerløser {

    override suspend fun hent(aktørId: String, fom: LocalDate, tom: LocalDate): Foreldrepengerløsning {
        val stønadsperioder = abakusClient.hent(identifiktor = aktørId.aktørId, fom = fom, tom = tom, Foreldrepenger, Svangerskapspenger)
        return Foreldrepengerløsning(
            foreldrepengeytelse = stønadsperioder.filter { it.ytelse == Foreldrepenger }.ytelse(aktørId),
            svangerskapsytelse = stønadsperioder.filter { it.ytelse == Svangerskapspenger }.ytelse(aktørId)
        )
    }

    private companion object {
        private val Foreldrepenger = AbakusYtelse("FORELDREPENGER")
        private val Svangerskapspenger = AbakusYtelse("SVANGERSKAPSPENGER")

        private fun List<Stønadsperiode>.ytelse(aktørId: String): Ytelse? {
            if (isEmpty()) return null
            val førsteFom = minOf { it.fom }
            val sisteTom = maxOf { it.tom }
            val vedtatt = first().vedtatt

            return Ytelse(
                aktørId = aktørId,
                fom = førsteFom,
                tom = sisteTom,
                vedtatt = vedtatt,
                perioder = map { Periode(it.fom, it.tom) }
            )
        }
    }
}