package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SøknadClient(
    private val baseUrl: String,
): DokumentClient {
    companion object {
        private val objectMapper = ObjectMapper()
        private val httpClient = HttpClient.newHttpClient()
    }
    override fun hentDokument(dokumentid: String): JsonNode {
        val request = HttpRequest.newBuilder(URI.create(baseUrl + "/api/v3/soknader/$dokumentid/kafkaformat"))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .GET()
            .build()

        val responseHandler = HttpResponse.BodyHandlers.ofString()

        val response = httpClient.send(request, responseHandler)
        response.statusCode().let {
            if (it >= 300) throw RuntimeException("error (responseCode=$it) from Flex")
        }
        return objectMapper.readTree(response.body())
    }
}
