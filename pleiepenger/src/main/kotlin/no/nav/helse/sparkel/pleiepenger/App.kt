package no.nav.helse.sparkel.pleiepenger

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.exporter.common.TextFormat
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.infotrygd.AzureClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdClient
import no.nav.helse.sparkel.pleiepenger.k9.AbakusClient
import no.nav.helse.sparkel.pleiepenger.k9.ClientSecretBasic

fun main() {
    if (gcp()) simpleApp()
    else rapidsApp()
}
private fun gcp() = System.getenv("NAIS_CLUSTER_NAME")?.lowercase()?.endsWith("-gcp") == true
private fun simpleApp() {
    val requests = Counter.build("requests", "requests").labelNames("endpoint").register()
    embeddedServer(CIO, port = 8080) {
        routing {
            get("/isalive") {
                requests.labels("isalive").inc()
                call.respondText("ALIVE!") }
            get("/isready") {
                requests.labels("isready").inc()
                call.respondText("READY!") }
            get("/metrics") {
                requests.labels("metrics").inc()
                call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                    TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
                }
            }
        }
    }.start(wait = true)
}
private fun rapidsApp() {
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
    val stsClient = ClientSecretBasic(
        tokenEndpoint = env.getValue("STS_TOKEN_ENDPOINT"),
        clientId = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/username")),
        clientSecret = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/password"))
    )
    val abakusClient = AbakusClient(
        url = URL(env.getValue("ABAKUS_URL")),
        accessTokenClient = stsClient
    )
    return RapidApplication.create(env).apply {
        PleiepengerløserV2(this, infotrygdClient, abakusClient)
        OmsorgspengerløserV2(this, infotrygdClient, abakusClient)
        OpplæringspengerløserV2(this, infotrygdClient)
    }
}
