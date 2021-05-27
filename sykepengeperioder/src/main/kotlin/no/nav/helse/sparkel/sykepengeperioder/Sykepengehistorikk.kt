package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

internal class Sykepengehistorikk(
    val utbetalinger: List<Utbetalingshistorikk.Utbetaling>,
    val feriepengehistorikk: List<Feriepenger>,
    val inntektshistorikk: List<Utbetalingshistorikk.Inntektsopplysninger>,
    val harStatslønn: Boolean,
    val arbeidskategorikoder: List<Arbeidskategori>,
    val feriepengerSkalBeregnesManuelt: Boolean
) {
    internal class Feriepenger(
        val orgnummer: String,
        val beløp: Double,
        val fom: LocalDate,
        val tom: LocalDate
    )

    internal class Arbeidskategori(
        val kode: String,
        val fom: LocalDate,
        val tom: LocalDate
    )
}
