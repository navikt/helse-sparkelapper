package nav.no.helse.sparkel.forsikring

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import no.nav.helse.sparkel.forsikring.Fnr
import no.nav.helse.sparkel.forsikring.formatAsITFnr

internal abstract class H2Database {
    protected companion object {
        val fnr = Fnr("14123456789")
        const val orgnummer = "80000000"
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
                DELETE FROM IF_VEDFRIVT_10;    
            """
                ).asExecute
            )
        }
    }


    protected fun opprettPeriode(
        fnr: Fnr = Companion.fnr,
        virkningsdato: LocalDate?,
        tom: LocalDate?,
        godkjent: String,
        forsikringstype: String,
        premiegrunnlag: Int
    ) {
        insertPeriode(
            fnr = fnr,
            virkningsdato = virkningsdato,
            tom = tom,
            godkjent = godkjent,
            forsikringstype = forsikringstype,
            premiegrunnlag = premiegrunnlag
        )
    }

    private var id: Int = 0
        get() = field++

    private fun insertPeriode(
        fnr: Fnr,
        virkningsdato: LocalDate?,
        tom: LocalDate?,
        godkjent: String,
        forsikringstype: String,
        premiegrunnlag: Int

    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IF_VEDFRIVT_10 (
                    IF01_KODE,
                    IF01_AGNR_FNR,
                    IF10_GODKJ,
                    IF10_TYPE,
                    IF10_VIRKDATO,
                    IF10_FORSTOM,
                    IF10_PREMGRL)
VALUES (:kode, :fnr, :godkjent, :type, :fom, :tom, :premiegrunnlag);
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "fom" to virkningsdato.format(),
                        "tom" to tom.format(),
                        "godkjent" to godkjent,
                        "type" to forsikringstype,
                        "kode" to "1",
                        "premiegrunnlag" to premiegrunnlag
                    )
                ).asUpdate
            )
        }
    }
    private fun LocalDate?.format() = this?.let { format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt() } ?: 0

}
