package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import io.ktor.client.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = setUpEnvironment()
    val tokenProvider = createAzureTokenClientFromEnvironment(env.raw)
    val inntektRestClient = InntektRestClient(
        baseUrl = env.inntektRestUrl,
        inntektskomponentenOAuthScope = env.inntektOAuthScope,
        httpClient = simpleHttpClient(),
        tokenSupplier = { tokenProvider.bearerToken(it).token },
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
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
}
