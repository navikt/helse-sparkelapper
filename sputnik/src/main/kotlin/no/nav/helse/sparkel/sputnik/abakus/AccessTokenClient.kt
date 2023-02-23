package no.nav.helse.sparkel.sputnik.abakus

import java.net.URL
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.sputnik.abakus.HttpRequest.post
import org.slf4j.LoggerFactory

internal interface AccessTokenClient {
    fun accessToken(): String
}

internal class ClientSecretPost(
    tokenEndpoint: String,
    clientId: String,
    clientSecret: String,
    scope: String
) : AccessTokenClient {
    private val tokenEndpointUrl = URL(tokenEndpoint)
    private val requestBody = "client_id=$clientId&client_secret=$clientSecret&scope=$scope&grant_type=client_credentials"
    private var cachedAccessToken: Pair<String, LocalDateTime>? = null

    override fun accessToken() =
        cachedAccessToken?.takeIf { it.second > now() }?.first ?: hentOgCache()

    private fun accessTokenResponse(): Pair<String, Long> {
        val (_, json) = tokenEndpointUrl.post(requestBody)
        val accessToken = json.path("access_token")
        val expiresIn = json.path("expires_in")
        if (accessToken.isMissingOrNull())
            throw IllegalStateException("Mangler access_token i response=$json")
        if (expiresIn.isMissingOrNull())
            throw IllegalStateException("Mangler expires_in i response=$json")
        return accessToken.asText() to expiresIn.asLong()
    }

    private fun hentOgCache(): String {
        logger.info("Henter nytt access token fra $tokenEndpointUrl")
        val (accessToken, expiresIn) = accessTokenResponse()
        cachedAccessToken = accessToken to now().plusSeconds(expiresIn).minusSeconds(30)
        return accessToken
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(ClientSecretPost::class.java)
    }
}
