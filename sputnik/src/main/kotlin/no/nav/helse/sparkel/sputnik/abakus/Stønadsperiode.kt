package no.nav.helse.sparkel.sputnik.abakus

import java.time.LocalDate
import java.time.LocalDateTime

internal class Stønadsperiode(val fom: LocalDate, val tom: LocalDate, val grad: Int, val ytelse: Ytelse, val vedtatt: LocalDateTime) {

    override fun hashCode() =
        fom.hashCode() + tom.hashCode() + grad.hashCode() + ytelse.hashCode() + vedtatt.hashCode()

    override fun equals(other: Any?) =
        other is Stønadsperiode && other.fom == fom && other.tom == tom && other.grad == grad && other.ytelse == ytelse && other.vedtatt == vedtatt

    override fun toString() = "$fom til $tom ($grad%) med $ytelse vedtatt $vedtatt"
}
