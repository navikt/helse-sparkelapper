package no.nav.helse.sparkel.sykepengeperioder

import org.flywaydb.core.Flyway
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

abstract class H2Database {
    protected val dataSource: DataSource = JdbcDataSource().apply {
        setUrl("jdbc:h2:mem:test1;MODE=Oracle;DB_CLOSE_DELAY=-1")
        user = "sa"
        password = "sa"
//        Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9090").start()

        flyway()
    }

    private fun DataSource.flyway() {
        Flyway.configure()
            .dataSource(this)
            .load()
            .migrate()
    }
}
