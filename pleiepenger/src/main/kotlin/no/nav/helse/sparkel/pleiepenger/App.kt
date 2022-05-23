package no.nav.helse.sparkel.pleiepenger

import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.infotrygd.AzureClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdService
import no.nav.helse.sparkel.pleiepenger.k9.AbakusClient
import no.nav.helse.sparkel.pleiepenger.k9.ClientSecretBasic

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

    return if (env.getValue("NAIS_CLUSTER_NAME") == "dev-fss") {
        val stsClient = ClientSecretBasic(
            tokenEndpoint = env.getValue("STS_TOKEN_ENDPOINT"),
            clientId = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/username")),
            clientSecret = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/password"))
        )
        val abakusClient = AbakusClient(
            url = URL(env.getValue("ABAKUS_URL")),
            accessTokenClient = stsClient
        )
        RapidApplication.create(env).apply {
            PleiepengerløserV2(this, infotrygdClient, abakusClient)
            OmsorgspengerløserV2(this, infotrygdClient, abakusClient)
            PleiepengerløserV2(this, infotrygdClient, abakusClient)
        }
    } else {
        val infotrygdService = InfotrygdService(
            infotrygdClient = infotrygdClient
        )

        RapidApplication.create(env).apply {
            Pleiepengerløser(this, infotrygdService)
            Omsorgspengerløser(this, infotrygdService)
            Opplæringspengerløser(this, infotrygdService)
        }
    }
}
