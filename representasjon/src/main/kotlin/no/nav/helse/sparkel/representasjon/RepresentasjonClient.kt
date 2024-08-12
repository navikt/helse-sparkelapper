package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
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
import java.util.Base64
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

    fun hentFullmakt(fnr: String): JsonNode? {
        val callId = UUID.randomUUID()
        return try {
            runBlocking {
                val response = httpClient.preparePost("$baseUrl/api/internbruker/fullmaktsgiver") {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    bearerAuth(tokenClient.bearerToken(scope).token)
                    setBody("""{"ident": "${Base64.getEncoder().encodeToString(fnr.toByteArray())}"}""")
                    header("Nav-Callid", "$callId")
                    header("no.nav.callid", "$callId")
                    header("Nav-Consumer-Id", "sparkel-representasjon")
                    header("no.nav.consumer.id", "sparkel-representasjon")
                }.execute()

                if (response.status != HttpStatusCode.OK) {
                    "Feil ved kall mot fullmakt-api, http-status: ${response.status.value}, returnerer tomt resultat. CallId=$callId".also {
                        log.info(it)
                        sikkerlog.info("$it, response:\n$response")
                    }
                    null
                } else response.body()
            }
        } catch (e: Exception) {
            log.warn("Feil mot fullmakt-api, callId=$callId, se sikker logg for exception")
            sikkerlog.warn("Feil mot fullmakt-api, callId=$callId", e)
            null
        }
    }
}

