package no.nav.helse.sparkel.inntekt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.slf4j.LoggerFactory
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

internal typealias TokenSupplier = (String) -> String

class AzureAd(env: Environment, private val props: AzureAdProperties = AzureAdProperties(env)) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    private lateinit var cachedAccessToken: Token

    internal fun accessToken(scope: String): String {
        if (!::cachedAccessToken.isInitialized || cachedAccessToken.expired) cachedAccessToken = try {
            hentToken(scope)
        } catch (e: Exception) {
            logger.warn("Kunne ikke hente token", e)
            throw e
        }
        return cachedAccessToken.access_token
    }

    private fun hentToken(scope: String): Token {
        val body = props.run {
            "client_id=$clientId&client_secret=$clientSecret&scope=${scope}&grant_type=client_credentials"
        }
        logger.info("Henter token fra AAD")
        val request = HttpRequest.newBuilder(props.url)
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        val token = objectMapper.readValue(response.body(), Token::class.java)!!
        logger.info("Hentet token fra AAD")
        return token
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

data class AzureAdProperties(val environment: Environment) {
    val url = environment.aadTokenEndpoint
    val clientId = environment.aadClientId
    val clientSecret = environment.aadClientSecret
}
