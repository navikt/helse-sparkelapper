package no.nav.helse.sparkel.sykepengeperioder

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

internal class InfotrygdService(
    private val periodeDAO: PeriodeDAO,
    private val utbetalingDAO: UtbetalingDAO,
    private val inntektDAO: InntektDAO,
    private val statslønnDAO: StatslønnDAO,
    private val feriepengeDAO: FeriepengeDAO
) {

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }

    fun løsningForSykepengehistorikkbehov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): List<Utbetalingshistorikk>? {
        try {
            val perioder = periodeDAO.perioder(
                fødselsnummer,
                fom,
                tom
            )
            val historikk: List<Utbetalingshistorikk> = perioder
                .sortedByDescending { it.sykemeldtFom }
                .mapIndexed { index, periode ->
                    periode.tilUtbetalingshistorikk(
                        UtbetalingDAO.UtbetalingDTO.tilHistorikkutbetaling(
                            utbetalingDAO.utbetalinger(
                                fødselsnummer,
                                periode.seq
                            )
                        ),
                        InntektDAO.InntektDTO.tilInntektsopplysninger(
                            inntektDAO.inntekter(
                                fødselsnummer,
                                periode.seq
                            )
                        ),
                        index == 0 && statslønnDAO.harStatslønn(fødselsnummer, periode.seq)
                    )
                }
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            return historikk
        } catch (err: Exception) {
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            return null
        }
    }

    fun løsningForSykepengehistorikkMk2behov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): Sykepengehistorikk? {
        try {
            val perioder = periodeDAO.perioder(
                fødselsnummer,
                fom,
                tom
            )
            val utbetalingshistorikk = perioder
                .sortedByDescending { it.sykemeldtFom }
                .mapIndexed { index, periode ->
                    periode.tilUtbetalingshistorikk(
                        UtbetalingDAO.UtbetalingDTO.tilHistorikkutbetaling(
                            utbetalingDAO.utbetalinger(
                                fødselsnummer,
                                periode.seq
                            )
                        ),
                        InntektDAO.InntektDTO.tilInntektsopplysninger(
                            inntektDAO.inntekter(
                                fødselsnummer,
                                periode.seq
                            )
                        ),
                        index == 0 && statslønnDAO.harStatslønn(fødselsnummer, periode.seq),
                    )
                }
            val feriepengehistorikk = FeriepengeDAO.FeriepengeDTO.tilFeriepenger(
                feriepengeDAO.feriepenger(fødselsnummer, fom, tom)
            )
            val utbetalinger = utbetalingshistorikk.flatMap { it.utbetalteSykeperioder }
            val inntektshistorikk = utbetalingshistorikk.flatMap { it.inntektsopplysninger }
            val harStatslønn = utbetalingshistorikk.any { it.statslønn }

            val arbeidskategorikoder: Map<String, LocalDate> = utbetalingshistorikk.fold(emptyMap()) { acc, periode ->
                val sisteUtbetalingsdagIPerioden = periode.utbetalteSykeperioder.mapNotNull { it.tom }.maxOrNull()
                if (periode.arbeidsKategoriKode !in acc && sisteUtbetalingsdagIPerioden != null) {
                    acc + mapOf(periode.arbeidsKategoriKode to sisteUtbetalingsdagIPerioden)
                } else acc
            }

            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )

            return Sykepengehistorikk(
                utbetalinger = utbetalinger,
                inntektshistorikk = inntektshistorikk,
                feriepengehistorikk = feriepengehistorikk,
                harStatslønn = harStatslønn,
                arbeidskategorikoder = arbeidskategorikoder
            )
        } catch (err: Exception) {
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            return null
        }
    }

    fun løsningForHentInfotrygdutbetalingerbehov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): List<Utbetalingsperiode>? {
        try {
            val perioder = periodeDAO.perioder(
                fødselsnummer,
                fom,
                tom
            )
            val historikk: List<Utbetalingsperiode> = perioder.flatMap { periode ->
                periode.tilUtbetalingsperiode(
                    utbetalingDAO.utbetalinger(fødselsnummer, periode.seq)
                )
            }
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            return historikk
        } catch (err: Exception) {
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            return null
        }
    }
}
