package no.nav.helse.sparkel.gosys

import com.github.navikt.tbd_libs.azure.AzureAuthMethod
import com.github.navikt.tbd_libs.azure.AzureTokenClient
import com.github.navikt.tbd_libs.azure.InMemoryAzureTokenCache
import java.net.URI
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = InMemoryAzureTokenCache(AzureTokenClient(
        tokenEndpoint = URI(env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        authMethod = AzureAuthMethod.Secret(env.getValue("AZURE_APP_CLIENT_SECRET"))
    ))
    val oppgaveClient = OppgaveClient(
        baseUrl = env.getValue("OPPGAVE_URL"),
        scope = env.getValue("OPPGAVE_SCOPE"),
        azureClient = azureClient
    )
    val oppgaveService = OppgaveService(oppgaveClient)

    return RapidApplication.create(env).apply {
        Oppgaveløser(this, oppgaveService)
    }
}
