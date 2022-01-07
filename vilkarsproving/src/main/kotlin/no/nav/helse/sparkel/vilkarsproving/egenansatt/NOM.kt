package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

class NOM(
    private val aad: AzureAD?, // Nullable inntil vi har byttet over til NOM
    private val baseUrl: URL,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {

    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun erEgenAnsatt(fødselsnummer: String, behovId: String): Boolean {
        val accessToken = aad?.accessToken() ?: throw RuntimeException("Kan ikke få tak i access_token.")

        val body = objectMapper.writeValueAsString(
            NomQuery(query = organisasjonsenhetQuery, variables = Variables(fødselsnummer))
        )

        val request = HttpRequest.newBuilder(URI.create("$baseUrl/graphql"))
            .header("TEMA", "SYK")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Nav-Call-Id", behovId)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val responseHandler = HttpResponse.BodyHandlers.ofString()

        val response = httpClient.send(request, responseHandler)
        if (response.statusCode() != 200) {
            throw RuntimeException("error (responseCode=${response.statusCode()}) from NOM")
        }
        val responseBody = objectMapper.readTree(response.body())
        if (responseBody.containsErrors()) {
            throw RuntimeException("errors from NOM: ${responseBody["errors"].errorMsgs()}")
        }
        return responseBody.erEgenAnsatt()
    }

}

private const val dollar = '$'
private val organisasjonsenhetQuery = """
    query(${dollar}fnr: String!) {
       ressurs(where: {personIdent: ${dollar}fnr}) {
          koblinger{
             organisasjonsenhet{
                navn
                gyldigFom
                gyldigTom
             }
          }
       }    
    }
""".trimIndent().replace("\n", " ")

private data class NomQuery(
    val query: String,
    val variables: Variables
)

private class Variables(
    val fnr: String
)

private fun JsonNode.containsErrors() = this.has("errors")

private fun JsonNode.errorMsgs() = with (this as ArrayNode) {
    joinToString(",") { it["message"]?.asText() ?: "unknown error" }
}

private fun JsonNode.erEgenAnsatt() = get("data")?.get("ressurs")?.get("koblinger")?.get("organisasjonsenhet")?.let { orgEnhet ->
    erAnsattNå(orgEnhet.maybeLocalDate("gyldigFom"), orgEnhet.maybeLocalDate("gyldigTom"))
} ?: false

private fun JsonNode.maybeLocalDate(key: String) =
    this[key].takeIf { it != null }?.let { LocalDate.parse(it.asText()) }

internal fun erAnsattNå(ansattFom: LocalDate?, ansattTom: LocalDate?) = LocalDate.now().let { now ->
    val harStartet = ansattFom != null && (ansattFom == now || ansattFom.isBefore(now))
    val harIkkeSluttet = ansattTom == null || (ansattTom == now || ansattTom.isAfter(now))
    harStartet && harIkkeSluttet
}
