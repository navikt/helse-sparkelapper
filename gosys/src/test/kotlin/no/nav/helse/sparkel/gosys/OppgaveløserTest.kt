package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.create
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle

@TestInstance(Lifecycle.PER_CLASS)
internal class OppgaveløserTest {

    private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private val speedClient = mockk<SpeedClient> {
        every { hentFødselsnummerOgAktørId(any(), any()) } returns IdentResponse(
            fødselsnummer = "fnr",
            aktørId = "aktørId",
            npid = null,
            kilde = IdentResponse.KildeResponse.PDL
        ).ok()
    }
    private val azureClient = object : AzureTokenProvider {
        override fun bearerToken(scope: String) = AzureToken("token", LocalDateTime.now()).ok()
        override fun onBehalfOfToken(scope: String, token: String): Result<AzureToken> {
            TODO("Not yet implemented")
        }
    }
    private lateinit var service: OppgaveService
    private val rapid = TestRapid()

    private val sendtMelding get() = rapid.inspektør.let {
        it.message(it.size - 1)
    }

    private val okBehov = UUID.randomUUID()
    private val feilendeBehov = UUID.randomUUID()

    @BeforeAll
    fun setup() {
        wireMockServer.start()
        configureFor(create().port(wireMockServer.port()).build())
        stubEksterneEndepunkt()
        service = OppgaveService(
            OppgaveClient(
                baseUrl = wireMockServer.baseUrl(),
                scope = "oppgave-scope",
                azureClient = azureClient
             )
        )
    }

    @AfterAll
    internal fun teardown() {
        wireMockServer.stop()
    }

    @BeforeEach
    internal fun beforeEach() {
        rapid.reset()
    }

    @Test
    fun `løser behov`() {
        testBehov(enkeltBehov())

        assertEquals(1, sendtMelding.antall())
        assertFalse(sendtMelding.oppslagFeilet())
    }

    @Test
    fun `løser behov hvor oppslag feiler`() {
        testBehov(behovSomFeiler())

        assertNull(sendtMelding.antall())
        assertTrue(sendtMelding.oppslagFeilet())
    }

    private fun JsonNode.antall() = this.path("@løsning").path(Oppgaveløser.behov).path("antall").takeUnless { it.isNull }?.asInt()
    private fun JsonNode.oppslagFeilet() = this.path("@løsning").path(Oppgaveløser.behov).path("oppslagFeilet").asBoolean()

    private fun testBehov(behov: String) {
        Oppgaveløser(rapid, service, speedClient)
        rapid.sendTestMessage(behov)
    }

    private fun enkeltBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "ÅpneOppgaver" ],
            "@id" : "$okBehov",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "ÅpneOppgaver": {
                "ikkeEldreEnn" : "2023-12-12"
            }
        }
        """

    private fun behovSomFeiler() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "ÅpneOppgaver" ],
            "@id" : "$feilendeBehov",
            "@opprettet" : "2020-05-18",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "ÅpneOppgaver": {
                "ikkeEldreEnn" : "2023-12-12"
            }
        }
        """

    private fun stubEksterneEndepunkt() {
        stubFor(
            get(urlPathEqualTo("/api/v1/oppgaver"))
                .withQueryParam("aktoerId", equalTo("aktørId"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(okBehov.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                            """{
                                      "antallTreffTotalt": 1,
                                      "oppgaver": [
                                            {
                                                  "id": 5436732,
                                                  "tildeltEnhetsnr": "0100",
                                                  "endretAvEnhetsnr": "0101",
                                                  "opprettetAvEnhetsnr": "0200",
                                                  "journalpostId": "84938201",
                                                  "journalpostkilde": "AS36",
                                                  "behandlesAvApplikasjon": "FS22",
                                                  "saksreferanse": "84942299",
                                                  "bnr": "11250199559",
                                                  "samhandlernr": "80000999999",
                                                  "aktoerId": "1234567891113",
                                                  "identer": 12345678911,
                                                  "orgnr": "979312059",
                                                  "tilordnetRessurs": "Z998323",
                                                  "beskrivelse": "string",
                                                  "temagruppe": "ANSOS",
                                                  "tema": "AAP",
                                                  "behandlingstema": "ab0203",
                                                  "oppgavetype": "HAST_BANK_OPPLYS",
                                                  "behandlingstype": "ae0001",
                                                  "versjon": 1,
                                                  "mappeId": 848,
                                                  "opprettetAv": "string",
                                                  "endretAv": "string",
                                                  "prioritet": "HOY",
                                                  "status": "UNDER_BEHANDLING",
                                                  "metadata": {
                                                    "additionalProp1": "string",
                                                    "additionalProp2": "string",
                                                    "additionalProp3": "string"
                                                  },
                                                  "fristFerdigstillelse": {},
                                                  "aktivDato": {},
                                                  "opprettetTidspunkt": "2020-10-07T07:58:47.791Z",
                                                  "ferdigstiltTidspunkt": "2020-10-07T07:58:47.791Z",
                                                  "endretTidspunkt": "2020-10-07T07:58:47.791Z"
                                                }
                                      ]
                                    }
                                    """
                        )
                )
        )
        stubFor(
            get(urlPathEqualTo("/api/v1/oppgaver"))
                .withHeader("Accept", equalTo("application/json"))
                .withHeader("X-Correlation-ID", equalTo(feilendeBehov.toString()))
                .willReturn(
                    aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                )
        )
    }
}
