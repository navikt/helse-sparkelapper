package no.nav.helse.sparkel.stoppknapp.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import org.flywaydb.core.Flyway
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

internal abstract class AbstractDatabaseTest(private val doTruncate: Boolean = true) {
    internal companion object {
        private val port: String
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:15").apply {
                withReuse(true)
                withLabel("app-navn", "sparkel-stoppknapp")
                start()
                port = firstMappedPort.toString()
                println("Database: jdbc:postgresql://localhost:$firstMappedPort/test startet opp, credentials: test og test")
            }

        internal val dataSource =
            HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = postgres.username
                    password = postgres.password
                    maximumPoolSize = 5
                    minimumIdle = 1
                    idleTimeout = 500001
                    connectionTimeout = 10000
                    maxLifetime = 600001
                    initializationFailTimeout = 5000
                },
            )

        init {
            Flyway.configure()
                .dataSource(dataSource)
                .failOnMissingLocations(true)
                .cleanDisabled(false)
                .load()
                .also { it.clean() }
                .migrate()

            createTruncateFunction(dataSource)
        }
    }

    @BeforeEach
    fun resetDatabase() {
        if (doTruncate) {
            sessionOf(dataSource).use {
                it.run(queryOf("SELECT truncate_tables()").asExecute)
            }
        }
    }
}

private fun createTruncateFunction(dataSource: DataSource) {
    sessionOf(dataSource).use {
        @Language("PostgreSQL")
        val query = """
            CREATE OR REPLACE FUNCTION truncate_tables() RETURNS void AS $$
            DECLARE
            truncate_statement text;
            BEGIN
                SELECT 'TRUNCATE ' || string_agg(format('%I.%I', schemaname, tablename), ',') || ' RESTART IDENTITY CASCADE' 
                    INTO truncate_statement
                FROM pg_tables
                WHERE schemaname='public';

                EXECUTE truncate_statement;
            END;
            $$ LANGUAGE plpgsql;
        """
        it.run(queryOf(query).asExecute)
    }
}
