package no.nav.helse.sparkel.aareg

import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.notFound
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ApietVårtSinTest {
    @Test
    fun `kan hente navn på organisasjon via REST-endepunkt`() {
        // Given:
        val organisasjonsnummer = Random.nextInt(800000000, 1000000000).toString()
        IntegrationTestApplikasjon.eregWireMock.stubFor(
            get(urlPathEqualTo("/api/v1/organisasjon/$organisasjonsnummer"))
                .willReturn(okJson("""{ "navn": { "navnelinje1": "Happy!" } }"""))
        )

        // When:
        val (httpStatusCode, responseJson) = callForJson("/organisasjoner/$organisasjonsnummer")

        // Then:
        assertEquals(200, httpStatusCode)
        assertEquals(organisasjonsnummer, responseJson["organisasjonsnummer"].asText())
        assertEquals("Happy!", responseJson["navn"].asText())
    }

    @Test
    fun `får HTTP 401 Unauthorized uten autentiseringstoken`() {
        // Given:
        val organisasjonsnummer = Random.nextInt(800000000, 1000000000).toString()
        IntegrationTestApplikasjon.eregWireMock.stubFor(
            get(urlPathEqualTo("/api/v1/organisasjon/$organisasjonsnummer"))
                .willReturn(okJson("""{ "navn": { "navnelinje1": "Happy!" } }"""))
        )

        // When:
        val (httpStatusCode, _) = callForJson("/organisasjoner/$organisasjonsnummer", token = null)

        // Then:
        assertEquals(401, httpStatusCode)
    }

    @Test
    fun `gir 404 hvis organisasjonsnummer ikke finnes`() {
        // Given:
        val organisasjonsnummer = Random.nextInt(800000000, 1000000000).toString()
        IntegrationTestApplikasjon.eregWireMock.stubFor(
            get(urlPathEqualTo("/api/v1/organisasjon/$organisasjonsnummer"))
                .willReturn(notFound())
        )

        // When:
        val (httpStatusCode, _) = callForJson("/organisasjoner/$organisasjonsnummer")

        // Then:
        assertEquals(404, httpStatusCode)
    }

    private val bearerAuthToken = IntegrationTestApplikasjon.mockOAuth2Server.issueToken(
        issuerId = IntegrationTestApplikasjon.ISSUER_ID,
        audience = IntegrationTestApplikasjon.CLIENT_ID,
        subject = UUID.randomUUID().toString(),
        claims = mapOf(
            "NAVident" to "a1234567",
            "preferred_username" to "nav.navesen@nav.no",
            "oid" to UUID.randomUUID().toString(),
            "name" to "Nav Navesen"
        )
    ).serialize()

    private val logger: Logger = LoggerFactory.getLogger(javaClass)

    private fun callForJson(urlPath: String, token: String? = bearerAuthToken): Pair<Int, JsonNode> =
        runBlocking {
            httpClient.get("http://localhost:${IntegrationTestApplikasjon.port}$urlPath") {
                accept(ContentType.Application.Json)
                token?.let(::bearerAuth)
            }.let { it.status to it.bodyAsText() }
        }.let { (status, bodyAsText) ->
            logger.info("Respons fra tjeneste:\nHTTP ${status.value} ${status.description}\n$bodyAsText")
            status.value to objectMapper.readTree(bodyAsText)
        }

    companion object {
        private val httpClient: HttpClient =
            HttpClient(Apache) {
                install(ContentNegotiation) {
                    register(ContentType.Application.Json, JacksonConverter())
                }
                engine {
                    socketTimeout = 0
                    connectTimeout = 1_000
                    connectionRequestTimeout = 1_000
                }
            }
    }
}
