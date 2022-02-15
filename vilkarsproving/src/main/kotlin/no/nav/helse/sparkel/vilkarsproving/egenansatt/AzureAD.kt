package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.vilkarsproving.objectMapper
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

class AzureAD(private val props: AzureADProps) {
    private var cachedAccessToken: Token = fetchToken()

    private val sikkerLogg = LoggerFactory.getLogger("tjenestekall")

    init {
        hentTokenMedEnklereHttpClient()
    }

    internal fun accessToken(): String {
        if (cachedAccessToken.expired) cachedAccessToken = fetchToken()
        return cachedAccessToken.access_token
    }

    private companion object {
        private val azureAdClient = HttpClient(CIO) {
            engine {
                System.getenv("HTTP_PROXY")?.let {
                    proxy = ProxyBuilder.http(Url(it))
                }
            }
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    registerModule(JavaTimeModule())
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
        }
    }

    private fun fetchToken(): Token {
        return runBlocking {
            azureAdClient.post(props.tokenEndpointURL) {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                body = FormDataContent(Parameters.build {
                    append("client_id", props.clientId)
                    append("scope", props.nomOauthScope)
                    append("grant_type", "client_credentials")
                    append("client_secret", props.clientSecret)
                })
            }
        }
    }

    private fun hentTokenMedEnklereHttpClient() {
        try {
            val body = props.run {
                "client_id=$clientId&client_secret=$clientSecret&scope=$nomOauthScope&grant_type=client_credentials"
            }
            val request = HttpRequest.newBuilder(props.tokenEndpointURL.toURI())
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

            val response = java.net.http.HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
            val token = objectMapper.readValue(response.body(), Token::class.java)
            sikkerLogg.info("Hentet token vha java.net.http.HttpClient: $token")
        } catch (e: Exception) {
            sikkerLogg.info("Noe gikk gæernt", e)
        }
    }

    internal data class Token(
        internal val access_token: String,
        private val token_type: String,
        private val expires_in: Long
    ) {
        private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)
        internal val expired get() = expirationTime.isBefore(LocalDateTime.now())
    }
}

data class AzureADProps(
    val tokenEndpointURL: URL,
    val clientId: String,
    val clientSecret: String,
    val nomOauthScope: String,
)

