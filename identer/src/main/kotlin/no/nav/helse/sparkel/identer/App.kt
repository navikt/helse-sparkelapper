package no.nav.helse.sparkel.identer

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory

val PDL_AKTØR_TOPIC = "aapen-person-pdl-aktor-v1"

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf(PDL_AKTØR_TOPIC))

    return RapidApplication.create(env).apply {
        sikkerlogg.info("gcp-srvsparkelidenter-credentials=${System.getenv("gcp-srvsparkelidenter-credentials")}")
        sikkerlogg.info("srvsparkelidenter-username=${System.getenv("srvsparkelidenter-username")}")
        val aktørConsumer = AktørConsumer(this, kafkaConsumer, IdenthendelseHandler())
        Thread(aktørConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                aktørConsumer.close()
            }
        })
    }
}

