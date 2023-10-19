package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

private val mapper = jacksonObjectMapper()
    .registerModule(JavaTimeModule())

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    return RapidApplication.create(env).apply {
        val httpClient = HttpClient(Apache) {
            install(ContentNegotiation) {
                register(
                    ContentType.Application.Json, JacksonConverter(mapper)
                )
            }
        }

        val søknadClient = SøknadClient(
            baseUrl = env.getValue("SOKNAD_API_URL"),
            AccessTokenClient(
                aadAccessTokenUrl = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
                clientId = env.getValue("AZURE_APP_CLIENT_ID"),
                clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
                httpClient = httpClient
            ),
            httpClient = httpClient,
            scope = env.getValue("ACCESS_TOKEN_SCOPE")
        )

        DokumentRiver(rapidsConnection = this, søknadClient = søknadClient)
    }
}

internal interface DokumentClient {
    fun hentDokument(dokumentId: String): JsonNode
}