package no.nav.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.forsikring.DatabaseConfig.Companion.databaseConfig
import org.slf4j.LoggerFactory

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val databaseConfig = env.databaseConfig()

    val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    sikkerlogg.info("Database url: ${databaseConfig.jdbcUrl}")

    return RapidApplication.create(env).apply {

        val dataSource by lazy {
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = databaseConfig.jdbcUrl
                username = databaseConfig.username
                password = databaseConfig.password
                schema = databaseConfig.schema
                connectionTimeout = Duration.ofSeconds(20).toMillis()
                maxLifetime = Duration.ofMinutes(30).toMillis()
                initializationFailTimeout = Duration.ofMinutes(1).toMillis()
            })
        }

        val forsikringDao = ForsikringDao { dataSource }

        Forsikringsløser(this, forsikringDao)
    }
}

