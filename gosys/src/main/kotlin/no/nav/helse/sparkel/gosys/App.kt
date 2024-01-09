package no.nav.helse.sparkel.gosys

import com.github.navikt.tbd_libs.azure.AzureAuthMethod
import com.github.navikt.tbd_libs.azure.AzureTokenClient
import com.github.navikt.tbd_libs.azure.InMemoryAzureTokenCache
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import java.io.File
import java.net.URI

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val stsClient = if (env.containsKey("STS_BASE_URL"))
        StsRestClient(
            baseUrl = env.getValue("STS_BASE_URL"),
            serviceUser = "/var/run/secrets/nais.io/service_user".let { ServiceUser("$it/username".readFile(), "$it/password".readFile()) }
        )
    else null
    val azureClient = if (env.containsKey("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"))
        InMemoryAzureTokenCache(AzureTokenClient(
            tokenEndpoint = URI(env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            authMethod = AzureAuthMethod.Secret(env.getValue("AZURE_APP_CLIENT_SECRET"))
        )) else null
    val oppgaveClient = OppgaveClient(
        baseUrl = env.getValue("OPPGAVE_URL"),
        scope = env["OPPGAVE_SCOPE"],
        stsClient = stsClient,
        azureClient = azureClient
    )
    val oppgaveService = OppgaveService(oppgaveClient)

    return RapidApplication.create(env).apply {
        Oppgaveløser(this, oppgaveService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
