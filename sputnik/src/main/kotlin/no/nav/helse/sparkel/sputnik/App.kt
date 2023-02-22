package no.nav.helse.sparkel.sputnik

import java.net.URL
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.abakus.AbakusClient
import no.nav.helse.sparkel.abakus.ClientSecretPost

fun main() {
    val env = System.getenv()
    val abakusClient = AbakusClient(
        url = URL(env.getValue("ABAKUS_URL")),
        accessTokenClient = ClientSecretPost(
            tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
            scope = env.getValue("ABAKUS_SCOPE")
        )
    )
    startRapidsApplication(Abakusløser(abakusClient))
}

internal fun startRapidsApplication(foreldrepengerløser: Foreldrepengerløser) =
    RapidApplication.create(System.getenv()).apply {
        Foreldrepenger(this, foreldrepengerløser)
    }.start()