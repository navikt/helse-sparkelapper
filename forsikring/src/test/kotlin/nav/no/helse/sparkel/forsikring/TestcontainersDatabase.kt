package nav.no.helse.sparkel.forsikring

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.helse.sparkel.forsikring.Fnr
import org.intellij.lang.annotations.Language
import org.testcontainers.oracle.OracleContainer

object TestcontainersDatabase {
    private val oracleContainer: OracleContainer =
        OracleContainer("gvenzl/oracle-free:slim-faststart")
            .also { it.start() }

    private val initScript =
        this::class.java.getResourceAsStream("/testcontainer_db_init.sql")!!.use { it.reader().readText() }

    val dataSource: DataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = oracleContainer.jdbcUrl
                username = oracleContainer.username
                password = oracleContainer.password
                maximumPoolSize = 3
                minimumIdle = 1
            }
        ).also { dataSource ->
            sessionOf(dataSource).use { session ->
                session.run(queryOf(initScript).asExecute)
            }
        }

    fun clear() {
        sessionOf(dataSource).use {
            it.run(queryOf("""DELETE FROM IF_VEDFRIVT_10""").asUpdate)
        }
    }

    fun opprettPeriode(
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
                INSERT INTO IF_VEDFRIVT_10 (IF01_KODE, IF01_AGNR_FNR, IF10_GODKJ, IF10_TYPE, IF10_VIRKDATO, IF10_FORSTOM, IF10_PREMGRL)
                VALUES (:kode, :fnr, :godkjent, :type, :fom, :tom, :premiegrunnlag);
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "fnr" to "${fnr.year()}${fnr.month()}${fnr.date()}${fnr.id()}",
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
