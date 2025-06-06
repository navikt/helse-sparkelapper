package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
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

class InntektsmeldingClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String
) : DokumentClient {

    override fun hentDokument(dokumentId: String): Result<JsonNode> {
        return runBlocking {
            try {
                fetch(dokumentId, callId = UUID.randomUUID())
            } catch (e: ClientRequestException) {
                // Diskutabelt om "success" gir mening i denne kontekst, men se på det som at clienten har klart å
                // produsere et svar. Alternativt kunne DokumentRiver ha håndtert en spesifikk exception for 404
                // i onFailure-blokka.
                Result.success(JsonNodeFactory.instance.objectNode().put("error", 404))
            }
        }
    }

    private suspend fun fetch(dokumentId: String, callId: UUID): Result<JsonNode> = retry("inntektsmelding", legalExceptions = retryableExceptions) {
            val response = httpClient.prepareGet("$baseUrl/api/v1/inntektsmelding/$dokumentId") {
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

            Result.success(response.body())
        }
    }


