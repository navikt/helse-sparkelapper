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

    fun utbetalinger(personidentifikatore: Set<Personidentifikator>, fom: LocalDate, tom:LocalDate): List<Infotrygdperiode> {
        val fødselsnummere = personidentifikatore.map { personidentifikator-> Fnr(personidentifikator.toString()) }
        val perioder = periodeDAO.perioder(fødselsnummere, fom, tom)
        val historikk: List<Utbetalingsperiode> = perioder.flatMap { periode ->
            periode.tilUtbetalingsperiode(
                utbetalingDAO.utbetalinger(fødselsnummere, periode.seq)
            )
        }

        return historikk.filter { it.fom != null }.map { Infotrygdperiode(
            organisasjonsnummer = it.organisasjonsnummer?.takeIf { orgnr -> orgnr.matches(organisasjonsnummerRegex) },
            fom = it.fom!!,
            tom = it.tom ?: LocalDate.MAX,
            grad = it.grad.somGrad
        )}
    }

    private companion object {
        private val String.somGrad get() = takeUnless { it.isBlank() }?.toInt() ?: 0
        private val organisasjonsnummerRegex = "\\d{9}".toRegex()
    }
}