package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = System.getenv()

    val serviceUser = ServiceUser(
        username = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/username")),
        password = Files.readString(Paths.get("/var/run/secrets/nais.io/service_user/password"))
    )

    val stsRestClient = StsRestClient("http://security-token-service.default.svc.nais.local", serviceUser)
    val inntektRestClient = InntektRestClient(
        baseUrl = env.getValue("INNTEKTSKOMPONENT_BASE_URL"),
        httpClient = simpleHttpClient(),
        stsRestClient = stsRestClient
    )

    RapidApplication.create(System.getenv()).apply {
        Inntekter(this, inntektRestClient)
    }.start()
}

private fun simpleHttpClient() = HttpClient() {
    val sikkerLogg = LoggerFactory.getLogger("tjenestekall")

    install(Logging) {
        level = LogLevel.BODY
        logger = object : io.ktor.client.features.logging.Logger {
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

    install(JsonFeature) {
        this.serializer = JacksonSerializer(jackson = objectMapper)
    }
}

class ServiceUser(
    val username: String,
    val password: String
) {
    val basicAuth = "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
}
