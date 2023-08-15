package no.nav.helse.sparkel.sigrun

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant
import kotlin.collections.set

class AccessTokenClient(
    private val aadAccessTokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient
) {

    private val log = LoggerFactory.getLogger(AccessTokenClient::class.java)

    suspend fun hentAccessToken(scope: String): AadAccessToken {
        log.info("Henter nytt token fra Azure AD")
        return try {
            httpClient.preparePost(aadAccessTokenUrl) {
                accept(ContentType.Application.Json)
                method = HttpMethod.Post
                setBody(FormDataContent(Parameters.build {
                    append("client_id", clientId)
                    append("scope", scope)
                    append("grant_type", "client_credentials")
                    append("client_secret", clientSecret)
                }))
            }.body()
        } catch (e: Exception) {
            throw RuntimeException("Klarte ikke hente nytt token fra Azure AD", e)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class AadAccessToken(
    val access_token: String,
    val expires_in: Duration
) {
    internal val expiry = Instant.now().plus(expires_in)
}
