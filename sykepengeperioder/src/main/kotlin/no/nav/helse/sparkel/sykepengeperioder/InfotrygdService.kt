package no.nav.helse.sparkel.sykepengeperioder

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.StatslønnDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.UtbetalingDAO
import org.slf4j.LoggerFactory
import java.time.LocalDate

internal class InfotrygdService(
    private val periodeDAO: PeriodeDAO,
    private val utbetalingDAO: UtbetalingDAO,
    private val inntektDAO: InntektDAO,
    private val statslønnDAO: StatslønnDAO
) {

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(this::class.java)
    }

    fun løsningForSykepengehistorikkbehov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): List<Utbetalingshistorikk> {
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
            log.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            return historikk
        } catch (err: Exception) {
            log.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            return emptyList()
        }
    }

    fun løsningForHentInfotrygdutbetalingerbehov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): List<Utbetalingsperiode> {
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
            log.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            sikkerlogg.info(
                "løser behov: {}",
                keyValue("id", behovId)
            )
            return historikk
        } catch (err: Exception) {
            log.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            sikkerlogg.warn(
                "feil ved henting av infotrygd-data: ${err.message} for {}",
                keyValue("id", behovId),
                err
            )
            return emptyList()
        }
    }
}
