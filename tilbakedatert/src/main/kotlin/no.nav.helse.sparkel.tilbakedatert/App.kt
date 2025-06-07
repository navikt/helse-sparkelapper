package no.nav.helse.sparkel.tilbakedatert

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        NyTilbakedatertRiver(rapidsConnection = this)
    }
}
