package no.nav.helse.sparkel.identer

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {

    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf("aapen-person-pdl-aktor-v1"))

    return RapidApplication.create(env).apply {
        val aktørConsumer = AktørConsumer(this, kafkaConsumer)
        Thread(aktørConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                aktørConsumer.close()
            }
        })
    }
}

