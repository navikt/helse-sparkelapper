package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.aareg.objectMapper
import java.util.*
import no.nav.helse.sparkel.aareg.sikkerlogg

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
    ): EregResponse {
        val response: HttpResponse =
            httpClient.get("$baseUrl/api/v1/organisasjon/$organisasjonsnummer?inkluderHierarki=true&inkluderHistorikk=true") {
                header("Nav-Consumer-Id", appName)
                header("Nav-Call-Id", callId)
                accept(ContentType.Application.Json)
            }

        sikkerlogg.info("EregResponse status: " + response.status )

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
