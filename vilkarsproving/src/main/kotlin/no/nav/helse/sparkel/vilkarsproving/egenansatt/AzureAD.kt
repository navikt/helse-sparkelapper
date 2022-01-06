package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime

private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

class AzureAD(val props: AzureADProps) {
    private var cachedAccessToken: Token = fetchToken()

    internal fun accessToken(): String {
        if (cachedAccessToken.expired) cachedAccessToken = fetchToken()
        return cachedAccessToken.access_token
    }

    private companion object {
        private val objectMapper: ObjectMapper = jacksonObjectMapper()
    }

    private fun fetchToken(): Token {
        val (responseCode, responseBody) = with(props.tokenEndpointURL.openConnection() as HttpURLConnection) {
            requestMethod = "POST"
            connectTimeout = 10000
            readTimeout = 10000
            doOutput = true
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("Accept", "application/json")
            outputStream.use { os ->
                os.writer().run {
                    write("client_id=${props.clientId}&client_secret=${props.clientSecret}&scope=${props.nomOauthScope}&grant_type=client_credentials"
                        .also {
                            sikkerLogg.info(it)
                        })
                    close()
                }
            }

            val stream: InputStream = if (responseCode < 300) inputStream else errorStream
            stream.use {
                val responseBody = it.bufferedReader().readText()
                sikkerLogg.info("Svar fra AD: $responseBody")
                responseCode to responseBody
            }
        }
        if (responseCode != 200) {
            throw RuntimeException("Error from Azure AD: $responseCode - $responseBody")
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

data class AzureADProps(
    val tokenEndpointURL: URL,
    val clientId: String,
    val clientSecret: String,
    val nomOauthScope: String,
)

