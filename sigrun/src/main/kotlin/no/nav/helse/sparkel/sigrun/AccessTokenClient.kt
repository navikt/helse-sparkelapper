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
import java.time.LocalDateTime
import kotlin.collections.set

class AccessTokenClient(
    private val aadAccessTokenUrl: String,
    private val clientId: String,
    private val clientSecret: String,
    private val httpClient: HttpClient
) {

    private val log = LoggerFactory.getLogger(AccessTokenClient::class.java)

    suspend fun hentAccessToken(scope: String): AccessToken? {
        log.info("Henter nytt token fra Azure AD")
        val result: AadResponse = try {
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

        if (result.access_token != null && result.expires_in != null)
            return AccessToken(result.access_token, LocalDateTime.now().plus(result.expires_in))
        log.error("feil fra Azure: ${result.error}: ${result.error_description}")
        return null
    }
}

class AccessToken(
    private val accessToken: String,
    private val expires: LocalDateTime
) {
    fun berikRequestMedBearer(headers: HeadersBuilder) {
        headers["Authorization"] = "Bearer $accessToken"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class AadResponse(
    val access_token: String?,
    val expires_in: Duration?,
    val error: String?,
    val error_description: String?
) {

}
