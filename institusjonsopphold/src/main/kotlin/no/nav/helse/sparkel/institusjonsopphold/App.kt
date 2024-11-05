package no.nav.helse.sparkel.institusjonsopphold

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.io.File
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = createAzureTokenClientFromEnvironment(env)
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
