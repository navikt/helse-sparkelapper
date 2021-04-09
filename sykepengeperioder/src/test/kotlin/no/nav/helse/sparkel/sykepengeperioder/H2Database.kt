package no.nav.helse.sparkel.sykepengeperioder

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

internal abstract class H2Database {
    protected companion object {
        internal val fnr = Fnr("14123456789")
        internal const val orgnummer = "80000000"
    }

    protected val dataSource: DataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:test1;MODE=Oracle;DB_CLOSE_DELAY=-1"
        username = "sa"
        password = "sa"
    }).apply {
        flyway()
    }

    private fun DataSource.flyway() {
        Flyway.configure()
            .dataSource(this)
            .load()
            .migrate()
    }

    protected fun clear() {
        sessionOf(dataSource).use {
            it.run(
                queryOf(
                    """
                DELETE FROM IS_PERIODE_10;    
                DELETE FROM IS_UTBETALING_15;    
                DELETE FROM IS_INNTEKT_13;    
                DELETE FROM IS_DIVERSE_11;    
            """
                ).asExecute
            )
        }
    }

    protected class Utbetaling(
        val fom: LocalDate,
        val tom: LocalDate,
        val grad: String = "100",
        val dagsats: Double = 1000.0,
        val orgnummer: String = Companion.orgnummer
    )

    protected class Inntekt(
        val fom: LocalDate,
        val lønn: Double = 565700.0,
        val orgnummer: String = Companion.orgnummer
    )

    protected fun opprettPeriode(
        fnr: Fnr = Companion.fnr,
        seq: Int = 1,
        maksdato: LocalDate? = null,
        utbetalinger: List<Utbetaling> = emptyList(),
        sykmeldtFom: LocalDate = utbetalinger.minOfOrNull { it.fom } ?: 1.januar(2020),
        sykmeldtTom: LocalDate = utbetalinger.maxOfOrNull { it.tom } ?: 31.januar(2020),
        inntekter: List<Inntekt> = emptyList(),
        statslønn: Double? = null,
        arbeidskategori: String = "01"
    ) {
        insertPeriode(
            fnr = fnr,
            seq = seq,
            fom = sykmeldtFom,
            tom = sykmeldtTom,
            maksdato = maksdato,
            arbeidskategori = arbeidskategori
        )
        utbetalinger.forEach { utbetaling ->
            insertUtbetaling(
                fnr = fnr,
                seq = seq,
                fom = utbetaling.fom,
                tom = utbetaling.tom,
                grad = utbetaling.grad,
                utbetalt = utbetaling.tom,
                dagsats = utbetaling.dagsats,
                periodetype = "5",
                arbOrgnr = utbetaling.orgnummer
            )
        }
        inntekter.forEach { inntekt ->
            insertInntekt(
                fnr = fnr,
                seq = seq,
                orgNr = inntekt.orgnummer,
                sykepengerFom = inntekt.fom,
                refusjonTom = null,
                refusjonstype = "J",
                periode = "Å",
                loenn = inntekt.lønn
            )
        }
        if (statslønn != null)
            insertStatslønn(
                fnr = fnr,
                seq = seq,
                statslønn = statslønn
            )
    }

    private var id: Int = 0
        get() = field++

    private fun insertPeriode(
        fnr: Fnr,
        seq: Int,
        fom: LocalDate,
        tom: LocalDate,
        maksdato: LocalDate?,
        arbeidskategori: String,
        ferie1Fom: LocalDate? = null,
        ferie1Tom: LocalDate? = null,
        ferie2Fom: LocalDate? = null,
        ferie2Tom: LocalDate? = null
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IS_PERIODE_10 (IS01_PERSONKEY,
                           TK_NR,
                           IS10_ARBUFOER_SEQ,
                           IS10_ARBUFOER,
                           IS10_ARBUFOER_TOM,
                           IS10_UFOEREGRAD,
                           IS10_MAX,
                           IS10_ARBPER,
                           IS10_FERIE_FOM,
                           IS10_FERIE_TOM,
                           IS10_FERIE_FOM2,
                           IS10_FERIE_TOM2,
                           IS10_STANS,
                           IS10_UNNTAK_AKTIVITET,
                           IS10_ARBKAT,
                           IS10_ARBKAT_99,
                           IS10_SANKSJON_FOM,
                           IS10_SANKSJON_TOM,
                           IS10_SANKSJON_BEKREFTET,
                           IS10_SANKSJONSDAGER,
                           IS10_STOPPDATO,
                           IS10_LEGENAVN,
                           IS10_BEHDATO,
                           IS10_SKADEART,
                           IS10_SKDATO,
                           IS10_SKM_MOTT,
                           F_NR,
                           IS10_FRISK,
-- What??
                           IS10_STOENADS_TYPE,
--Uinteressante påkrevde
                           IS10_STAT,
                           IS10_ARBUFOER_OPPR,
--ID
                           ID_PERI10)
VALUES (111111111145680, 1111, :seq, :fom, :tom, 100, :slutt, 'j', :ferie1Fom, :ferie1Tom, :ferie2Fom, :ferie2Tom, '', 'M', :arbeidskategori,
        '', null, null, '', 0, null, 'TROND-VIGGO TORGERSEN', :fom, '', null, null, :fnr, null,
-- What??
        '  ',
--Uinteressante påkrevde
        '?', -1,
--ID
        :id);
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "seq" to seq,
                        "fom" to fom.format(),
                        "tom" to tom.format(),
                        "slutt" to maksdato?.format(),
                        "arbeidskategori" to arbeidskategori,
                        "ferie1Fom" to ferie1Fom?.format(),
                        "ferie1Tom" to ferie1Tom?.format(),
                        "ferie2Fom" to ferie2Fom?.format(),
                        "ferie2Tom" to ferie2Tom?.format(),
                        "id" to id
                    )
                ).asUpdate
            )
        }
    }

    private fun insertUtbetaling(
        fnr: Fnr,
        seq: Int,
        fom: LocalDate,
        tom: LocalDate,
        grad: String,
        utbetalt: LocalDate,
        dagsats: Double,
        periodetype: String,
        arbOrgnr: String,
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IS_UTBETALING_15 (IS15_UTBETFOM,
                              IS15_UTBETTOM,
                              IS15_GRAD,
                              IS15_OP,
                              IS15_UTBETDATO,
                              IS15_DSATS,
                              IS15_TYPE,
                              IS15_ARBGIVNR,
                              F_NR,
                              IS10_ARBUFOER_SEQ,
                              IS15_KORR,
                              ID_UTBT,
-- Andre påkrevde
                              IS01_PERSONKEY,
                              IS15_UTBETFOM_SEQ,
                              IS15_BILAG,
                              IS15_TILB_UTBETDATO,
                              IS15_TILB_BILAG,
                              IS15_TILB_OP,
                              IS15_TIDSKONTO_KODE,
                              IS15_BRUKERID,
                              IS15_REGDATO_BATCH,
                              IS15_TILTAK_TYPE,
                              IS15_AARSAK_FORSKYV,
                              IS15_BEREGNET_I_OS,
                              TK_NR,
                              KILDE_IS,
                              REGION)
VALUES (:fom, :tom, :grad, ' ', :utbetalt, :dagsats, :periodetype, :arbOrgnr, :fnr, :seq, 'NOTK', :id,
-- Andre påkrevde
 111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e');
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "seq" to seq,
                        "fom" to fom.format(),
                        "tom" to tom.format(),
                        "grad" to grad,
                        "utbetalt" to utbetalt.format(),
                        "dagsats" to dagsats,
                        "periodetype" to periodetype,
                        "arbOrgnr" to arbOrgnr.toInt(),
                        "id" to id
                    )
                ).asUpdate
            )
        }
    }

    private fun insertInntekt(
        fnr: Fnr,
        seq: Int,
        orgNr: String,
        sykepengerFom: LocalDate,
        refusjonTom: LocalDate?,
        refusjonstype: String,
        periode: String,
        loenn: Double
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IS_INNTEKT_13(IS13_ARBGIVNR,
                          IS13_SPFOM,
                          IS13_REF_TOM,
                          IS13_REF,
                          IS13_PERIODE,
                          IS13_LOENN,
                          F_NR,
                          IS10_ARBUFOER_SEQ,
                          ID_INNT,
-- Andre påkrevde
                          IS01_PERSONKEY,
                          IS13_GML_SPFOM,
                          IS13_GML_LOENN,
                          IS13_GML_PERIODE,
                          IS13_PO_INNT,
                          IS13_UTBET,
                          IS13_TIDSKONTO_KODE,
                          TK_NR,
                          KILDE_IS,
                          REGION)
VALUES (:orgNr, :sykepengerFom, :refusjonTom, :refusjonstype, :periode, :loenn, :fnr, :seq, :id,
           -- Andre påkrevde
        111111111145680, 1, 1, 'e', 'e', 'e', 'e', 'tknr', 'ee', 'e');
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "seq" to seq,
                        "orgNr" to orgNr,
                        "sykepengerFom" to sykepengerFom.format(),
                        "refusjonTom" to (refusjonTom?.format() ?: 0),
                        "refusjonstype" to refusjonstype,
                        "periode" to periode,
                        "loenn" to loenn,
                        "id" to id
                    )
                ).asUpdate
            )
        }
    }

    private fun insertStatslønn(
        fnr: Fnr,
        seq: Int,
        statslønn: Double
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IS_DIVERSE_11(F_NR,
                          IS10_ARBUFOER_SEQ,
                          ID_DIV,
-- Statslønn
                          IS11_STLONN,
-- Andre påkrevde
                          IS01_PERSONKEY,
                          IS11_NATURAL,
                          IS11_UTB_NAT,
                          IS11_OPPH_NAT,
                          IS11_REDUKSJ_BELOEP,
                          IS11_REDUKSJ_TYPE,
                          IS11_REDUKSJ_FOM,
                          IS11_REDUKSJ_TOM,
                          TK_NR)
VALUES (:fnr, :seq, :id, :statslonn,
-- Andre påkrevde
        111111111145680, 0, 'e', 0, 0, 'e', 0, 0, 'tnkr');
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "seq" to seq,
                        "statslonn" to statslønn,
                        "id" to id
                    )
                ).asUpdate
            )
        }
    }
}

private fun LocalDate.format() = format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
