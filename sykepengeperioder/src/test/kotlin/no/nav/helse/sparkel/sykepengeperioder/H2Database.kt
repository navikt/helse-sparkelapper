package no.nav.helse.sparkel.sykepengeperioder

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import javax.sql.DataSource

abstract class H2Database {
    private val hikariConfig = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:test1;MODE=Oracle;DB_CLOSE_DELAY=-1"
        username = "sa"
        password = "sa"
    }
    protected val dataSource: DataSource = HikariDataSource(hikariConfig).apply {
        flyway()
    }

    private fun DataSource.flyway() {
        Flyway.configure()
            .dataSource(this)
            .load()
            .migrate()
    }
}
