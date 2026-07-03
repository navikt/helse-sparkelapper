package no.nav.helse.sparkel.egenansatt

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.helse.rapids_rivers.RapidApplication
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

fun main() {
    val env = setUpEnvironment()
    val app = createApp(env)
    app.start()
}

internal val objectMapper: ObjectMapper = jacksonObjectMapper()

fun createApp(env: Environment): RapidsConnection {
    val rapidsConnection = RapidApplication.create(env.raw)


    val aad = createAzureTokenClientFromEnvironment(env.raw)

    val skjermedePersoner = SkjermedePersoner(aad, env.skjermedeBaseURL, env.skjermendeOauthScope)

    EgenAnsattLøser(rapidsConnection, skjermedePersoner)

    return rapidsConnection
}
