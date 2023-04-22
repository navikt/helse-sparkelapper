package no.nav.helse.sparkel.sykepengeperioder.dbting

import java.time.LocalDate
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.sykepengeperioder.Fnr
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk.Inntektsopplysninger
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk.Inntektsopplysninger.PeriodeKode.Premiegrunnlag
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory

internal class InntektDAO(
    private val dataSource: DataSource
) {
    internal companion object {
        private val log = LoggerFactory.getLogger(InntektDAO::class.java)
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun inntekter(fnr: Fnr, vararg seq: Int): List<InntektDTO> {
        if (seq.isEmpty()) return emptyList()
        return sessionOf(dataSource).use { session ->
            val antallSpørsmålstegn = seq.joinToString(",") { "?" }

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
         and is10_arbufoer_seq in ($antallSpørsmålstegn)    -- 2
                """
            session.run(
                queryOf(statement, fnr.formatAsITFnr(), *seq.toTypedArray()).map { rs ->
                    InntektDTO(
                        orgNr = rs.string("is13_arbgivnr"),
                        sykepengerFom = rs.intToLocalDate("is13_spfom"),
                        refusjonTom = rs.intOrNullToLocalDate("is13_ref_tom"),
                        refusjonsType = rs.string("is13_ref"),
                        periode = rs.string("is13_periode"),
                        loenn = rs.double("is13_loenn")
                    )
                }.asList
            ).also { loggHvisFlereInntekterForSammeDato(it, fnr) }
        }
    }

    private fun loggHvisFlereInntekterForSammeDato(inntektDTOS: List<InntektDTO>, fnr: Fnr) {
        inntektDTOS.fold(mutableMapOf<LocalDate, Set<Double>>()) { acc, dto ->
            acc.apply { merge(dto.sykepengerFom, setOf(dto.loenn), Set<Double>::plus) }
        }.filter { it.value.size > 1 }
            .forEach { (dato, _) -> tjenestekallLog.info("Mer enn én inntekt for $dato for FNR $fnr: $inntektDTOS") }
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
                        in Inntektsopplysninger.PeriodeKode.gyldigePeriodeKoder -> true
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
                .sortedBy { it.sykepengerFom }
        }
    }
}
