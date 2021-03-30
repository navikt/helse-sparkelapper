package no.nav.helse.sparkel.sykepengeperioder

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.StatslønnDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.UtbetalingDAO
import no.nav.helse.sparkel.sykepengeperioder.infotrygd.InfotrygdClient
import org.slf4j.LoggerFactory
import java.time.LocalDate
import javax.sql.DataSource

internal class InfotrygdService(private val infotrygdClient: InfotrygdClient, private val dataSource: DataSource) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForSykepengehistorikkbehov(
        behovId: String,
        fødselsnummer: Fnr,
        fom: LocalDate,
        tom: LocalDate
    ): List<Utbetalingshistorikk> {
        try {
            val historikk = Utbetalingshistorikk.tilPerioder(
                infotrygdClient.hentHistorikk(
                    behovId = behovId,
                    fnr = fødselsnummer,
                    fom = fom,
                    tom = tom
                )
            )
            val perioder = PeriodeDAO(dataSource).perioder(
                fødselsnummer,
                fom,
                tom
            )
            val historikk3: List<Utbetalingshistorikk> = perioder
                .sortedByDescending { it.sykemeldtFom }
                .mapIndexed { index, periode ->
                    periode.tilUtbetalingshistorikk(
                        UtbetalingDAO.UtbetalingDTO.tilHistorikkutbetaling(UtbetalingDAO(dataSource).utbetalinger(fødselsnummer, periode.seq)),
                        InntektDAO.InntektDTO.tilInntektsopplysninger(InntektDAO(dataSource).inntekter(fødselsnummer, periode.seq)),
                        index == 0 && StatslønnDAO(dataSource).harStatslønn(fødselsnummer, periode.seq)
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
            val historikk = Utbetalingsperiode.tilPerioder(
                infotrygdClient.hentHistorikk(
                    behovId = behovId,
                    fnr = fødselsnummer,
                    fom = fom,
                    tom = tom
                )
            )
            val perioder = PeriodeDAO(dataSource).perioder(
                fødselsnummer,
                fom,
                tom
            )
            val historikk3: List<Utbetalingsperiode> = perioder.flatMap { periode ->
                periode.tilUtbetalingsperiode(
                    UtbetalingDAO(dataSource).utbetalinger(fødselsnummer, periode.seq)
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
