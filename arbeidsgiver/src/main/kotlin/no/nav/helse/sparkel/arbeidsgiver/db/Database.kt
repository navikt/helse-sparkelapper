package no.nav.helse.sparkel.arbeidsgiver.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val DB_URL = "jdbc:postgresql://%s:%s/%s".format(Environment.Database.host, Environment.Database.port, Environment.Database.name)
object Environment {
    object Database {
        private val env = System.getenv()

        val host: String = requireNotNull(env["DATABASE_HOST"]) { "host må settes" }
        val port: String = requireNotNull(env["DATABASE_PORT"]) { "port må settes" }
        val name: String = requireNotNull(env["DATABASE_DATABASE"]) { "databasenavn må settes" }
        val username: String = requireNotNull(env["DATABASE_USERNAME"]) { "brukernavn må settes" }
        val password: String = requireNotNull(env["DATABASE_PASSWORD"]) { "passord må settes" }
    }
}
class Database(dbConfig: HikariConfig = dbConfig()) {
    val dataSource by lazy { HikariDataSource(dbConfig) }
    val db by lazy { Database.connect(dataSource) }

    fun migrate() {
        migrationConfig()
            .let(::HikariDataSource)
            .also {
                Flyway.configure()
                    .dataSource(it)
                    .lockRetryCount(-1)
                    .load()
                    .migrate()
            }
            .close()
    }
}

private fun dbConfig(): HikariConfig =
    HikariConfig().apply {
        jdbcUrl = DB_URL
        username = Environment.Database.username
        password = Environment.Database.password
        maximumPoolSize = 1
        connectionTimeout = 30.seconds.toMillis()
        initializationFailTimeout = 1.minutes.toMillis()
        idleTimeout = 1.minutes.toMillis()
        maxLifetime = idleTimeout * 5
    }

private fun migrationConfig(): HikariConfig =
    HikariConfig().apply {
        jdbcUrl = DB_URL
        username = Environment.Database.username
        password = Environment.Database.username
        maximumPoolSize = 2
        connectionTimeout = 1.minutes.toMillis()
        initializationFailTimeout = 1.minutes.toMillis()
    }

private fun Duration.toMillis(): Long =
    toJavaDuration().toMillis()
