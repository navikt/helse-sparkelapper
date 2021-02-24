package no.nav.helse.sparkel.personinfo

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import java.io.File

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val stsClient = StsRestClient(
        baseUrl = env.getValue("STS_BASE_URL"),
        serviceUser = "/var/run/secrets/nais.io/service_user".let { ServiceUser("$it/username".readFile(), "$it/password".readFile()) }
    )
    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        stsClient = stsClient
    )
    val personinfoService = PersoninfoService(pdlClient)

    return RapidApplication.create(env).apply {
        Personinfoløser(this, personinfoService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
