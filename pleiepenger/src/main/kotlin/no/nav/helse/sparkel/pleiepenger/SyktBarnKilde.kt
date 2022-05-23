package no.nav.helse.sparkel.pleiepenger

import java.time.LocalDate

internal interface SyktBarnKilde {
    fun pleiepenger(fnr: String, fom: LocalDate, tom: LocalDate): Set<Stønadsperiode>
    fun omsorgspenger(fnr: String, fom: LocalDate, tom: LocalDate): Set<Stønadsperiode>
    fun opplæringspenger(fnr: String, fom: LocalDate, tom: LocalDate): Set<Stønadsperiode>
}