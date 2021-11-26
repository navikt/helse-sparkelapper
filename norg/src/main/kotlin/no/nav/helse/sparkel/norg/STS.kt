package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

class STS(
    private val baseUrl: String,
    private val serviceUser: ServiceUser
) {
    private var cachedOidcToken: Token = fetchToken()

    internal fun token(): String {
        if (cachedOidcToken.expired) cachedOidcToken = fetchToken()
        return cachedOidcToken.access_token
    }

    private companion object {
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
    }

    private fun fetchToken(): Token {
        val url = "$baseUrl/rest/v1/sts/token"
        val (responseCode, responseBody) = with(URL(url).openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            setRequestProperty("Authorization", serviceUser.basicAuth)
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Accept", "application/json")
            outputStream.use { os ->
                os.writer().write("grant_type=client_credentials&scope=openid")
            }

            this.inputStream.use { responseCode to this.inputStream.bufferedReader().readText() }
        }
        if (responseCode != 200) {
            throw RuntimeException("error from sts: $responseCode - $responseBody")
        }

        return objectMapper.readValue(responseBody)
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
