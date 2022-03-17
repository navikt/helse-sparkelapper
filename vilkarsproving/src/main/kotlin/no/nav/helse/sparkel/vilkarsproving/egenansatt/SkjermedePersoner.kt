package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.call.receive
import io.ktor.client.features.HttpTimeout
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.HttpClient as KtorHttpClient
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

class SkjermedePersoner(
    private val tokenSupplier: () -> String,
    private val baseUrl: URL,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
    private val ktorHttpClient: KtorHttpClient = KtorHttpClient {
        install(JsonFeature)
        {
            serializer = JacksonSerializer {
                registerKotlinModule()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10000
            requestTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }
) {

    private companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    internal fun erSkjermetPerson(fødselsnummer: String, behovId: String): Boolean {

        testerSkjermetPersonMedKtorKlient(behovId, fødselsnummer)

        val requestBody = objectMapper.writeValueAsString(
            SkjermetDataRequestDTO(fødselsnummer)
        )

        val request = HttpRequest.newBuilder(URI.create("$baseUrl/skjermet"))
            .header("Authorization", "Bearer ${tokenSupplier()}")
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Nav-Call-Id", behovId)
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .timeout(Duration.ofSeconds(60))
            .build()

        val responseHandler = HttpResponse.BodyHandlers.ofString()

        val response = httpClient.send(request, responseHandler)

        if (response.statusCode() != 200) {
            throw RuntimeException("error (responseCode=${response.statusCode()}) from Skjerming")
        }

        return objectMapper.readTree(response.body()).asBoolean()
    }

    private fun testerSkjermetPersonMedKtorKlient(behovId: String, fødselsnummer: String) {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

        try {
            runBlocking {
                val httpResponse = ktorHttpClient.post<HttpStatement>("$baseUrl/skjermet") {
                    header("Authorization", "Bearer ${tokenSupplier()}")
                    header("Nav-Call-Id", behovId)
                    accept(ContentType.Application.Json)
                    contentType(ContentType.Application.Json)
                    body = SkjermetDataRequestDTO(fødselsnummer)
                }.execute()
                when (httpResponse.status.value) {
                    200 -> {
                        val response: Boolean = httpResponse.call.response.receive()
                        sikkerlogg.info("ktorHttpClient kallet fungerte fikk dette som svar: " + response)
                    }
                    else -> {
                        sikkerlogg.warn("Statuskode: ${httpResponse.status.description} feil på oppslag mot skjermet")
                    }
                }
            }
        } catch (exception: Exception) {
            sikkerlogg.warn("ktorHttpClient feilet på oppslag mot skjermet", exception)
        }
    }

}

private data class SkjermetDataRequestDTO(
    val personident: String
)

