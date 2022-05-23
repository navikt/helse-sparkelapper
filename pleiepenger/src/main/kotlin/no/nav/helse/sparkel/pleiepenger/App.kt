package no.nav.helse.sparkel.pleiepenger

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.infotrygd.AzureClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdService

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = AzureClient(
        tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
    )
    val infotrygdClient = InfotrygdClient(
        baseUrl = env.getValue("INFOTRYGD_URL"),
        accesstokenScope = env.getValue("INFOTRYGD_SCOPE"),
        azureClient = azureClient
    )
    val infotrygdService = InfotrygdService(infotrygdClient)

    return RapidApplication.create(env).apply {
        Pleiepengerløser(this, infotrygdService)
        Omsorgspengerløser(this, infotrygdService)
        Opplæringspengerløser(this, infotrygdService)
    }
}
