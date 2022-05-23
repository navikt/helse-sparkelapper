package no.nav.helse.sparkel.pleiepenger

import java.time.LocalDate

class Stønadsperiode(
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int
) {
    override fun hashCode() =
        fom.hashCode() + tom.hashCode() + grad.hashCode()

    override fun equals(other: Any?) =
        other is Stønadsperiode && other.fom == fom && other.tom == tom && other.grad == grad
}
