package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate
import javax.sql.DataSource
import no.nav.helse.sparkel.infotrygd.Fnr
import no.nav.helse.sparkel.infotrygd.PeriodeDAO
import no.nav.helse.sparkel.infotrygd.UtbetalingDAO
import no.nav.helse.sparkel.infotrygd.Utbetalingsperiode

class Infotrygdutbetalinger (dataSource: DataSource) {
    private val utbetalingDAO = UtbetalingDAO(dataSource)
    private val periodeDAO = PeriodeDAO(dataSource)

    fun utbetalinger(personidentifikator: Personidentifikator, fom: LocalDate, tom:LocalDate): List<Infotrygdperiode> {
        val fødselsnummer = Fnr(personidentifikator.toString())
        val perioder = periodeDAO.perioder(fødselsnummer, fom, tom)
        val historikk: List<Utbetalingsperiode> = perioder.flatMap { periode ->
            periode.tilUtbetalingsperiode(
                utbetalingDAO.utbetalinger(fødselsnummer, periode.seq)
            )
        }

        return historikk.filter { it.fom != null }.map { Infotrygdperiode(
            fom = it.fom!!,
            tom = it.tom ?: LocalDate.MAX,
            grad = it.grad.somGrad
        )}
    }

    private val String.somGrad get() = takeUnless { it.isBlank() }?.toInt() ?: 0
}