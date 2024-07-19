package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import java.util.UUID
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.retry

class EregClient(
    private val baseUrl: String,
    private val appName: String,
    private val httpClient: HttpClient
) {
    suspend fun hentNavnOgNæringForOrganisasjon(
        organisasjonsnummer: String,
        callId: UUID,
    ) = mapNavnOgNæring(hentFraEreg(organisasjonsnummer, callId))

    suspend fun hentOverOgUnderenheterForOrganisasjon(organisasjonsnummer: String, callId: UUID) =
        mapOverOgUnderEnheter(hentFraEreg(organisasjonsnummer, callId))

    private suspend fun hentFraEreg(
        organisasjonsnummer: String,
        callId: UUID,
    ): HttpResponse = retry("ereg") {
        val response: HttpResponse =
            httpClient.get("$baseUrl/api/v1/organisasjon/$organisasjonsnummer?inkluderHierarki=true&inkluderHistorikk=true") {
                header("Nav-Consumer-Id", appName)
                header("Nav-Call-Id", callId)
                accept(ContentType.Application.Json)
            }
        sikkerlogg.info("EregResponse: ${response.status}\n${response.bodyAsText()}")
        if (!response.status.isSuccess()) throw FeilVedHenting("ereg svarte med ${response.status.value}")
        response
    }

    private suspend fun mapOverOgUnderEnheter(response: HttpResponse): OverOgUnderenheter {
        return objectMapper.readTree(response.bodyAsText()).let { json ->
            OverOgUnderenheter(
                overenheter = trekkUtJuridiskeEnheter(json),
                underenheter = emptyList()
            )
        }
    }

    private fun trekkUtJuridiskeEnheter(json: JsonNode): List<Enhet> {
        return json["inngaarIJuridiskEnheter"].map { enhet ->
            Enhet(
                orgnummer = enhet["organisasjonsnummer"].asText(),
                navn = trekkUtNavn(enhet)
            )
        }
    }


    private suspend fun mapNavnOgNæring(response: HttpResponse) =
        objectMapper.readTree(response.bodyAsText()).let { json ->
            NavnOgNæring(
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

data class NavnOgNæring(
    val navn: String,
    val næringer: List<String>,
)

data class OverOgUnderenheter(
    val overenheter: List<Enhet>,
    val underenheter: List<Enhet>
)

data class Enhet(
    val orgnummer: String,
    val navn: String
)

class FeilVedHenting(msg: String) : RuntimeException(msg)
