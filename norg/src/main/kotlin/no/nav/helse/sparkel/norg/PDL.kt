package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

private const val dollar = '$'

internal class PDL(
    private val sts: STS,
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {
    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    internal suspend fun finnPerson(fødselsnummer: String, behovId: String): Person? = try {
        retry(
            "pdl_hent_person",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) {
            val accessToken = sts.token()

            val body = objectMapper.writeValueAsString(PdlQuery(variables = Variables(fødselsnummer)))

            val request = HttpRequest.newBuilder(URI.create(baseUrl))
                .header("TEMA", "SYK")
                .header("Authorization", "Bearer $accessToken")
                .header("Nav-Consumer-Token", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Nav-Call-Id", behovId)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build()

            val responseHandler = HttpResponse.BodyHandlers.ofString()

            val response = httpClient.send(request, responseHandler)
            if (response.statusCode() != 200) {
                throw RuntimeException("error (responseCode=${response.statusCode()}) from PDL")
            }
            val responseBody = objectMapper.readTree(response.body())
            if (responseBody.containsErrors()) {
                throw RuntimeException("errors from PDL: ${responseBody.errorMsgs()}")
            }
            responseBody["data"].get("hentPerson").toPerson()
        }
    } catch (ex: Exception) {
        null
    }

}

internal class PdlQuery(
    val query: String = """
        query(${dollar}ident: ID!) {
           hentPerson(ident: ${dollar}ident) {
              navn(historikk: false) {
                 fornavn mellomnavn etternavn
              }
              foedsel {
                 foedselsdato
              }
              kjoenn {
                 kjoenn
              }
           }
        }

    """.trimIndent(),
    val variables: Variables
)

internal class Variables(
    val ident: String,
    val historikk: Boolean = false
)

internal data class Person(
    private val fornavn: String,
    private val mellomnavn: String?,
    private val etternavn: String,
    private val fødselsdato: LocalDate,
    private val kjønn: Kjønn
)

internal fun JsonNode.toPerson(): Person {
    val person = this.get("data").get("hentPerson")
    val pdlNavn = person["navn"]
    val pdlFødsel = person["foedsel"]
    val pdlKjønn = person["kjoenn"]
    return Person(
        pdlNavn["fornavn"]?.asText() ?: "Ukjent",
        pdlNavn["mellomnavn"]?.asText(),
        pdlNavn["etternavn"]?.asText() ?: "Ukjentsen",
        LocalDate.parse(pdlFødsel["foedselsdato"]?.asText()),
        pdlKjønn["kjoenn"].toKjønn()
    )
}

private fun JsonNode.toKjønn() = when(this.asText()) {
    "MANN" -> Kjønn.Mann
    "KVINNE" -> Kjønn.Kvinne
    "UKJENT" -> Kjønn.Ukjent
    else -> throw IllegalArgumentException("Kjenner ikke til kjønn '$this'")
}

internal fun JsonNode.containsErrors() = this.has("errors")

private fun JsonNode.errorMsgs() = with (this as ArrayNode) {
    this.map { it["message"]?.asText() ?: "unknown error" }
}
