package no.nav.helse.sparkel.sykepengeperioder

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import no.nav.helse.sparkel.sykepengeperioder.dbting.FeriepengeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = env.getValue("INFOTRYGDSP_URL")
        username = env.getValue("INFOTRYGDSP_USERNAME")
        password = env.getValue("INFOTRYGDSP_PASSWORD")
        schema = env.getValue("INFOTRYGDSP_SCHEMA")
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
    }
}
