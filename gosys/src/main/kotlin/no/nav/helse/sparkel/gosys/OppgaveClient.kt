package no.nav.helse.sparkel.gosys

import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.getOrThrow
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson3.JacksonConverter
import java.io.IOException
import javax.net.ssl.SSLHandshakeException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import no.nav.helse.sparkel.retry
import tools.jackson.core.exc.StreamReadException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

internal class OppgaveClient(
    private val baseUrl: String,
    private val scope: String,
    private val azureClient: AzureTokenProvider,
    private val httpClient: HttpClient = Companion.httpClient,
) : Oppgavehenter {

    companion object {
        private val objectMapper = ObjectMapper()
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                register(Json, JacksonConverter(objectMapper))
            }
            expectSuccess = false
        }
    }

    private suspend fun kallOppgavetjenesten(aktørId: String, behovId: String): JsonNode {
        val response = httpClient.get("${baseUrl}/api/v1/oppgaver?statuskategori=AAPEN&tema=SYK&aktoerId=${aktørId}") {
            azureClient.bearerToken(scope).getOrThrow().let { azureToken ->
                header("Authorization", "Bearer ${azureToken.token}")
            }
            header("X-Correlation-ID", behovId)
            System.getenv("NAIS_APP_NAME")?.let { header("Nav-Consumer-Id", it) }
            accept(Json)
        }
        val status = response.status
        if (status >= HttpStatusCode.MultipleChoices) {
            throw RuntimeException("Feil fra oppgavetjenesten, status: $status")
        }

        val body = response.bodyAsText()
        sikkerlogg.info("Respons fra oppgavetjenesten: ${status}\n$body")
        if (body.isBlank()) {
            throw RuntimeException("Fikk ikke noe innhold tilbake fra oppgavetjenesten, status: $status")
        }

        return objectMapper.readTree(body)
    }

    override suspend fun hentÅpneOppgaver(
        aktørId: String,
        behovId: String,
    ): JsonNode = retry("oppgavetjenesten", legalExceptions = arrayOf(
        StreamReadException::class, // I Jackson 3 har man gått vekk fra checked exceptions
        IOException::class,
        ClosedReceiveChannelException::class,
        SSLHandshakeException::class,
    )) {
        kallOppgavetjenesten(aktørId, behovId)
    }
}
