package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class SkjermedePersoner(
    private val aadAccessToken: String,
    private val baseUrl: URL,
    private val httpClient: HttpClient = HttpClient.newHttpClient()
) {

    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    internal fun erSkjermetPerson(fødselsnummer: String, behovId: String): Boolean {

        val requestBody = objectMapper.writeValueAsString(
            SkjermetDataRequestDTO(fødselsnummer)
        )

        val request = HttpRequest.newBuilder(URI.create("$baseUrl/skjermet"))
            .header("Authorization", "Bearer $aadAccessToken")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Nav-Call-Id", behovId)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(30))
            .build()

        val responseHandler = HttpResponse.BodyHandlers.ofString()

        val response = httpClient.send(request, responseHandler)

        if (response.statusCode() != 200) {
            throw RuntimeException("error (responseCode=${response.statusCode()}) from Skjerming")
        }

        return objectMapper.readTree(response.body()).asBoolean()
    }

}

private data class SkjermetDataRequestDTO(
    val personident: String
)
