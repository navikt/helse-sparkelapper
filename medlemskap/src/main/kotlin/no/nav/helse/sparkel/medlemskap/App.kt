package no.nav.helse.sparkel.medlemskap

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val client = MedlemskapClient(
            baseUrl = env.getValue("MEDLEMSKAP_BASE_URL"),
            AzureClient(
                tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
                clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
            ),
            env.getValue("MEDLEMSKAP_SCOPE")
        )
        Medlemskap(this, client)
    }.start()
}
