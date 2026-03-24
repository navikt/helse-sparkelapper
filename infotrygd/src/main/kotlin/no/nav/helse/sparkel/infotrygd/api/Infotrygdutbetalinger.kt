package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate
import javax.sql.DataSource
import no.nav.helse.sparkel.infotrygd.Fnr
import no.nav.helse.sparkel.infotrygd.PeriodeDAO
import no.nav.helse.sparkel.infotrygd.UtbetalingDAO
import no.nav.helse.sparkel.infotrygd.api.Organisasjonsnummer.Companion.organisasjosnummerOrNull

class Infotrygdutbetalinger(dataSource: DataSource) {
    private val utbetalingDAO = UtbetalingDAO { dataSource }
    private val periodeDAO = PeriodeDAO { dataSource }

    fun utbetalinger(personidentifikatorer: Set<Personidentifikator>, fom: LocalDate, tom:LocalDate, inkluderAllePeriodetyper: Boolean): List<Infotrygdperiode> {
        return personidentifikatorer
            .map { personidentifikator -> Fnr(personidentifikator.toString()) }
            .flatMap { fnr ->
                val perioder = periodeDAO.perioder(fnr, fom, tom)
                val sekvensIdeer = perioder.map { it.seq }.toIntArray()
                utbetalingDAO.utbetalinger(fnr, *sekvensIdeer)
                    .filter { inkluderAllePeriodetyper || it.periodeType in utbetalingstyper }
                    .map { utbetaling ->
                        Infotrygdperiode(
                            personidentifikator = Personidentifikator(fnr.toString()),
                            organisasjonsnummer = utbetaling.arbOrgnr.organisasjosnummerOrNull,
                            fom = utbetaling.fom!!,
                            tom = utbetaling.tom ?: LocalDate.MAX,
                            grad = utbetaling.grad.somGrad,
                            dagsats = utbetaling.dagsats.toBigDecimal(),
                            type = Periodetype.fraKode(utbetaling.periodeType)
                        )
                    }
            }
            .filtrer(fom, tom)
    }

    internal companion object {
        private val utbetalingstyper = setOf("0", "1", "5", "6")
        private val String.somGrad get() = takeUnless { it.isBlank() }?.toInt() ?: 0

        internal fun List<Infotrygdperiode>.filtrer(fom: LocalDate, tom: LocalDate) =
            filterNot { it.tom < fom }.filterNot { it.fom > tom }
    }
}
