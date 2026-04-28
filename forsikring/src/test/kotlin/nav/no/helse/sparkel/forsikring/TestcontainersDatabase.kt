package nav.no.helse.sparkel.forsikring

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import kotliquery.queryOf
import kotliquery.sessionOf
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

    fun insertVedfrivt(
        agnrFnr: String,
        virkdato: Int,
        forstom: Int,
        godkj: String,
        type: String,
        premgrl: Int
    ) {
        sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val query = """
                INSERT INTO IF_VEDFRIVT_10 (IF01_KODE, IF01_AGNR_FNR, IF10_GODKJ, IF10_TYPE, IF10_VIRKDATO, IF10_FORSTOM, IF10_PREMGRL)
                VALUES (:kode, :agnr_fnr, :godkj, :type, :virkdato, :forstom, :premgrl);
            """
            session.run(
                queryOf(
                    query,
                    mapOf(
                        "kode" to "1",
                        "agnr_fnr" to agnrFnr,
                        "godkj" to godkj,
                        "type" to type,
                        "virkdato" to virkdato,
                        "forstom" to forstom,
                        "premgrl" to premgrl
                    )
                ).asUpdate
            )
        }
    }
}
