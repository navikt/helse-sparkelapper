package no.nav.helse.sparkel.sykepengeperioder

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.infotrygd.PeriodeDAO
import no.nav.helse.sparkel.infotrygd.UtbetalingDAO
import no.nav.helse.sparkel.sykepengeperioder.DatabaseConfig.Companion.databaseConfig
import no.nav.helse.sparkel.sykepengeperioder.dbting.FeriepengeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.StatslønnDAO

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val databaseConfig = env.databaseConfig()

    return RapidApplication.create(env).apply {
        // bruker lazy slik at vi ikke kobler oss til datasourcen før den brukes
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

        val infotrygdService = InfotrygdService(
            PeriodeDAO { dataSource },
            UtbetalingDAO { dataSource },
            InntektDAO { dataSource },
            StatslønnDAO { dataSource },
            FeriepengeDAO { dataSource }
        )

        Sykepengehistorikkløser(this, infotrygdService)
        Utbetalingsperiodeløser(this, infotrygdService)
        SykepengehistorikkForFeriepengerløser(this, infotrygdService)
    }
}
