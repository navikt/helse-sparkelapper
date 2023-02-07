package no.nav.helse.sparkel.abakus

import java.time.LocalDate

class Stønadsperiode(val fom: LocalDate, val tom: LocalDate, val grad: Int, val ytelse: Ytelse) {

    override fun hashCode() =
        fom.hashCode() + tom.hashCode() + grad.hashCode() + ytelse.hashCode()

    override fun equals(other: Any?) =
        other is Stønadsperiode && other.fom == fom && other.tom == tom && other.grad == grad && other.ytelse == ytelse

    override fun toString() = "$fom til $tom ($grad%) med $ytelse"
}
