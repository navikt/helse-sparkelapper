package no.nav.helse.sparkel.gosys

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = createAzureTokenClientFromEnvironment(env)
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
