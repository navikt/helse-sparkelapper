package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

internal class Sykepengehistorikk(
    val utbetalinger: List<Utbetalingshistorikk.Utbetaling>,
    val feriepengehistorikk: List<Utbetalingshistorikk.Feriepenger>,
    val inntektshistorikk: List<Utbetalingshistorikk.Inntektsopplysninger>,
    val harStatslønn: Boolean,
    val arbeidskategorikoder: Map<String, LocalDate>,
    val feriepengerSkalBeregnesManuelt: Boolean
) {

}