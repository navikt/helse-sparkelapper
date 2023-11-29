package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate

open class Infotrygdperiode(
    val personidentifikator: Personidentifikator,
    val organisasjonsnummer: Organisasjonsnummer?,
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int
) {
    override fun equals(other: Any?) = other is Infotrygdperiode &&
            other.personidentifikator == this.personidentifikator &&
            other.organisasjonsnummer == this.organisasjonsnummer &&
            other.fom == this.fom &&
            other.tom == this.tom &&
            other.grad == this.grad

    override fun toString() = "$fom - $tom $grad%"
}