package no.nav.helse.sparkel.pleiepenger.k9

import java.net.URL
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.util.Base64
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.pleiepenger.k9.HttpRequest.get

interface AccessTokenClient {
    fun accessToken(): String
}

internal class ClientSecretBasic(
    tokenEndpoint: String,
    clientId: String,
    clientSecret: String,
    scope: String = "openid"
) : AccessTokenClient {
    private val tokenUrl = URL("$tokenEndpoint?grant_type=client_credentials&scope=$scope")

    private val authorizationHeader =
        "Basic ${Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())}"

    private var cachedAccessToken: Pair<String, LocalDateTime>? = null

    override fun accessToken() =
        cachedAccessToken?.takeIf { it.second > now() }?.first ?: hentOgCache()

    private fun accessTokenResponse(): Pair<String, Long> {
        val (_, json) = tokenUrl.get(
            "Authorization" to authorizationHeader
        )
        val accessToken = json.path("access_token")
        val expiresIn = json.path("expires_in")
        if (accessToken.isMissingOrNull())
            throw IllegalStateException("Mangler access_token i response=$json")
        if (expiresIn.isMissingOrNull())
            throw IllegalStateException("Mangler expires_in i response=$json")
        return accessToken.asText() to expiresIn.asLong()
    }

    private fun hentOgCache(): String {
        val (accessToken, expiresIn) = accessTokenResponse()
        cachedAccessToken = accessToken to now().plusSeconds(expiresIn).minusSeconds(30)
        return accessToken
    }
}
