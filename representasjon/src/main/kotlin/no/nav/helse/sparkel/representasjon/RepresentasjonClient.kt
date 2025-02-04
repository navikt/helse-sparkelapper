package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.UUID
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import no.nav.helse.sparkel.retry

private val retryableExceptions = arrayOf(
    IOException::class,
    ClosedReceiveChannelException::class,
    SSLHandshakeException::class,
    SocketTimeoutException::class,
    ServerResponseException::class,
)

class RepresentasjonClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String,
) {
    suspend fun hentFullmakt(fnr: String): Result<JsonNode> {
        val callId = UUID.randomUUID()
        return retry("fullmakt", legalExceptions = retryableExceptions) {
            val response = httpClient.preparePost("$baseUrl/api/internbruker/fullmakt/fullmaktsgiver") {
                expectSuccess = true
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                val bearerToken = tokenClient.bearerToken(scope).getOrThrow()
                bearerAuth(bearerToken.token)
                setBody(mapOf("ident" to fnr))
                header("Nav-Call-Id", "$callId")
                header("no.nav.callid", "$callId")
                header("Nav-Consumer-Id", "sparkel-representasjon")
                header("no.nav.consumer.id", "sparkel-representasjon")
            }.execute()

            Result.success(response.body())
        }
    }
}
