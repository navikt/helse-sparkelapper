package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.preparePost
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class RepresentasjonClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String
) {
    companion object {
        private val log = LoggerFactory.getLogger(RepresentasjonClient::class.java)
        private val sikkerlog = LoggerFactory.getLogger("tjenestekall")
    }

    fun hentFullmakt(fnr: String): Result<JsonNode> {
        val callId = UUID.randomUUID()
        return try {
            runBlocking {
                val response = httpClient.preparePost("$baseUrl/api/internbruker/fullmakt/fullmaktsgiver") {
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

                if (response.status != HttpStatusCode.OK) {
                    "Feil ved kall mot repr-api, http-status: ${response.status.value}, returnerer tomt resultat. CallId=$callId".also {
                        log.warn(it)
                        sikkerlog.warn("$it, response:\n$response")
                    }
                    Result.failure(RuntimeException("Feil ved kall mot repr-api"))
                } else Result.success(response.body())
            }
        } catch (e: Exception) {
            log.warn("Feil mot repr-api, callId=$callId, se sikker logg for exception")
            sikkerlog.warn("Feil mot repr-api, callId=$callId", e)
            Result.failure(e)
        }
    }
}

