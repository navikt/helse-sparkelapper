package no.nav.helse.sparkel.aareg.arbeidsforholdV2

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.util.ServiceUser
import java.time.LocalDateTime

/**
 * henter jwt fra STS
 */
class StsRestClient(
    private val baseUrl: String,
    private val serviceUser: ServiceUser,
    private val httpClient: HttpClient = HttpClient() {
        install(HttpTimeout) {
            socketTimeoutMillis = 10000
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
        }
    }
) {
    private var cachedOidcToken: Token = runBlocking { fetchToken() }

    suspend fun token(): String {
        if (cachedOidcToken.expired) cachedOidcToken = fetchToken()
        return cachedOidcToken.access_token
    }

    private suspend fun fetchToken(): Token = httpClient.get<HttpStatement>(
        "$baseUrl/rest/v1/sts/token?grant_type=client_credentials&scope=openid"
    ) {
        header("Authorization", serviceUser.basicAuth)
        accept(ContentType.Application.Json)
    }.execute { objectMapper.readValue<Token>(it.readText()) }

    internal data class Token(
        internal val access_token: String,
        private val token_type: String,
        private val expires_in: Long
    ) {
        // expire 10 seconds before actual expiry. for great margins.
        private val expirationTime: LocalDateTime = LocalDateTime.now().plusSeconds(expires_in - 10L)
        internal val expired get() = expirationTime.isBefore(LocalDateTime.now())
    }
}
