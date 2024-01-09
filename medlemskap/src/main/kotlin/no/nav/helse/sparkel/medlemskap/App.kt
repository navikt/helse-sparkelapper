package no.nav.helse.sparkel.medlemskap

import com.github.navikt.tbd_libs.azure.AzureAuthMethod
import com.github.navikt.tbd_libs.azure.AzureTokenClient
import com.github.navikt.tbd_libs.azure.InMemoryAzureTokenCache
import java.net.URI
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val client = MedlemskapClient(
            baseUrl = URI(env.getValue("MEDLEMSKAP_BASE_URL")),
            azureClient = InMemoryAzureTokenCache(AzureTokenClient(
                tokenEndpoint = URI(env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT")),
                clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                authMethod = AzureAuthMethod.Secret(env.getValue("AZURE_APP_CLIENT_SECRET"))
            )),
            scope = env.getValue("MEDLEMSKAP_SCOPE")
        )
        Medlemskap(this, client)
    }.start()
}