package no.nav.helse.sparkel.skjermetendret

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication

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
