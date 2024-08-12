package no.nav.helse.sparkel.representasjon

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) { jackson() }
        }

        val azureClient = createAzureTokenClientFromEnvironment(env)

        val representasjonClient = RepresentasjonClient(
            baseUrl = env.getValue("PDL_FULLMAKT_URL"),
            tokenClient = azureClient,
            httpClient = httpClient,
            scope = env.getValue("PDL_FULLMAKT_SCOPE")
        )

        RepresentasjonRiver(rapidsConnection = this, representasjonClient = representasjonClient)
    }
}
