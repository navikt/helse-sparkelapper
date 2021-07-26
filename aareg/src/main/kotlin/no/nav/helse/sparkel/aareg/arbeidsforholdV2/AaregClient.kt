package no.nav.helse.sparkel.aareg.arbeidsforholdV2

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.sparkel.aareg.objectMapper
import java.time.LocalDate
import java.util.*


class AaregClient(
    private val baseUrl: String,
    private val stsRestClient: StsRestClient,
    private val httpClient: HttpClient = HttpClient()
) {
    suspend fun hentFraAareg(
        fnr: String,
        callId: UUID
    ) = httpClient.get<HttpStatement>("$baseUrl/v1/arbeidstaker/arbeidsforhold") {
        header("Authorization", "Bearer ${stsRestClient.token()}")
        header("Nav-Consumer-Token", "Bearer ${stsRestClient.token()}")
        System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
        header("Nav-Call-Id", callId)
        accept(ContentType.Application.Json)
        header("Nav-Personident", fnr)
    }
        .execute { objectMapper.readValue<ArrayNode>(it.readText()) }
}

data class Arbeidsforhold(
    val orgnummer: String,
    val ansattSiden: LocalDate,
    val ansattTil: LocalDate?
)

internal fun JsonNode.asOptionalLocalDate() =
    takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)?.let { LocalDate.parse(it) }

internal fun JsonNode.asLocalDate() =
    asText().substring(0, 10).let { LocalDate.parse(it) }
