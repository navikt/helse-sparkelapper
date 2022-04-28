package no.nav.helse.sparkel.oppgaveendret.sts

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import no.nav.helse.sparkel.oppgaveendret.util.ServiceUser

/**
 * henter jwt token fra STS
 */
class StsRestClient(
    private val baseUrl: String,
    private val serviceUser: ServiceUser,
    private val objectMapper: ObjectMapper
) {
    private var cachedOidcToken: Token = fetchToken()

    internal fun token(): String {
        if (cachedOidcToken.expired) cachedOidcToken = fetchToken()
        return cachedOidcToken.access_token
    }

    private fun fetchToken(): Token {
        val url = "$baseUrl/rest/v1/sts/token?grant_type=client_credentials&scope=openid"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "GET"
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Authorization", serviceUser.basicAuth)
            setRequestProperty("Accept", "application/json")

            val stream: InputStream? = if (responseCode < 300) this.inputStream else this.errorStream
            responseCode to stream?.bufferedReader()?.readText()
        }
        if (responseCode >= 300 || responseBody == null) {
            throw RuntimeException("unknown error (responseCode=$responseCode) from sts")
        }

        return objectMapper.readValue(responseBody)

    }

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
