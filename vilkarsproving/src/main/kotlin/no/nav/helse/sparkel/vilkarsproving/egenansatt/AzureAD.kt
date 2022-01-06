package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import java.net.URL
import java.time.LocalDateTime

class AzureAD(val props: AzureADProps) {
    private var cachedAccessToken: Token = fetchToken()

    internal fun accessToken(): String {
        if (cachedAccessToken.expired) cachedAccessToken = fetchToken()
        return cachedAccessToken.access_token
    }

    private companion object {
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
        private val azureAdClient = HttpClient(CIO) {
            install(JsonFeature) {
                serializer = JacksonSerializer {
                    registerModule(JavaTimeModule())
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

