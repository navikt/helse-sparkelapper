package no.nav.helse.sparkel.vilkarsproving.egenansatt

import com.fasterxml.jackson.databind.DeserializationFeature
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
import io.ktor.client.HttpClient
import java.net.URL
import java.time.Duration
import kotlinx.coroutines.runBlocking

class SkjermedePersoner(
    private val tokenSupplier: () -> String,
    private val baseUrl: URL,
    private val ktorHttpClient: HttpClient = HttpClient {
        install(JsonFeature)
        {
            serializer = JacksonSerializer {
                registerKotlinModule()
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(60).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(60).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(60).toMillis()
        }
    }
) {

    internal fun erSkjermetPerson(fødselsnummer: String, behovId: String): Boolean =
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
                    val response = httpResponse.call.response.receive<Boolean>()
                    return@runBlocking response
                }
                else -> {
                    throw RuntimeException("error (responseCode=${httpResponse.status.value}) from Skjerming")
                }
            }
        }
}

private data class SkjermetDataRequestDTO(
    val personident: String
)

