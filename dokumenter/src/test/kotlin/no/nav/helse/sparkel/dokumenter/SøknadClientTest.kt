package no.nav.helse.sparkel.dokumenter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.result_object.Result
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.create
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.okJson
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathTemplate
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class SøknadClientTest {
    private var wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private var søknadClient: SøknadClient

    private val azureTokenProvider = object : AzureTokenProvider {
        override fun bearerToken(scope: String) =
            Result.Ok(AzureToken("bearer token", LocalDateTime.now().plusHours(1)))

        override fun onBehalfOfToken(scope: String, token: String) = bearerToken(scope)
    }

    init {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())

        søknadClient = SøknadClient(
            "http://localhost:${wireMockServer.port()}",
            azureTokenProvider,
            httpClient = HttpClient { install(ContentNegotiation) { jackson() } },
            scope = "scope"
        )
    }

    private val endpoint = "/api/v3/soknader/{dokumentId}/kafkaformat"

    @Test
    fun `happy case`() {
        val svar = """ { "søknadId": "123" } """

        stubFor(
            get(urlPathTemplate(endpoint)).willReturn(
                okJson(jacksonObjectMapper().readTree(svar).toString())
            )
        )

        val respons = runBlocking { søknadClient.hentDokument("en_id") }

        assertEquals(svar.replace(Regex("\\s"), ""), respons.getOrNull()?.toString())
        verify(getRequestedFor(urlPathTemplate(endpoint)))
    }

    @Test
    fun `sad case at first, but then happy`() {
        val svar = """ { "søknadId": "123" } """

        val scenario = "Feiler først, så ok"
        stubFor(
            get(urlPathTemplate(endpoint)).inScenario(scenario).willReturn(
                aResponse().withStatus(500).withBody("noe gikk galt her")
            ).willSetStateTo("har feilet")
        )
        stubFor(
            get(urlPathTemplate(endpoint)).inScenario(scenario).whenScenarioStateIs("har feilet").willReturn(
                okJson(jacksonObjectMapper().readTree(svar).toString())
            )
        )

        val respons = runBlocking { søknadClient.hentDokument("en_id") }

        assertEquals(svar.replace(Regex("\\s"), ""), respons.getOrNull()?.toString())
        verify(2, getRequestedFor(urlPathTemplate(endpoint)))
    }
}
