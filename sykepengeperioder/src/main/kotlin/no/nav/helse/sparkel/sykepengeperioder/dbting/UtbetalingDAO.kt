package no.nav.helse.sparkel.sykepengeperioder.dbting

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.sykepengeperioder.Fnr
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingsperiode
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import javax.sql.DataSource

class UtbetalingDAO(
    private val dataSource: DataSource
) {
    internal fun utbetalinger(fnr: Fnr, vararg seq: Int): List<UtbetalingDTO> {
        if (seq.isEmpty()) return emptyList()
        return sessionOf(dataSource).use { session ->
            val antallSpørsmålstegn = seq.joinToString(",") { "?" }

            @Language("Oracle")
            val statement = """
                    select
        is15_utbetfom
        , is15_utbettom
        , is15_grad
        , is15_op
        , is15_utbetdato
        , is15_dsats
        , is15_type
        , is15_arbgivnr
        , is10_arbufoer_seq
        from is_utbetaling_15
        where f_nr = ?               -- 1
        and is10_arbufoer_seq in ($antallSpørsmålstegn)  -- 2
        and is15_korr <> 'KORR'
                """
            session.run(
                queryOf(statement, fnr.formatAsITFnr(), *seq.toTypedArray()).map { rs ->
                    UtbetalingDTO(
                        fom = rs.intOrNullToLocalDate("is15_utbetfom"),
                        tom = rs.intOrNullToLocalDate("is15_utbettom"),
                        grad = rs.string("is15_grad").trim(),
                        oppgjorType = rs.string("is15_op").trim { it <= '\u0020' },
                        utbetalt = rs.intOrNullToLocalDate("is15_utbetdato"),
                        dagsats = rs.double("is15_dsats"),
                        periodeType = rs.string("is15_type").trim(),
                        arbOrgnr = rs.string("is15_arbgivnr"),
                        sekvensId = rs.int("is10_arbufoer_seq")
                    )
                }.asList
            )
        }
    }

    internal data class UtbetalingDTO(
        val fom: LocalDate?,
        val tom: LocalDate?,
        val grad: String,
        val oppgjorType: String,
        val utbetalt: LocalDate?,
        val dagsats: Double,
        val periodeType: String,
        val arbOrgnr: String,
        val sekvensId: Int
    ) {
        internal companion object {
            internal fun tilHistorikkutbetaling(utbetalinger: List<UtbetalingDTO>) =
                utbetalinger.map { it.tilHistorikkUtbetaling() }
        }

        private fun tilHistorikkUtbetaling() = Utbetalingshistorikk.Utbetaling(
            fom = fom,
            tom = tom,
            utbetalingsGrad = grad,
            oppgjorsType = oppgjorType,
            utbetalt = utbetalt,
            dagsats = dagsats,
            typeKode = periodeType,
            typeTekst = periodeType.toTypeTekst(),
            orgnummer = arbOrgnr
        )

        internal fun tilUtbetalingsperiode(arbeidsKategori: String) = Utbetalingsperiode(
            arbeidsKategoriKode = arbeidsKategori,
            fom = fom,
            tom = tom,
            dagsats = dagsats,
            grad = grad,
            typetekst = periodeType.toTypeTekst(),
            organisasjonsnummer = arbOrgnr
        )

        private fun String.toTypeTekst() =
            when (this) {
                "0" -> "Utbetaling"
                "1" -> "ReduksjonMedlem"
                "2" -> "EtterbetalingForhøyetLønn"
                "3" -> "EtterbetalingInntektMangler"
                "4" -> "KontertRegnskap"
                "5" -> "ArbRef"
                "6" -> "ReduksjonArbRef"
                "7" -> "Tilbakeført"
                "8" -> "Konvertert"
                "9" -> "Ferie"
                "O" -> "Opphold"
                "S" -> "Sanksjon"
                else -> "Ukjent.."
            }
    }
}
