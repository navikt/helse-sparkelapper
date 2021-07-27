package no.nav.helse.sparkel.ereg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
    ) =
        httpClient.get<HttpStatement>("$baseUrl/v1/organisasjon/$organisasjonsnummer?inkluderHierarki=true&inkluderHistorikk=true") {
            header("Authorization", "Bearer ${stsRestClient.token()}")
            header("Nav-Consumer-Token", "Bearer ${stsRestClient.token()}")
            header("Nav-Consumer-Id", appName)
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
        }
            .execute { it.readText() }
            .let<String, JsonNode>(objectMapper::readValue)
            .let { response ->
                EregResponse(
                    navn = trekkUtNavn(response),
                    næringer = response["organisasjonDetaljer"]["naeringer"].map { it["naeringskode"].asText() }
                )
            }

    private fun trekkUtNavn(organisasjon: JsonNode) =
        organisasjon["navn"].let { navn ->
            (1..5).map { index ->
                navn["navnelinje$index"].asText()
            }.filterNot(String::isBlank)
        }.joinToString()

}

data class EregResponse(
    val navn: String,
    val næringer: List<String>,
)
