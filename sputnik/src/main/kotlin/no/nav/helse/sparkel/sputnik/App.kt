package no.nav.helse.sparkel.sputnik

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson


internal val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val serviceUser = readServiceUserCredentials()
    val environment = setUpEnvironment()
    val rapidsConnection = launchApplication(environment, serviceUser)
    rapidsConnection.start()
}

fun launchApplication(
    environment: Environment,
    serviceUser: ServiceUser
): RapidsConnection {
    val stsRestClient = StsRestClient(environment.stsBaseUrl, serviceUser)
    val fpsakRestClient = FpsakRestClient(
        baseUrl = environment.fpsakBaseUrl,
        httpClient = simpleHttpClient(),
        stsRestClient = stsRestClient
    )

    return RapidApplication.create(environment.raw).apply {
        Foreldrepenger(this, fpsakRestClient)
    }
}

private fun simpleHttpClient() = HttpClient {
    install(ContentNegotiation) {
        jackson {
            objectMapper
        }
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }
}
