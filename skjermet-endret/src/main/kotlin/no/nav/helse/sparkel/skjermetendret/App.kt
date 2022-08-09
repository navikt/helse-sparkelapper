package no.nav.helse.sparkel.skjermetendret

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

val SKJERMET_TOPIC = "nom.skjermede-personer-status-v1"

fun main() {
    val env = System.getenv()

    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf(SKJERMET_TOPIC))

    RapidApplication.create(env).apply {
        val skjermetConsumer = SkjermetConsumer(this, kafkaConsumer)
        Thread(skjermetConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                skjermetConsumer.close()
            }
        })
    }.start()
}
