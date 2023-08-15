package no.nav.helse.sparkel.sigrun

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("no.nav.helse.sparkel.sigrun.App")
private val sikkerlog = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                register(
                    ContentType.Application.Json, JacksonConverter(
                        jacksonObjectMapper()
                            .registerModule(JavaTimeModule())
                    )
                )
            }
            install(Logging) {
                this.level = LogLevel.ALL
                this.logger = object : Logger {
                    override fun log(message: String) {
                        sikkerlog.info(message)
                    }
                }
            }
        }

        val tokenClient = AccessTokenClient(
            aadAccessTokenUrl = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
            httpClient = httpClient
        )

        val result = runBlocking { tokenClient.hentAccessToken(env.getValue("ACCESS_TOKEN_SCOPE")) }
        log.info("Hentet token OK")
    }
}
