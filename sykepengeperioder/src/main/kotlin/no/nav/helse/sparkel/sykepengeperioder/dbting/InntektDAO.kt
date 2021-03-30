package no.nav.helse.sparkel.sykepengeperioder.dbting

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.sykepengeperioder.Fnr
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk.Inntektsopplysninger
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk.Inntektsopplysninger.PeriodeKode.Premiegrunnlag
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.time.LocalDate
import javax.sql.DataSource

internal class InntektDAO(
    private val dataSource: DataSource
) {
    internal companion object {
        private val log = LoggerFactory.getLogger(InntektDAO::class.java)
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun inntekter(fnr: Fnr, seq: Int): List<InntektDTO> {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                    select
        is13_arbgivnr
         , is13_spfom
         , is13_ref_tom
         , is13_ref
         , is13_periode
         , is13_loenn
         from is_inntekt_13
         where f_nr = ?               -- 1
         and is10_arbufoer_seq = ?    -- 2
                """
            session.run(
                queryOf(statement, fnr.formatAsITFnr(), seq).map { rs ->
                    InntektDTO(
                        rs.string("is13_arbgivnr"),
                        rs.intToLocalDate("is13_spfom"),
                        rs.intOrNullToLocalDate("is13_ref_tom"),
                        rs.string("is13_ref"),
                        rs.string("is13_periode"),
                        rs.double("is13_loenn")
                    )
                }.asList
            )
        }
    }

    internal data class InntektDTO(
        var orgNr: String,
        var sykepengerFom: LocalDate,
        var refusjonTom: LocalDate?,
        var refusjonsType: String,
        var periode: String,
        var loenn: Double
    ) {
        internal companion object {
            internal fun tilInntektsopplysninger(inntekter: List<InntektDTO>) = inntekter
                .filter {
                    when (val periodeKode = it.periode) {
                        in Inntektsopplysninger.gyldigePeriodeKoder -> true
                        else -> {
                            log.warn("Ukjent periodetype i respons fra Infotrygd: $periodeKode")
                            tjenestekallLog.warn("Ukjent periodetype i respons fra Infotrygd: $periodeKode")
                            false
                        }
                    }
                }
                .filter { Inntektsopplysninger.PeriodeKode.verdiFraKode(it.periode) != Premiegrunnlag }
                .map {
                    Inntektsopplysninger(
                        it.sykepengerFom,
                        Inntektsopplysninger.PeriodeKode.verdiFraKode(it.periode).omregn(it.loenn),
                        it.orgNr,
                        it.refusjonTom,
                        "J" == it.refusjonsType
                    )
                }
        }

        private fun String.toPeriode() =
            when (this) {
                "D" -> "Daglig"
                "U" -> "Ukentlig"
                "F" -> "Hver 14. dag"
                "M" -> "Månedlig"
                "Å" -> "Årlig"
                "X" -> "Inntekt fastsatt etter 25% avvik"
                "Y" -> "Premiegrunnlag oppdragstaker (gjelder 2 først ukene)"
                else -> "Ukjent.."
            }
    }
}
