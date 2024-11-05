package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.speed.SpeedClient
import java.net.http.HttpClient
import no.nav.helse.rapids_rivers.RapidApplication

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
    val speedClient = SpeedClient(
        httpClient = HttpClient.newHttpClient(),
        objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule()),
        tokenProvider = azureClient
    )
    val oppgaveService = OppgaveService(oppgaveClient)

    return RapidApplication.create(env).apply {
        Oppgaveløser(this, oppgaveService, speedClient)
    }
}
