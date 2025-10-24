package no.nav.helse.sparkel.forsikring

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    //val databaseConfig = env.databaseConfig()

    return RapidApplication.create(env).apply {

        // bruker lazy slik at vi ikke kobler oss til datasourcen før den brukes
        // Kommentert ut til vi faktisk har det vi trenger for å koble oss til databasen
        /*val dataSource by lazy {
            HikariDataSource(HikariConfig().apply {
                jdbcUrl = databaseConfig.jdbcUrl
                username = databaseConfig.username
                password = databaseConfig.password
                schema = databaseConfig.schema
                connectionTimeout = Duration.ofSeconds(20).toMillis()
                maxLifetime = Duration.ofMinutes(30).toMillis()
                initializationFailTimeout = Duration.ofMinutes(1).toMillis()
            })
        }*/

        Forsikringsløser(this)
    }
}

