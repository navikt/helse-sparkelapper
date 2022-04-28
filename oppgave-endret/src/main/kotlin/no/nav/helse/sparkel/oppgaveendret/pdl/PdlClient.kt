package no.nav.helse.sparkel.oppgaveendret.pdl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PdlClient(
    private val baseUrl: String,
    private val stsClient: StsRestClient
) {

    companion object {
        private val objectMapper = ObjectMapper()
        private val httpClient = HttpClient.newHttpClient()
        private val hentIdenterQuery = this::class.java.getResource("/pdl/hentIdenter.graphql").readText().replace(Regex("[\n\r]"), "")
    }

    private fun request(
        ident: String,
        callId: String,
        query: String
    ): JsonNode {
        val stsToken = stsClient.token()

        val body = objectMapper.writeValueAsString(PdlQueryObject(query, Variables(ident)))

        val request = HttpRequest.newBuilder(URI.create(baseUrl))
            .header("TEMA", "SYK")
            .header("Authorization", "Bearer $stsToken")
            .header("Nav-Consumer-Token", "Bearer $stsToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Nav-Call-Id", callId)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val responseHandler = HttpResponse.BodyHandlers.ofString()

        val response = httpClient.send(request, responseHandler)
        response.statusCode().let {
            if (it >= 300) throw RuntimeException("error (responseCode=$it) from PDL")
        }
        return objectMapper.readTree(response.body())
    }

    internal fun hentIdenter(
        ident: String,
        callId: String
    ) = PdlOversetter.oversetterIdenter(request(ident, callId, hentIdenterQuery))
}