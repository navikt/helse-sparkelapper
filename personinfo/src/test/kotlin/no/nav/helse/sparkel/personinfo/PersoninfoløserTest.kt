package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.create
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.helse.rapids_rivers.RapidsConnection
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class PersoninfoløserTest {

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private val emptyNode = objectMapper.createObjectNode()
    private lateinit var sendtLøsning: JsonNode
    private lateinit var service: PersoninfoService

    private val rapid = object : RapidsConnection() {

        fun sendTestMessage(message: String) {
            listeners.forEach { it.onMessage(message, this) }
        }

        override fun publish(message: String) {
            sendtLøsning = objectMapper.readTree(message)
        }

        override fun publish(key: String, message: String) {}

        override fun start() {}
        override fun stop() {}
    }

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())
        stubSts()
        service = PersoninfoService(
            PdlClient(
                baseUrl = "${wireMockServer.baseUrl()}/graphql",
                stsClient = StsRestClient(
                    baseUrl = wireMockServer.baseUrl(),
                    serviceUser = ServiceUser("", "")
                )
            )
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @BeforeEach
    internal fun beforeEach() {
        sendtLøsning = emptyNode
    }

    @Test
    fun `løser behov`() {
        stubOkPdlRespons()

        testBehov(enkeltBehov())

        val løsning = sendtLøsning.personinfoløsning()

        assertEquals("1962-07-08", løsning["dødsdato"].asText())
    }

    @Test
    fun `feiler hvis ikke tilgang til PDL`() {
        stub401PdlRespons()

        testBehov(ikkeTilgangBehov())

        assertEquals(emptyNode, sendtLøsning)
    }

    private fun JsonNode.personinfoløsning() = this.path("@løsning").path(Personinfoløser.behov)

    private fun testBehov(behov: String) {
        Personinfoløser(rapid, service)
        rapid.sendTestMessage(behov)
    }

    private fun enkeltBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Dødsinfo" ],
            "@id" : "id",
            "@opprettet" : "2020-05-18",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "fnr"
        }
        """

    private fun ikkeTilgangBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Dødsinfo" ],
            "@id" : "id",
            "@opprettet" : "2020-05-18",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "ikkeTilgang"
        }
        """

    private fun stubSts() {
        stubFor(
            get(urlPathEqualTo("/rest/v1/sts/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "token_type": "Bearer",
                                "expires_in": 3599,
                                "access_token": "1234abc"
                            }
                            """
                        )
                )
        )
    }

    private fun stubOkPdlRespons() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Nav-Call-Id", equalTo("id"))

                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """
                            {
                                "data": {
                                    "hentPerson": {
                                        "doedsfall": [
                                            {
                                                "doedsdato": "1962-07-08",
                                                "metadata": {
                                                    "master": "Freg"
                                                }
                                            }
                                        ]
                                    }
                                }
                            }
                            """
                        )
                )
        )
    }

    private fun stub401PdlRespons() {
        stubFor(
            post(urlPathEqualTo("/graphql"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Nav-Call-Id", equalTo("id"))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                )
        )
    }
}
