package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.retry

class SøknadClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String
) : DokumentClient {

    override fun hentDokument(dokumentId: String): Result<JsonNode> {
        return runBlocking {
            runCatching {
                fetch(dokumentId, callId = UUID.randomUUID())
            }
        }
    }

    private suspend fun fetch(dokumentId: String, callId: UUID): JsonNode = retry("søknad", legalExceptions = (retryableExceptions + ClientRequestException::class)) {
        val response = httpClient.prepareGet("$baseUrl/api/v3/soknader/$dokumentId/kafkaformat") {
            accept(ContentType.Application.Json)
            method = HttpMethod.Get
            expectSuccess = true
            val bearerToken = tokenClient.bearerToken(scope).getOrThrow()
            bearerAuth(bearerToken.token)
            header("Nav-Callid", "$callId")
            header("no.nav.callid", "$callId")
            header("Nav-Consumer-Id", "sparkel-dokumenter")
            header("no.nav.consumer.id", "sparkel-dokumenter")
        }.execute()

        response.body()
    }
}

