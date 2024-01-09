package no.nav.helse.sparkel.norg

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import no.nav.helse.rapids_rivers.isMissingOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import net.logstash.logback.argument.StructuredArguments.keyValue

private const val dollar = '$'

class PDL(
    private val azureClient: AzureTokenProvider?,
    private val sts: STS?,
    private val baseUrl: String,
    private val scope: String?,
    private val httpClient: HttpClient = HttpClient.newHttpClient()

) {
    private companion object {
        private val objectMapper = jacksonObjectMapper()
        private val log = LoggerFactory.getLogger(PDL::class.java)
        private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")
    }

    internal suspend fun finnAdressebeskyttelse(fødselsnummer: String, behovId: String): Adressebeskyttelse? = try {
        httpKall(fødselsnummer, finnAdressebeskyttelseQuery, behovId, JsonNode::asAdressebeskyttelse).also {
            log.info("Gjorde oppslag på adressebeskyttelse til person for behov $behovId")
        }
    } catch (ex: Exception) {
        log.error("Feil under oppslag på adressebeskyttelse til person", ex)
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
            val accessToken = azureClient?.bearerToken(scope ?: "")?.token ?: sts?.token()

            val body =
                objectMapper.writeValueAsString(
                    PdlQuery(query = query.onOneLine(), variables = Variables(fødselsnummer))
                )

            val request = HttpRequest.newBuilder(URI.create(baseUrl))
                .header("TEMA", "SYK")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("behandlingsnummer", "B139")
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
            sikkerLogg.info("Svar fra PDL for behov $behovId")
            try {
                responseMapper(responseBody)
            } catch (exception: Exception) {
                sikkerLogg.error("Feil ved mapping av response fra PDL. Response:\n\t$responseBody",
                    keyValue("behovId", behovId),
                    keyValue("fødselsnummer", fødselsnummer),
                    exception
                )
                throw exception
            }
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

internal fun JsonNode.asAdressebeskyttelse() = this.get("data").get("hentPerson").let { personen ->
    val adresseBeskyttelse = (personen["adressebeskyttelse"] as ArrayNode)
        .map { Adressebeskyttelse.valueOf(it["gradering"]?.asText() ?: "UGRADERT") }
        .sorted()
        .firstOrNull()
    adresseBeskyttelse ?: Adressebeskyttelse.UGRADERT
}

internal fun JsonNode.asGeotilknytning(): GeografiskTilknytning =
    this.get("data").get("hentGeografiskTilknytning").let { tilknytningen ->
        val land = tilknytningen["gtLand"].takeUnless { it.isMissingOrNull() }?.asText()
        val kommune = tilknytningen["gtKommune"].takeUnless { it.isMissingOrNull() }?.asText()
        val bydel = tilknytningen["gtBydel"].takeUnless { it.isMissingOrNull() }?.asText()
        GeografiskTilknytning(land, kommune, bydel)
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

private val finnAdressebeskyttelseQuery: String = """
    query(${dollar}ident: ID!) {
       hentPerson(ident: ${dollar}ident) {
          adressebeskyttelse {
             gradering
          }
       }
    }
""".trimIndent()


