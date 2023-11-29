package no.nav.helse.sparkel.sykepengeperioderapi

import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode

internal class InfotrygdperiodeMedSikkerGrad(
    infotrygdperiode: Infotrygdperiode
) : Infotrygdperiode(infotrygdperiode.personidentifikator, infotrygdperiode.organisasjonsnummer, infotrygdperiode.fom, infotrygdperiode.tom, infotrygdperiode.grad) {
    override fun equals(other: Any?) = other is InfotrygdperiodeMedSikkerGrad && super.equals(other)
    override fun toString() = "Sikker grad ${super.toString()}"
}

internal class InfotrygdperiodeMedUsikkerGrad(
    infotrygdperiode: Infotrygdperiode
) : Infotrygdperiode(infotrygdperiode.personidentifikator, infotrygdperiode.organisasjonsnummer, infotrygdperiode.fom, infotrygdperiode.tom, infotrygdperiode.grad) {
    override fun equals(other: Any?) = other is InfotrygdperiodeMedUsikkerGrad && super.equals(other)
    override fun toString() = "Usikker grad ${super.toString()}"
}

private fun Infotrygdperiode.overlapperMed(other: Infotrygdperiode): Boolean {
    val start = maxOf(this.fom, other.fom)
    val slutt = minOf(this.tom, other.tom)
    return start <= slutt
}

private fun Infotrygdperiode.overlappPåAnnenArbeidsgiverMedGrad100(other: Infotrygdperiode) =
    this.organisasjonsnummer != null && other.organisasjonsnummer != null &&    // Kun aktuelt å sammenligne når vi har orgnr på begge
    this.organisasjonsnummer != other.organisasjonsnummer &&                    // Kun aktuelt når periodene er på forskjellige arbeidsgivere
    this.grad == 100 && other.grad == 100 &&                                    // Kun aktuelt når begge periodene har 100%
    this.overlapperMed(other)                                                   // .. også må de jo overlappe da

private fun List<Infotrygdperiode>.uten(index: Int) = toMutableList().apply { removeAt(index) }

internal fun List<Infotrygdperiode>.markerUsikkerGrad(): List<Infotrygdperiode> {
    val sortert = sortedBy { it.fom }
    return sortert.mapIndexed { index, denne ->
        val resten = sortert.uten(index)
        if (resten.any { it.overlappPåAnnenArbeidsgiverMedGrad100(denne) }) InfotrygdperiodeMedUsikkerGrad(denne)
        else InfotrygdperiodeMedSikkerGrad(denne)
    }
}