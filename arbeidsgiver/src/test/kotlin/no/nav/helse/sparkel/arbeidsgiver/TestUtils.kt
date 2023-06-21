package no.nav.helse.sparkel.arbeidsgiver

import com.zaxxer.hikari.HikariConfig
import no.nav.helse.sparkel.arbeidsgiver.db.Database
import org.flywaydb.core.Flyway
import org.testcontainers.containers.PostgreSQLContainer

internal fun dbConfig(): HikariConfig {
    val postgres = postgres()
    return HikariConfig().apply {
        jdbcUrl = postgres.jdbcUrl
        username = postgres.username
        password = postgres.password
        maximumPoolSize = 5
        minimumIdle = 1
        idleTimeout = 500001
        connectionTimeout = 10000
        maxLifetime = 600001
        initializationFailTimeout = 5000
    }
}

internal fun postgres(): PostgreSQLContainer<Nothing> =
    PostgreSQLContainer<Nothing>("postgres:14").apply {
        withReuse(true)
        withLabel("app-navn", "im-db")
        start()
        println(
            "🎩 Databasen er startet opp, portnummer: $firstMappedPort, jdbcUrl: jdbc:postgresql://localhost:$firstMappedPort/test, credentials: test og test"
        )
    }

internal fun Database.configureFlyway(): Database =
    also {
        Flyway.configure()
            .dataSource(it.dataSource)
            .failOnMissingLocations(true)
            .cleanDisabled(false)
            .load()
            .also(Flyway::clean)
            .migrate()
    }