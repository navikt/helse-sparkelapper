package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.vilkarsproving.logger
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
        hentTokenMedEnklereHttpClient().also { logger.info("Token mot skjermende hentet ut fra AD.") }
    }

    internal fun accessToken(): String {
        if (cachedAccessToken.expired) cachedAccessToken = fetchToken().also { logger.info("Token mot skjermede oppfrisket 👍") }
        return cachedAccessToken.access_token
    }

    private companion object {
        private val azureAdClient = HttpClient(CIO) {
            engine {
                System.getenv("HTTP_PROXY")?.let {
                    proxy = ProxyBuilder.http(Url(it))
                }
            }
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
        }
    }

    private fun fetchToken(): Token {
        return runBlocking {
            azureAdClient.preparePost(props.tokenEndpointURL) {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                setBody(FormDataContent(Parameters.build {
                    append("client_id", props.clientId)
                    append("scope", props.skjermendeOauthScope)
                    append("grant_type", "client_credentials")
                    append("client_secret", props.clientSecret)
                }))
            }.body()
        }
    }

    private fun hentTokenMedEnklereHttpClient() {
        try {
            val body = props.run {
                "client_id=$clientId&client_secret=$clientSecret&scope=$skjermendeOauthScope&grant_type=client_credentials"
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

    @JsonIgnoreProperties(ignoreUnknown = true)
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
    val skjermendeOauthScope: String,
)

