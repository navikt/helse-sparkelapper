package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import java.util.UUID
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.retry

class EregClient(
    private val baseUrl: String,
    private val appName: String,
    private val httpClient: HttpClient
) {
    suspend fun hentOrganisasjon(
        organisasjonsnummer: String,
        callId: UUID,
    ) = hentFraEreg(organisasjonsnummer, callId)

    private suspend fun hentFraEreg(
        organisasjonsnummer: String,
        callId: UUID,
    ): EregResponse = retry("ereg") {
        val response: HttpResponse =
            httpClient.get("$baseUrl/api/v1/organisasjon/$organisasjonsnummer?inkluderHierarki=true&inkluderHistorikk=true") {
                header("Nav-Consumer-Id", appName)
                header("Nav-Call-Id", callId)
                accept(ContentType.Application.Json)
            }

        sikkerlogg.info("EregResponse: ${response.status}\n${response.bodyAsText()}")

        if (!response.status.isSuccess()) throw FeilVedHenting("ereg svarte med ${response.status.value}")

        mapResponse(response)
    }

    private suspend fun mapResponse(response: HttpResponse) =
        objectMapper.readTree(response.bodyAsText()).let { json ->
            EregResponse(
                navn = trekkUtNavn(json),
                næringer = json.path("organisasjonDetaljer").path("naeringer").takeIf { !it.isMissingNode }
                    ?.map { it["naeringskode"].asText() } ?: emptyList()
            )
        }

    private fun trekkUtNavn(organisasjon: JsonNode) =
        organisasjon["navn"].let { navn ->
            (1..5).mapNotNull { index -> navn["navnelinje$index"] }
                .filterNot(JsonNode::isMissingOrNull)
                .map(JsonNode::asText)
                .filterNot(String::isBlank)
        }.joinToString()

}

data class EregResponse(
    val navn: String,
    val næringer: List<String>,
)

class FeilVedHenting(msg: String) : RuntimeException(msg)
