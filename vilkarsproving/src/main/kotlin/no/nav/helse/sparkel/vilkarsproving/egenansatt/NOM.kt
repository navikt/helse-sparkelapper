package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

class NOM(
    private val aad: AzureAD,
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {

    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")
    }

    internal fun erEgenAnsatt(fødselsnummer: String, behovId: String): Boolean {
        val accessToken = aad.accessToken()

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
        sikkerLogg.info("Svar fra NOM for behov $behovId")
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
    val ident: String
)

private fun JsonNode.containsErrors() = this.has("errors")

private fun JsonNode.errorMsgs() = with (this as ArrayNode) {
    map { it["message"]?.asText() ?: "unknown error" }?.joinToString(",")
}

private fun JsonNode.erEgenAnsatt() = get("data")?.get("ressurs")?.get("koblinger")?.get("organisasjonsenhet")?.let { orgEnhet ->
    erAnsattNå(orgEnhet.maybeLocalDate("gyldigFom"), orgEnhet.maybeLocalDate("gyldigTom"))
} ?: false

private fun JsonNode.maybeLocalDate(key: String) =
    if(this.hasNonNull(key)) LocalDate.parse(this[key].asText()) else null

internal fun erAnsattNå(ansattFom: LocalDate?, ansattTom: LocalDate?) = LocalDate.now().let { now ->
    val harStartet = ansattFom != null && ansattFom.isBefore(now)
    val harIkkeSluttet = ansattTom == null || ansattTom.isAfter(now)
    harStartet && harIkkeSluttet
}