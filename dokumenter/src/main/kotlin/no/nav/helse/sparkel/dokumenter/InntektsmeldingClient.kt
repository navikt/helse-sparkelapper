package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class InntektsmeldingClient(
    private val baseUrl: String,
    private val tokenClient: AzureTokenProvider,
    private val httpClient: HttpClient,
    private val scope: String
) : DokumentClient {
    companion object {
        private val objectMapper = ObjectMapper()
        private val log = LoggerFactory.getLogger(InntektsmeldingClient::class.java)
        private val sikkerlog = LoggerFactory.getLogger("tjenestekall")
    }

    override fun hentDokument(dokumentId: String): JsonNode {
        return runBlocking {
            val response = httpClient.prepareGet("$baseUrl/api/v1/inntektsmelding/$dokumentId") {
                accept(ContentType.Application.Json)
                method = HttpMethod.Get
                val bearerToken = tokenClient.bearerToken(scope)
                bearerToken as Result.Ok
                bearerAuth(bearerToken.value.token)
                val callId = UUID.randomUUID()
                header("Nav-Callid", "$callId")
                header("no.nav.callid", "$callId")
                header("Nav-Consumer-Id", "sparkel-dokumenter")
                header("no.nav.consumer.id", "sparkel-dokumenter")
            }.execute()

            if (response.status != HttpStatusCode.OK) {
                "Feil ved kall mot syfoinntektsmelding http ${response.status.value}, returnerer derfor tomt resultat".also {
                    log.info(it)
                    sikkerlog.info("$it response: $response")
                }
                objectMapper.createObjectNode().put("error", response.status.value)
            } else response.body<JsonNode>()
        }
    }
}

