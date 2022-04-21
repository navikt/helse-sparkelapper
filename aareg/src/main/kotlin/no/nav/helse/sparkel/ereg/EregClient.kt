package no.nav.helse.sparkel.ereg

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.StsRestClient
import no.nav.helse.sparkel.aareg.objectMapper
import java.util.*

class EregClient(
    private val baseUrl: String,
    private val appName: String,
    private val httpClient: HttpClient,
    private val stsRestClient: StsRestClient,
) {
    suspend fun hentOrganisasjon(
        organisasjonsnummer: String,
        callId: UUID,
    ) = hentFraEreg(organisasjonsnummer, callId)

    private suspend fun hentFraEreg(
        organisasjonsnummer: String,
        callId: UUID,
    ): EregResponse {
        val response: HttpResponse =
            httpClient.get("$baseUrl/v1/organisasjon/$organisasjonsnummer?inkluderHierarki=true&inkluderHistorikk=true") {
                header("Authorization", "Bearer ${stsRestClient.token()}")
                header("Nav-Consumer-Token", "Bearer ${stsRestClient.token()}")
                header("Nav-Consumer-Id", appName)
                header("Nav-Call-Id", callId)
                accept(ContentType.Application.Json)
            }

        if (response.status.isSuccess()) {
            val json = objectMapper.readTree(response.bodyAsText())
            return EregResponse(
                    navn = trekkUtNavn(json),
                    næringer = json.path("organisasjonDetaljer").path("naeringer").takeIf { !it.isMissingNode }
                        ?.map { it["naeringskode"].asText() } ?: emptyList()
                )
        } else throw FeilVedHenting("ereg svarte med ${response.status.value}")
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

class FeilVedHenting(msg: String): RuntimeException(msg)
