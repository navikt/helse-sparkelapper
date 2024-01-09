package no.nav.helse.sparkel.institusjonsopphold

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
    val azureClient = InMemoryAzureTokenCache(AzureTokenClient(
        tokenEndpoint = URI(env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
        clientId = env.getValue("AZURE_APP_CLIENT_ID"),
        authMethod = AzureAuthMethod.Secret(env.getValue("AZURE_APP_CLIENT_SECRET"))
    ))
    val institusjonsoppholdClient = InstitusjonsoppholdClient(
        baseUrl = env.getValue("INSTITUSJONSOPPHOLD_URL"),
        scope = env.getValue("INSTITUSJONSOPPHOLD_SCOPE"),
        azureClient = azureClient
    )
    val institusjonsoppholdService = InstitusjonsoppholdService(institusjonsoppholdClient)

    return RapidApplication.create(env).apply {
        Institusjonsoppholdløser(this, institusjonsoppholdService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
