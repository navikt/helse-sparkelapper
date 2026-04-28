package no.nav.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.io.File
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        Forsikringsløser(
            rapidsConnection = this,
            forsikringDao = if (env["NAIS_CLUSTER_NAME"] == "dev-fss") {
                MockForsikringDao()
            } else {
                ReplikabaseForsikringDao(
                    dataSource = HikariDataSource(
                        HikariConfig().apply {
                            jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText()
                            username = File("/var/run/secrets/nais.io/oracle/creds/username").readText()
                            password = File("/var/run/secrets/nais.io/oracle/creds/password").readText()
                            schema = env.getValue("DATABASE_SCHEMA")
                            connectionTimeout = Duration.ofSeconds(20).toMillis()
                            maxLifetime = Duration.ofMinutes(30).toMillis()
                            initializationFailTimeout = Duration.ofMinutes(1).toMillis()
                        }
                    )
                )
            }
        )
    }.apply(RapidsConnection::start)
}
