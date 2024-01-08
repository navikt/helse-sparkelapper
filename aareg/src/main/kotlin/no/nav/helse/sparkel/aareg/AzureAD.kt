package no.nav.helse.sparkel.aareg

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.jackson.JacksonConverter
import java.net.URI
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class AzureAD(
    private val props: AzureADProps,
    private val objectMapper: ObjectMapper,
    private val client: HttpClient = HttpClient.newHttpClient()
) {
    private val cachedTokens = ConcurrentHashMap<String, Token>()

    internal fun accessToken(scope: String): String {
        return try {
            cachedTokens.compute(scope) { _, eksisterendeVerdi ->
                eksisterendeVerdi?.takeUnless(Token::expired) ?: hentToken(scope)
            }!!.access_token
        } catch (e: Exception) {
            logger.warn("Kunne ikke hente token", e)
            throw e
        }
    }

    private fun hentToken(scope: String): Token {
        val body = "client_id=${props.clientId}&client_secret=${props.clientSecret}&scope=$scope&grant_type=client_credentials"
        logger.info("Henter token for $scope")
        val request = HttpRequest.newBuilder(props.tokenEndpointURL)
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val responseBody = response.body()
        try {
            val token = objectMapper.readValue(responseBody, Token::class.java)!!
            logger.info("Hentet token fra AAD")
            return token
        } catch (err: Exception) {
            try {
                val error = objectMapper.readValue<ErrorResponse>(body)
                logger.error("Klarte ikke å hente token. Azure sier: ${error.error}: ${error.error_description}", err)
                throw RuntimeException("${error.error}: ${error.error_description}")
            } catch (_: Exception) {
                logger.error("Klarte ikke å hente token, noe galt skjedde: ${err.message}", err)
                sikkerlogg.error("Klarte ikke å hente token, noe galt skjedde: ${err.message}.\nResponse body: $responseBody", err)
            }
            throw err
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

    private data class ErrorResponse(
        val error: String,
        val error_description: String
    )
}

data class AzureADProps(
    val tokenEndpointURL: URI,
    val clientId: String,
    val clientSecret: String
)

