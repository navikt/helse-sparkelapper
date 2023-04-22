package no.nav.helse.sparkel.sykepengeperioder

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO.PeriodeDTO.Companion.ekstraFerieperioder
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
                .filter { it.inntektsopplysninger.isNotEmpty() || it.utbetalteSykeperioder.isNotEmpty() }
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            return historikk
        } catch (err: Exception) {
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}, {}",
                keyValue("id", behovId),
                keyValue("fødselsnummer", fødselsnummer),
                err
            )
            return null
        }
    }

    fun løsningForSykepengehistorikkForFeriepenger(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): Sykepengehistorikk? {
        try {
            val perioder = periodeDAO.perioder(fødselsnummer, fom, tom).sortedByDescending { it.sykemeldtFom }
            val sekvensIdeer = perioder.map { it.seq }.toIntArray()

            val feriepengehistorikk =
                FeriepengeDAO.FeriepengeDTO.tilFeriepenger(feriepengeDAO.feriepenger(fødselsnummer, fom, tom))

            val feriepengerSkalBeregnesManuelt = feriepengeDAO.feriepengerSkalBeregnesManuelt(fødselsnummer, fom, tom)

            val utbetalingDAOer = utbetalingDAO.utbetalinger(
                fødselsnummer,
                *sekvensIdeer
            )

            val utbetalinger =
                UtbetalingDAO.UtbetalingDTO.tilHistorikkutbetaling(utbetalingDAOer) + perioder.ekstraFerieperioder()

            val inntektshistorikk =
                InntektDAO.InntektDTO.tilInntektsopplysninger(inntektDAO.inntekter(fødselsnummer, *sekvensIdeer))

            val harStatslønn = perioder.firstOrNull()?.let { statslønnDAO.harStatslønn(fødselsnummer, it.seq) } ?: false

            val arbeidskategorikoder = perioder
                .mapNotNull { periode ->
                    val periodeFom = utbetalingDAOer
                        .filter { it.sekvensId == periode.seq }
                        .filterNot { it.periodeType == "7" }
                        .mapNotNull { it.fom }
                        .minOrNull()
                    val periodeTom = utbetalingDAOer
                        .filter { it.sekvensId == periode.seq }
                        .filterNot { it.periodeType == "7" }
                        .mapNotNull { it.tom }
                        .maxOrNull()
                    if (periodeFom != null && periodeTom != null) {
                        Sykepengehistorikk.Arbeidskategori(
                            kode = periode.arbeidsKategori,
                            fom = periodeFom,
                            tom = periodeTom
                        )
                    } else null
                }
                .sortedBy { it.fom }

            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )

            return Sykepengehistorikk(
                utbetalinger = utbetalinger,
                inntektshistorikk = inntektshistorikk,
                feriepengehistorikk = feriepengehistorikk,
                harStatslønn = harStatslønn,
                arbeidskategorikoder = arbeidskategorikoder,
                feriepengerSkalBeregnesManuelt = feriepengerSkalBeregnesManuelt
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
