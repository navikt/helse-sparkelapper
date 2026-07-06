package no.nav.helse.sparkel.inntekt

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.jackson3.JacksonConverter
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

val objectMapper: ObjectMapper = jacksonObjectMapper()

fun main() {
    val env = setUpEnvironment()
    val tokenProvider = createAzureTokenClientFromEnvironment(env.raw)
    val inntektRestClient = InntektRestClient(
        baseUrl = env.inntektRestUrl,
        inntektskomponentenOAuthScope = env.inntektOAuthScope,
        httpClient = simpleHttpClient(),
        tokenSupplier = {
            tokenProvider.bearerToken(it).getOrThrow().token
        },
    )

    RapidApplication.create(System.getenv()).apply {
        Inntekter(this, inntektRestClient)
    }.start()
}

internal typealias TokenSupplier = (String) -> String

private fun simpleHttpClient() = HttpClient {
    val sikkerLogg = LoggerFactory.getLogger("tjenestekall")

    install(Logging) {
        level = LogLevel.BODY
        logger = object : io.ktor.client.plugins.logging.Logger {
            private var logBody = false
            override fun log(message: String) {
                when {
                    message == "BODY START" -> logBody = true
                    message == "BODY END" -> logBody = false
                    logBody -> sikkerLogg.debug("respons fra Inntektskomponenten: $message")
                }
            }
        }
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 1000
        requestTimeoutMillis = 120_000
        socketTimeoutMillis = 120_000
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
}
