package no.nav.helse.sparkel.representasjon

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.create
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RepresentasjonClientTest {
    private lateinit var wireMockServer: WireMockServer
    private lateinit var representasjonClient: RepresentasjonClient

    private val azureTokenProvider = object : AzureTokenProvider {
        override fun bearerToken(scope: String) =
            Result.Ok(AzureToken("bearer token", LocalDateTime.now().plusHours(1)))

        override fun onBehalfOfToken(scope: String, token: String) = bearerToken(scope)
    }

    private val endepunkt = "/api/internbruker/fullmakt/fullmaktsgiver"

    @BeforeEach
    fun setup() {
        wireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())

        representasjonClient = RepresentasjonClient(
            "http://localhost:${wireMockServer.port()}",
            azureTokenProvider,
            httpClient = HttpClient { install(ContentNegotiation) { jackson() } },
            scope = "scope"
        )
    }

    @Test
    fun `happy case`() {
        val svar = """ { "svar": "hei" } """

        stubFor(
            post(endepunkt).willReturn(
                okJson(jacksonObjectMapper().readTree(svar).toString())
            )
        )

        val respons = runBlocking { representasjonClient.hentFullmakt("en ident") }

        assertEquals(svar.replace(Regex("\\s"), ""), respons.getOrNull()?.toString())
        verify(postRequestedFor(urlEqualTo(endepunkt)))
    }

    @Test
    fun `sad, but retryable, case`() {
        val svar = """ { "svar": "hei" } """

        val scenario = "Feiler først, så ok"
        stubFor(
            post(endepunkt).inScenario(scenario).willReturn(
                aResponse().withStatus(500).withBody("noe gikk galt her")
            ).willSetStateTo("har feilet")
        )
        stubFor(
            post(endepunkt).inScenario(scenario).whenScenarioStateIs("har feilet").willReturn(
                okJson(jacksonObjectMapper().readTree(svar).toString())
            )
        )

        val respons = runBlocking { representasjonClient.hentFullmakt("en ident") }

        assertEquals(svar.replace(Regex("\\s"), ""), respons.getOrNull()?.toString())
        verify(2, postRequestedFor(urlEqualTo(endepunkt)))
    }
}
