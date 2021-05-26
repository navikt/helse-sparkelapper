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
                DELETE FROM T_LOPENR_FNR;
                DELETE FROM T_STONAD;
                DELETE FROM T_VEDTAK;
                DELETE FROM T_DELYTELSE;
                DELETE FROM T_DELYTELSE_SP_FA_BS;
                DELETE FROM IP_MERKNAD_20
            """
                ).asExecute
            )
        }
    }

    protected class Utbetaling(
        val fom: LocalDate? = null,
        val tom: LocalDate? = null,
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
        sykmeldtFom: LocalDate = utbetalinger.mapNotNull { it.fom }.minOrNull() ?: 1.januar(2020),
        sykmeldtTom: LocalDate = utbetalinger.mapNotNull { it.tom }.maxOrNull() ?: 31.januar(2020),
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
        fom: LocalDate?,
        tom: LocalDate?,
        grad: String,
        utbetalt: LocalDate?,
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

    private fun insertFødselsnummerLøpenummer(fnr: Fnr, løpenummer: Double) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """INSERT INTO T_LOPENR_FNR(PERSONNR, PERSON_LOPENR) VALUES (:fnr, :fnr_lopenr)"""
            session.run(queryOf(query, mapOf("fnr" to fnr.toString(), "fnr_lopenr" to løpenummer)).asUpdate)
        }
    }

    private fun insertStønad(løpenummer: Double, datoStart: LocalDate, stønadId: Double) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """INSERT INTO T_STONAD(STONAD_ID, PERSON_LOPENR, KODE_RUTINE, DATO_START) 
                VALUES (:stonad_id, :lopenr, 'SR', :datoStart)
            """
            session.run(
                queryOf(
                    query, mapOf("lopenr" to løpenummer, "datoStart" to datoStart, "stonad_id" to stønadId)
                ).asUpdate
            )
        }
    }

    private fun insertVedtak(løpenummer: Double, stønadId: Double, vedtakId: Double, fom: LocalDate, tom: LocalDate) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """INSERT INTO T_VEDTAK( 
                VEDTAK_ID,
                PERSON_LOPENR,
                DATO_INNV_FOM,
                DATO_INNV_TOM,
                STONAD_ID,
-- Andre påkrevde
                KODE_RUTINE,
                DATO_START,
                TKNR,
                SAKSBLOKK,
                SAKSNR,
                TYPE_SAK,
                KODE_RESULTAT,
                DATO_MOTTATT_SAK,
                KODE_VEDTAKSNIVAA,
                TYPE_BEREGNING,
                TKNR_BEH,
                BRUKERID
                ) 
                VALUES (:vedtak_id, :lopenr, :fom, :tom, :stonad_id,
                    -- Andre påkrevde
                    'FY', '2021-05-01', '4514', 'X', 1234, 'XX', 'XX', '2021-05-01', '999', '999', '0000', '12345678'
                )
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fom" to fom,
                        "tom" to tom,
                        "lopenr" to løpenummer,
                        "vedtak_id" to vedtakId,
                        "stonad_id" to stønadId
                    )
                ).asUpdate
            )
        }
    }

    private fun insertDelytelse(
        vedtakId: Double,
        fom: LocalDate,
        tom: LocalDate,
        beløp: List<Double>,
        orgnumre: List<String>
    ) {
        sessionOf(dataSource).use { session ->
            beløp.forEach { sum ->
                @Language("Oracle")
                val query1 = """INSERT INTO T_DELYTELSE(
                VEDTAK_ID, 
                TYPE_DELYTELSE, 
                FOM, 
                TOM, 
                BELOP, 
                TYPE_SATS,
-- Andre påkrevde
                BRUKERID,
                TYPE_UTBETALING
                ) 
                VALUES (:vedtak_id, 'FY', :fom, :tom, :belop, 'E',
                    -- Andre påkrevde
                    '12345678', 'E'
                )
            """
                session.run(
                    queryOf(
                        query1,
                        mapOf("fom" to fom, "tom" to tom, "belop" to sum, "vedtak_id" to vedtakId)
                    ).asUpdate,
                )
            }

            orgnumre.forEach { orgnr ->
                @Language("Oracle")
                val query2 = """INSERT INTO T_DELYTELSE_SP_FA_BS(
                VEDTAK_ID, 
                ORGNR,
-- Andre påkrevde
                TYPE_DELYTELSE,
                TYPE_INNTEKT,
                TYPE_TILTAK,
                TYPE_FORSIKRING,
                PERIODE_KARENS,
                PROSENT_INNT_GRL,
                REFUSJON,
                GRAD,
                KODE_KLASSE,
                SATSENDRING,
                SJOMANN,
                TYPE_SATS,
                SATS_DAGER,
                BRUKERID
                ) 
                VALUES (:vedtak_id, :orgnr,
                    -- Andre påkrevde
                    'FY', 'XX', 'XX', 'X', 'X', 1, 'X', 100, 'KULKLASSE', 'X', 'X', 'XXXX', 1234, '12345678'      
                )
            """
                session.run(
                    queryOf(query2, mapOf("vedtak_id" to vedtakId, "orgnr" to orgnr)).asUpdate,
                )
            }
        }
    }

    protected fun opprettManuellFeriepengeberegningMerknad(
        fnr: Fnr = Companion.fnr,
        kode: String = "242",
        merknadsdato: LocalDate = 31.desember(2021),
        id: Int = 1
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """INSERT INTO IP_MERKNAD_20(
                          F_NR,
                          IP20_MERKNADS_KODE,
                          IP20_MERKNAD_DATO1,
                          IP01_PERSNKEY,
                          IP01_PERSONKEY,
                          IP20_MERKNAD_DATO2,
                          IP20_MERKNAD_BELOP,
                          IP20_BRUKERIDENT,
                          IP20_REG_DATO,
                          TK_NR,
                          ID_MERKN
                          )
                          VALUES (:fnr, :merknad_kode, :merknad_dato,
                                -- Andre påkrevde
                                1111111, 111111111145680, :merknad_dato, 1000.00, 'X', 'X', 'X', :id
                          )  
            """
            session.run(
                queryOf(
                    query, mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "merknad_kode" to kode,
                        "merknad_dato" to merknadsdato.format(),
                        "id" to id
                    )
                ).asUpdate
            )
        }
    }


    protected fun opprettFeriepenger(
        fnr: Fnr = Companion.fnr,
        fom: LocalDate = 1.mai(2020),
        tom: LocalDate = 31.mai(2020),
        beløp: List<Double> = listOf(1000.0),
        orgnumre: List<String> = listOf(Companion.orgnummer)
    ) {
        val løpenummer = (10000..99999).random().toDouble()
        val stønadId = (10000..99999).random().toDouble()
        val vedtakId = (10000..99999).random().toDouble()
        insertFødselsnummerLøpenummer(fnr, løpenummer)
        insertStønad(løpenummer, fom, stønadId)
        insertVedtak(løpenummer, stønadId, vedtakId, fom, tom)
        insertDelytelse(vedtakId, fom, tom, beløp, orgnumre)
    }
}

private fun LocalDate?.format() = this?.let { format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt() } ?: 0
