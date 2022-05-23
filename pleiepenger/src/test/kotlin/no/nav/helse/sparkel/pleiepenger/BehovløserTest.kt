package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.util.UUID
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.pleiepenger.infotrygd.AzureClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdClient
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdService
import no.nav.helse.sparkel.pleiepenger.infotrygd.Stønadsperiode
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class BehovløserTest {

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private var sendteMeldinger = mutableListOf<JsonNode>()
    private lateinit var service: InfotrygdService

    private val rapid = object : RapidsConnection() {

        fun sendTestMessage(message: String) {
            notifyMessage(message, this)
        }

        override fun publish(message: String) {
            sendteMeldinger.add(objectMapper.readTree(message))
        }

        override fun publish(key: String, message: String) {}

        override fun rapidName(): String {
            return "Test"
        }

        override fun start() {}

        override fun stop() {}
    }

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())
        stubEksterneEndepunkt()
        service = InfotrygdService(
            InfotrygdClient(
                baseUrl = wireMockServer.baseUrl(),
                accesstokenScope = "a_scope",
                azureClient = AzureClient(
                    tokenEndpoint = "${wireMockServer.baseUrl()}/token",
                    clientId = "client_id",
                    clientSecret = "client_secret"
                )
            )
        )
        Pleiepengerløser(rapid, service)
        Omsorgspengerløser(rapid, service)
        Opplæringspengerløser(rapid, service)
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @BeforeEach
    internal fun beforeEach() {
        sendteMeldinger.clear()
    }

    @Test
    fun `løser behov for pleiepenger, omsorgspenger og opplæringspenger`() {
        testBehov(alleBehov())
        assertEquals(1, sendteMeldinger.løsning(Pleiepengerløser.behov).size)
        assertEquals(1, sendteMeldinger.løsning(Omsorgspengerløser.behov).size)
        assertEquals(1, sendteMeldinger.løsning(Opplæringspengerløser.behov).size)
    }

    @Test
    fun `løser behov for pleiepenger`() {
        testBehov(enkeltPleiepengerBehov())
        assertEquals(1, sendteMeldinger.løsning(Pleiepengerløser.behov).size)
    }

    @Test
    fun `ignorerer behov dersom feil for pleiepenger`() {
        testBehov(enkeltPleiepengerBehov("feilITjenesten"))
        assertEquals(0, sendteMeldinger.løsning(Pleiepengerløser.behov).size)
    }

    @Test
    fun `løser behov for omsorgspenger`() {
        testBehov(enkeltOmsorgspengerBehov())
        assertEquals(1, sendteMeldinger.løsning(Omsorgspengerløser.behov).size)
    }

    @Test
    fun `løser behov for opplæringspenger`() {
        testBehov(enkeltOpplæringspengerBehov())
        assertEquals(1, sendteMeldinger.løsning(Opplæringspengerløser.behov).size)
    }

    @Test
    fun `kaster ikke feil hvis ikke tilgang til Infotrygd`() {
        assertDoesNotThrow { testBehov(ikkeTilgangPleiepengerBehov()) }
        assertDoesNotThrow { testBehov(ikkeTilgangOmsorgspengerBehov()) }
        assertDoesNotThrow { testBehov(ikkeTilgangOpplæringspengerBehov()) }
    }

    private fun List<JsonNode>.løsning(behov: String) = map { it.path("@løsning").path(behov) }
        .firstOrNull { !it.isMissingNode }
        ?.map { Stønadsperiode(it) }
        ?: emptyList()

    private fun testBehov(behov: String) {
        rapid.sendTestMessage(behov)
    }

    private fun alleBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Pleiepenger", "Omsorgspenger", "Opplæringspenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "fnr",
            "Pleiepenger" : {
                "pleiepengerFom" : "2017-05-18",
                "pleiepengerTom" : "2020-05-18"
            },
            "Omsorgspenger": {
                "omsorgspengerFom" : "2017-05-18",
                "omsorgspengerTom" : "2020-05-18"
            },
            "Opplæringspenger": {
                "opplæringspengerFom" : "2017-05-18",
                "opplæringspengerTom" : "2020-05-18"
            }
        }
        """

    private fun enkeltPleiepengerBehov(fnr: String = "fnr") =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Pleiepenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "$fnr",
            "Pleiepenger": {
                "pleiepengerFom" : "2017-05-18",
                "pleiepengerTom" : "2020-05-18"
            }
        }
        """

    private fun ikkeTilgangPleiepengerBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Pleiepenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "ikkeTilgang",
            "Pleiepenger": {
                "pleiepengerFom" : "2017-05-18",
                "pleiepengerTom" : "2020-05-18"
            }
        }
        """

    private fun enkeltOmsorgspengerBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Omsorgspenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "fnr",
            "Omsorgspenger": {
                "omsorgspengerFom" : "2017-05-18",
                "omsorgspengerTom" : "2020-05-18"
            }
        }
        """

    private fun ikkeTilgangOmsorgspengerBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Omsorgspenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "ikkeTilgang",
            "Omsorgspenger": {
                "omsorgspengerFom" : "2017-05-18",
                "omsorgspengerTom" : "2020-05-18"
            }
        }
        """

    private fun enkeltOpplæringspengerBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Opplæringspenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "fnr",
            "Opplæringspenger": {
                "opplæringspengerFom" : "2017-05-18",
                "opplæringspengerTom" : "2020-05-18"
            }
        }
        """

    private fun ikkeTilgangOpplæringspengerBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "Opplæringspenger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "ikkeTilgang",
            "Opplæringspenger": {
                "opplæringspengerFom" : "2017-05-18",
                "opplæringspengerTom" : "2020-05-18"
            }
        }
        """

    private fun stubEksterneEndepunkt() {
        stubFor(
            post(urlMatching("/token"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                        "token_type": "Bearer",
                        "expires_in": 3599,
                        "access_token": "1234abc"
                    }"""
                        )
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/pleiepenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("fnr")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                      "vedtak": [
                                        {
                                          "fom": "2018-01-01",
                                          "tom": "2018-01-31",
                                          "grad": 100
                                        }
                                      ]
                                    }"""
                        )
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/pleiepenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("ikkeTilgang")))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/pleiepenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("feilITjenesten")))
                .willReturn(
                    aResponse()
                        .withStatus(500)
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/omsorgspenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("fnr")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                      "vedtak": [
                                        {
                                          "fom": "2018-01-01",
                                          "tom": "2018-01-31",
                                          "grad": 100
                                        }
                                      ]
                                    }"""
                        )
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/omsorgspenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("ikkeTilgang")))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/opplaeringspenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("fnr")))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                      "vedtak": [
                                        {
                                          "fom": "2018-01-01",
                                          "tom": "2018-01-31",
                                          "grad": 100
                                        }
                                      ]
                                    }"""
                        )
                )
        )
        stubFor(
            post(urlPathEqualTo("/vedtak/opplaeringspenger"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("identitetsnummer", equalTo("ikkeTilgang")))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                )
        )
    }
}
