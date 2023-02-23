package no.nav.helse.sparkel.sputnik

import java.net.URL
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.abakus.ClientSecretPost
import no.nav.helse.sparkel.sputnik.abakus.RestAbakusClient

fun main() {
    val env = System.getenv()
    val abakusClient = RestAbakusClient(
        url = URL(env.getValue("ABAKUS_URL")),
        accessTokenClient = ClientSecretPost(
            tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
            scope = env.getValue("ABAKUS_SCOPE")
        )
    )
    startRapidsApplication(abakusClient,)
}

internal fun startRapidsApplication(abakusClient: AbakusClient) =
    RapidApplication.create(System.getenv()).apply {
        if (System.getenv("NAIS_CLUSTER_NAME")?.lowercase() == "dev-gcp") {
            Sputnik(this, abakusClient)
        } else {
            Foreldrepenger(this, Abakusløser(abakusClient))
        }
    }.start()