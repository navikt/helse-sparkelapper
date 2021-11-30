package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate

private const val dollar = '$'

class PDL(
    private val sts: STS,
    private val baseUrl: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {
    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val log = LoggerFactory.getLogger(PDL::class.java)
    }

    internal suspend fun finnPerson(fødselsnummer: String, behovId: String): Person? = try {
        httpKall(fødselsnummer, finnPersonQuery, behovId, JsonNode::asPerson).also {
            log.info("Gjorde oppslag på person for behov $behovId")
        }
    } catch (ex: Exception) {
        log.error("Feil under oppslag av person", ex)
        null
    }

    internal suspend fun finnGeografiskTilhørighet(fødselsnummer: String, behovId: String): GeografiskTilknytning? = try {
        httpKall(fødselsnummer, geografiskTilknytningQuery, behovId, JsonNode::asGeotilknytning).also {
            log.info("Gjorde oppslag på geografisk tilhørighet for behov $behovId")
        }
    } catch (ex: Exception) {
        log.error("Feil under oppslag av geografisk tilhørighet ", ex)
        null
    }

    private suspend fun <T> httpKall(fødselsnummer: String, query: String, behovId: String, responseMapper: (JsonNode) -> T): T =
        retry(
            "pdl",
            IOException::class,
            retryIntervals = arrayOf(500L, 1000L, 3000L, 5000L, 10000L)
        ) {
            val accessToken = sts.token()

            val body =
                objectMapper.writeValueAsString(
                    PdlQuery(query = query.onOneLine(), variables = Variables(fødselsnummer))
                )

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
                throw RuntimeException("errors from PDL: ${responseBody["errors"].errorMsgs()}")
            }
            responseMapper(responseBody)
        }
}

internal data class PdlQuery(
    val query: String,
    val variables: Variables
)

internal class Variables(
    val ident: String,
    val historikk: Boolean = false
)

internal data class Person(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val fødselsdato: LocalDate,
    val kjønn: Kjønn,
    val adressebeskyttelse: Adressebeskyttelse
)

internal data class GeografiskTilknytning(
    private val land: String?,
    private val kommune: String?,
    private val bydel: String?
) {
    fun mestNøyaktig() = bydel ?: kommune ?: land ?: "ukjent"
}

enum class Adressebeskyttelse(val kode: String) {
    STRENGT_FORTROLIG_UTLAND("SPSF"), // behandles som STRENGT_FORTROLIG
    STRENGT_FORTROLIG("SPSF"),
    FORTROLIG("SPFO"),
    UGRADERT("")
}

internal fun JsonNode.asPerson() = this.get("data").get("hentPerson").let { personen ->
    val pdlNavn = personen["navn"]
    val pdlFødsel = personen["foedsel"]
    val pdlKjønn = personen["kjoenn"]
    val pdlBeskyttelse = personen["adressebeskyttelse"]
    Person(
        pdlNavn["fornavn"]?.asText() ?: "Ukjent",
        pdlNavn["mellomnavn"]?.asText(),
        pdlNavn["etternavn"]?.asText() ?: "Ukjentsen",
        LocalDate.parse(pdlFødsel["foedselsdato"]?.asText()),
        pdlKjønn["kjoenn"].asKjønn(),
        Adressebeskyttelse.valueOf(pdlBeskyttelse["gradering"]?.asText() ?: "UGRADERT")
    )
}

internal fun JsonNode.asGeotilknytning(): GeografiskTilknytning =
    this.get("data").get("hentGeografiskTilknytning").let { tilknytningen ->
        val land = tilknytningen["gtLand"]?.asText()
        val kommune = tilknytningen["gtKommune"]?.asText()
        val bydel = tilknytningen["gtBydel"]?.asText()
        GeografiskTilknytning(land, kommune, bydel)
    }

private fun JsonNode.asKjønn() = when(this.asText()) {
    "MANN" -> Kjønn.Mann
    "KVINNE" -> Kjønn.Kvinne
    "UKJENT" -> Kjønn.Ukjent
    else -> throw IllegalArgumentException("Kjenner ikke til kjønn '$this'")
}

internal fun JsonNode.containsErrors() = this.has("errors")

internal fun String.onOneLine() = this.replace("\n", " ")

private fun JsonNode.errorMsgs() = with (this as ArrayNode) {
    val errorMsgs = this.map { it["message"]?.asText() ?: "unknown error" }
    val extensions = this.map { it["extensions"]?.get("details")?.asText() ?: "extension details unknown" }
    "$errorMsgs -- $extensions"
}

private val geografiskTilknytningQuery: String = """
    query(${dollar}ident: ID!) {
       hentGeografiskTilknytning(ident: ${dollar}ident) {
          gtLand
          gtKommune
          gtBydel 
       }
    }
""".trimIndent()

private val finnPersonQuery: String = """
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
          adressebeskyttelse {
             gradering
          }
       }
    }
""".trimIndent()
