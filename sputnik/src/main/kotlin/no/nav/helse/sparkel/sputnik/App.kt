package no.nav.helse.sparkel.sputnik

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import java.net.URI
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.abakus.RestAbakusClient

fun main() {
    val env = System.getenv()
    val abakusClient = RestAbakusClient(
        url = URI(env.getValue("ABAKUS_URL")),
        scope = env.getValue("ABAKUS_SCOPE"),
        accessTokenClient = createAzureTokenClientFromEnvironment(env)
    )
    startRapidsApplication(env, abakusClient)
}

internal fun startRapidsApplication(env: Map<String, String>, abakusClient: AbakusClient) =
    RapidApplication.create(env).apply {
        Sputnik(this, abakusClient)
    }.start()