package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

internal class SuperNovaHistorikk(
    val utbetalinger: List<Utbetalingshistorikk.Utbetaling>,
    val feriepengehistorikk: List<Utbetalingshistorikk.Feriepenger>,
    val inntektshistorikk: List<Utbetalingshistorikk.Inntektsopplysninger>,
    val harStatslønn: Boolean,
    var arbeidskategorikoder: Map<String, LocalDate>
) {

}