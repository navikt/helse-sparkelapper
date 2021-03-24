package no.nav.helse.sparkel.medlemskap

import no.nav.helse.rapids_rivers.RapidApplication
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Paths

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val client = MedlemskapClient("http://medlemskap-oppslag.medlemskap.svc.nais.local", AzureClient(
            tokenEndpoint = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET")
        ), env.getValue("MEDLEMSKAP_SCOPE"))
        Medlemskap(this, client)
    }.start()
}


