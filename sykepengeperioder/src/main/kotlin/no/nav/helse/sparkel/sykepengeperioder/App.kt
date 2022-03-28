package no.nav.helse.sparkel.sykepengeperioder

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.sykepengeperioder.DatabaseConfig.Companion.databaseConfig
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import no.nav.helse.sparkel.sykepengeperioder.dbting.FeriepengeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val databaseConfig = env.databaseConfig()

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = databaseConfig.jdbcUrl
        username = databaseConfig.username
        password = databaseConfig.password
        schema = databaseConfig.schema
    })

    val infotrygdService = InfotrygdService(
        PeriodeDAO(dataSource),
        UtbetalingDAO(dataSource),
        InntektDAO(dataSource),
        StatslønnDAO(dataSource),
        FeriepengeDAO(dataSource)
    )

    return RapidApplication.create(env).apply {
        Sykepengehistorikkløser(this, infotrygdService)
        Utbetalingsperiodeløser(this, infotrygdService)
        SykepengehistorikkForFeriepengerløser(this, infotrygdService)
    }
}
