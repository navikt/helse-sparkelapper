package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.helse.sparkel.aareg.objectMapper
import java.time.LocalDate
import java.util.*
import no.nav.helse.sparkel.aareg.sikkerlogg

class AaregClient(
    private val baseUrl: String,
    private val tokenSupplier: () -> String,
    private val httpClient: HttpClient = HttpClient()
) {
    suspend fun hentFraAareg(
        fnr: String,
        callId: UUID
    ): ArrayNode {
        val response = httpClient.prepareGet(aaregUrl) {
            header("Authorization", "Bearer ${tokenSupplier()}")
            System.getenv("NAIS_APP_NAME")?.also { header("Nav-Consumer-Id", it) }
            header("Nav-Call-Id", callId)
            accept(ContentType.Application.Json)
            header("Nav-Personident", fnr)
        }.execute()
        // if (erDev()) sikkerlogg.debug("Respons fra v2:\n\t{}", response.bodyAsText())

        sikkerlogg.info("AaregResponse status: " + response.status)
        val responseValue = objectMapper.readTree(response.bodyAsText())
        if (!responseValue.isArray) throw AaregException(responseValue.path("melding").asText("Ukjent respons fra Aareg"), responseValue)
        return responseValue as ArrayNode
    }

    private fun erDev() = "dev-fss" == System.getenv("NAIS_CLUSTER_NAME")
    private val aaregUrl by lazy {
        val versjon = "v1" // if (erDev()) "v2" else "v1"
        "$baseUrl/$versjon/arbeidstaker/arbeidsforhold"
    }
}

class AaregException(message: String, private val responseValue: JsonNode) : RuntimeException(message) {
    fun responseValue() = responseValue
}

data class Arbeidsforhold(
    val orgnummer: String,
    val ansattSiden: LocalDate,
    val ansattTil: LocalDate?
)

internal fun JsonNode.asOptionalLocalDate() =
    takeIf(JsonNode::isTextual)?.asText()?.takeIf(String::isNotEmpty)?.let { LocalDate.parse(it.substring(0, 10)) }

internal fun JsonNode.asLocalDate() =
    asText().substring(0, 10).let { LocalDate.parse(it) }