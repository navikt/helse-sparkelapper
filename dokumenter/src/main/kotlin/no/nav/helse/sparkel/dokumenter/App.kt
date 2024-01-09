package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
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

        val azureClient = createAzureTokenClientFromEnvironment(env)

        val søknadClient = SøknadClient(
            baseUrl = env.getValue("SOKNAD_API_URL"),
            tokenClient = azureClient,
            httpClient = httpClient,
            scope = env.getValue("ACCESS_TOKEN_SCOPE")
        )

        val inntektsmeldingClient = InntektsmeldingClient(
            baseUrl = env.getValue("IM_API_URL"),
            tokenClient = azureClient,
            httpClient = httpClient,
            scope = env.getValue("ACCESS_TOKEN_SCOPE_IM")
        )

        DokumentRiver(rapidsConnection = this, søknadClient = søknadClient, inntektsmeldingClient = inntektsmeldingClient)
    }
}

internal interface DokumentClient {
    fun hentDokument(dokumentId: String): JsonNode
}