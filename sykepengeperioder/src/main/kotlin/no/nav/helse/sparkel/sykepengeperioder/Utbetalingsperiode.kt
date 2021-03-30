package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

internal class Utbetalingsperiode(
    val arbeidsKategoriKode: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val dagsats: Double,
    val grad: String,
    val typetekst: String?,
    val organisasjonsnummer: String?
) {

    internal companion object {
        internal fun tilPerioder(sykepenger: Sykepenger): List<Utbetalingsperiode> = sykepenger
            .sykmeldingsperioder
            .flatMap {
                val arbeidsKategoriKode = it.arbeidsKategoriKode
                it.utbetalingList.map { utbetaling ->
                    Utbetalingsperiode(
                        arbeidsKategoriKode,
                        fom = utbetaling.fom,
                        tom = utbetaling.tom,
                        dagsats = utbetaling.dagsats,
                        grad = utbetaling.utbetalingsGrad,
                        typetekst = utbetaling.typeTekst,
                        organisasjonsnummer = utbetaling.arbOrgnr
                    )
                }
            }
    }
}
