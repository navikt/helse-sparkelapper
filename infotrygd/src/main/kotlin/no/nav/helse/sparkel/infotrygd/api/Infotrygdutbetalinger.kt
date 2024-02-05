package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate
import javax.sql.DataSource
import no.nav.helse.sparkel.infotrygd.Fnr
import no.nav.helse.sparkel.infotrygd.PeriodeDAO
import no.nav.helse.sparkel.infotrygd.UtbetalingDAO
import no.nav.helse.sparkel.infotrygd.api.Organisasjonsnummer.Companion.organisasjosnummerOrNull

class Infotrygdutbetalinger (dataSource: DataSource) {
    private val utbetalingDAO = UtbetalingDAO(dataSource)
    private val periodeDAO = PeriodeDAO(dataSource)

    fun utbetalinger(personidentifikatorer: Set<Personidentifikator>, fom: LocalDate, tom:LocalDate): List<Infotrygdperiode> {
        val fnrTilUtbetalinger = personidentifikatorer
            .map { personidentifikator -> Fnr(personidentifikator.toString()) }
            .associateWith { fnr -> periodeDAO.perioder(fnr, fom, tom) }
            .mapValues { (fnr, perioder) ->
                val sekvensIdeer = perioder.map { it.seq }.toIntArray()
                utbetalingDAO.utbetalinger(fnr, *sekvensIdeer)
            }

      return fnrTilUtbetalinger.flatMap { (fnr, utbetalingsperioder) ->
            utbetalingsperioder.filter { it.periodeType in utbetalingstyper }.map {
                Infotrygdperiode(
                    personidentifikator = Personidentifikator(fnr.toString()),
                    organisasjonsnummer = it.arbOrgnr.organisasjosnummerOrNull,
                    fom = it.fom!!,
                    tom = it.tom ?: LocalDate.MAX,
                    grad = it.grad.somGrad
                )
            }
        }.filtrer(fom, tom)
    }

    internal companion object {
        private val utbetalingstyper = setOf("0", "1", "5", "6")
        private val String.somGrad get() = takeUnless { it.isBlank() }?.toInt() ?: 0

        internal fun List<Infotrygdperiode>.filtrer(fom: LocalDate, tom: LocalDate) =
            filterNot { it.tom < fom }.filterNot { it.fom > tom }
    }
}