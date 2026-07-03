package no.nav.helse.sparkel.dokumenter

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson3.JacksonConverter
import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import no.nav.helse.rapids_rivers.RapidApplication
import tools.jackson.databind.JsonNode
import tools.jackson.module.kotlin.jacksonObjectMapper

private val mapper = jacksonObjectMapper()

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
    fun hentDokument(dokumentId: String): Result<JsonNode>
}

internal val retryableExceptions = arrayOf(
    IOException::class,
    ClosedReceiveChannelException::class,
    SSLHandshakeException::class,
    SocketTimeoutException::class,
    ServerResponseException::class,
)
