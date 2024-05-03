package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        StoppknappRiver(rapidsConnection = this)
    }
}
