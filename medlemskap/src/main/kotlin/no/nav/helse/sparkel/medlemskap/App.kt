package no.nav.helse.sparkel.medlemskap

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import java.net.URI
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val client = MedlemskapClient(
            baseUrl = URI(env.getValue("MEDLEMSKAP_BASE_URL")),
            azureClient = createAzureTokenClientFromEnvironment(env),
            scope = env.getValue("MEDLEMSKAP_SCOPE")
        )
        Medlemskap(this, client)
    }.start()
}