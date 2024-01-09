package no.nav.helse.sparkel.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureAuthMethod
import com.github.navikt.tbd_libs.azure.AzureTokenClient
import com.github.navikt.tbd_libs.azure.InMemoryAzureTokenCache
import javax.swing.InputMap
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main() {
    val env = setUpEnvironment()
    val app = createApp(env)
    app.start()
}

internal val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

internal val logger: Logger = LoggerFactory.getLogger(::main.javaClass)

fun createApp(env: Environment): RapidsConnection {
    val rapidsConnection = RapidApplication.create(env.raw)


    val aad = InMemoryAzureTokenCache(AzureTokenClient(
        tokenEndpoint = env.tokenEndpointURL,
        clientId = env.clientId,
        authMethod = AzureAuthMethod.Secret(env.clientSecret)
    ))

    val skjermedePersoner = SkjermedePersoner(aad, env.skjermedeBaseURL, env.skjermendeOauthScope)

    EgenAnsattLøser(rapidsConnection, skjermedePersoner)

    return rapidsConnection
}
