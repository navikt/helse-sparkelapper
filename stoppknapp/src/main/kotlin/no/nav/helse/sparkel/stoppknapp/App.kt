package no.nav.helse.sparkel.stoppknapp

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication

internal fun main() {
    val app = createApp(System.getenv())
    app.start()
}

private fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        StoppknappRiver(rapidsConnection = this)
    }
}
