package no.nav.helse.sparkel.identer

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.identer.db.DataSourceBuilder
import no.nav.helse.sparkel.identer.db.IdentifikatorDao

val PDL_AKTØR_TOPIC = "aapen-person-pdl-aktor-v1"

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {

    val dataSourceBuilder = DataSourceBuilder(env)
    val dataSource = dataSourceBuilder.getDataSource()
    dataSourceBuilder.migrate()

    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf(PDL_AKTØR_TOPIC))

    return RapidApplication.create(env).apply {
        val aktørConsumer = AktørConsumer(this, kafkaConsumer, IdentifikatorDao(dataSource))
        Thread(aktørConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                dataSource.close()
                aktørConsumer.close()
            }
        })
    }
}

