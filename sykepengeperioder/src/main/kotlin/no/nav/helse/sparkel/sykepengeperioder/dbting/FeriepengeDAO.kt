package no.nav.helse.sparkel.sykepengeperioder.dbting

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.sykepengeperioder.Fnr
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingshistorikk
import org.intellij.lang.annotations.Language
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

internal class FeriepengeDAO(
    private val dataSource: DataSource
) {
    internal companion object {
        private val log = LoggerFactory.getLogger(FeriepengeDAO::class.java)
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun feriepenger(fnr: Fnr, fom: LocalDate, tom: LocalDate): List<FeriepengeDTO> {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                SELECT dy.BELOP,
                    dy.FOM,
                    dy.TOM,
                    (SELECT DISTINCT ORGNR FROM T_DELYTELSE_SP_FA_BS WHERE VEDTAK_ID = dy.VEDTAK_ID) AS ORGNR
                FROM T_LOPENR_FNR fnr
                    INNER JOIN T_STONAD st ON st.PERSON_LOPENR = fnr.PERSON_LOPENR
                    INNER JOIN T_VEDTAK v ON v.STONAD_ID = st.STONAD_ID
                    INNER JOIN T_DELYTELSE dy ON dy.VEDTAK_ID = v.VEDTAK_ID
                WHERE fnr.PERSONNR = ? 
                    AND st.KODE_RUTINE = 'FY'
                    AND dy.TYPE_DELYTELSE = 'FY'
                    AND dy.TYPE_SATS = 'E'
                    AND dy.FOM between ? and ?
            """
            session.run(
                queryOf(statement, fnr.toString(), fom.plusYears(1), tom.plusYears(1)).map { rs ->
                    FeriepengeDTO(
                        orgnummer = rs.string("ORGNR"),
                        beløp = rs.double("BELOP"),
                        fom = rs.localDate("FOM"),
                        tom = rs.localDate("TOM")
                    )
                }.asList
            )
        }
    }

    internal fun feriepengerSkalBeregnesManuelt(fnr: Fnr, fom: LocalDate, tom: LocalDate): Boolean {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                SELECT count(*) as count
                FROM IP_MERKNAD_20 
                WHERE F_NR = ? AND IP20_MERKNADS_KODE = '242' AND IP20_MERKNAD_DATO1 >= ? AND IP20_MERKNAD_DATO1 <= ?
            """
            session.run(
                queryOf(
                    statement,
                    fnr.formatAsITFnr(),
                    fom.plusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt(),
                    tom.plusYears(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
                ).map { rs -> rs.int("count") > 0 }.asSingle
            )?: false
        }
    }

    internal data class FeriepengeDTO(
        var orgnummer: String,
        var beløp: Double,
        var fom: LocalDate,
        var tom: LocalDate
    ) {
        internal companion object {
            internal fun tilFeriepenger(feriepenger: List<FeriepengeDTO>) =
                feriepenger.map { it.tilFeriepenger() }
        }

        private fun tilFeriepenger() = Utbetalingshistorikk.Feriepenger(
            orgnummer = orgnummer,
            beløp = beløp,
            fom = fom,
            tom = tom
        )
    }


}
