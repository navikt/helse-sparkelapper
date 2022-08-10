package no.nav.helse.sparkel.skjermetendret

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val env = System.getenv()
    val kafkaConsumer = createConsumer()
    RapidApplication.create(env).apply {
        val skjermetEndretPubliserer = SkjermetEndretPubliserer(this)
        val skjermetConsumer = SkjermetConsumer(this, kafkaConsumer, skjermetEndretPubliserer)
        Thread(skjermetConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                skjermetConsumer.close()
            }
        })
    }.start()
}
