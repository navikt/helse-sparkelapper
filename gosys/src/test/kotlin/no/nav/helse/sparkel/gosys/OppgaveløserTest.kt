package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.azure.AzureToken
import com.github.navikt.tbd_libs.azure.AzureTokenProvider
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.configureFor
import com.github.tomakehurst.wiremock.client.WireMock.create
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.junit5.WireMockExtension
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class OppgaveløserTest {

    val endepunkt = "/api/v1/oppgaver"

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
    private val rapid = TestRapid()

    private val sistSendteMelding
        get() = rapid.inspektør.let {
            it.message(it.size - 1)
        }

    companion object {
        @RegisterExtension
        val wireMock: WireMockExtension = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build()
    }

    @BeforeEach
    fun setup() {
        configureFor(create().port(wireMock.runtimeInfo.httpPort).build())
        stubOppgaveendepunkt()
        val service = OppgaveService(
            OppgaveClient(
                baseUrl = wireMock.runtimeInfo.httpBaseUrl,
                scope = "oppgave-scope",
                azureClient = azureClient
            )
        )
        Oppgaveløser(rapid, service, speedClient)
    }

    @Test
    fun `løser behov`() {
        rapid.sendTestMessage(behov(Scenarioer.behovIdOk.first))

        assertEquals(1, sistSendteMelding.antall())
        assertFalse(sistSendteMelding.oppslagFeilet())
    }

    @Test
    fun `løser 401 som oppslag feilet`() {
        rapid.sendTestMessage(behov(Scenarioer.behovIdForUautentisertKall.first))

        assertNull(sistSendteMelding.antall())
        assertTrue(sistSendteMelding.oppslagFeilet())
    }

    @Test
    fun `retryer behov når responsen ikke er gyldig JSON`() {
        rapid.sendTestMessage(behov(Scenarioer.behovIdForUgyldigRespons.first))
        assertEquals(1, sistSendteMelding.antall())
        assertFalse(sistSendteMelding.oppslagFeilet())
        verify(
            2,
            getRequestedFor(urlPathEqualTo(endepunkt))
                .withHeader("X-Correlation-ID", equalTo(Scenarioer.behovIdForUgyldigRespons.first.toString()))
        )
    }

    private fun JsonNode.antall() = this.path("@løsning").path(Oppgaveløser.behov).path("antall").takeUnless { it.isNull }?.asInt()
    private fun JsonNode.oppslagFeilet() = this.path("@løsning").path(Oppgaveløser.behov).path("oppslagFeilet").asBoolean()

    private fun behov(behovId: UUID) =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "ÅpneOppgaver" ],
            "@id" : "$behovId",
            "@opprettet" : "${LocalDateTime.now()}",
            "hendelseId" : "hendelseId",
            "fødselsnummer" : "fnr",
            "ÅpneOppgaver": {
                "ikkeEldreEnn" : "2023-12-12"
            }
        }
        """

    object Scenarioer {
        val behovIdOk = UUID.randomUUID() to aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody(
                """
                {
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
        val behovIdForUautentisertKall = UUID.randomUUID() to aResponse()
            .withStatus(401)
            .withHeader("Content-Type", "application/json")

        val behovIdForUgyldigRespons = UUID.randomUUID() to aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBody("ikke gyldig JSON")
    }

    private fun stubOppgaveendepunkt() {
        Scenarioer.behovIdOk.let { (behovId, respons) ->
            stubFor(
                get(urlPathEqualTo(endepunkt))
                    .withQueryParam("aktoerId", equalTo("aktørId"))
                    .withHeader("X-Correlation-ID", equalTo(behovId.toString()))
                    .willReturn(respons)
            )
        }

        Scenarioer.behovIdForUautentisertKall.let { (behovId, respons) ->
            stubFor(
                get(urlPathEqualTo(endepunkt))
                    .withHeader("X-Correlation-ID", equalTo(behovId.toString()))
                    .willReturn(respons)
            )
        }

        Scenarioer.behovIdForUgyldigRespons.let { (behovId, respons) ->
            val scenarioUgyldigRespons = "ugyldig respons"
            stubFor(
                get(urlPathEqualTo(endepunkt))
                    .inScenario(scenarioUgyldigRespons)
                    .withHeader("X-Correlation-ID", equalTo(behovId.toString()))
                    .willReturn(respons)
                    .willSetStateTo("har feilet")
            )
            stubFor(
                get(urlPathEqualTo(endepunkt))
                    .inScenario(scenarioUgyldigRespons)
                    .withHeader("X-Correlation-ID", equalTo(behovId.toString()))
                    .whenScenarioStateIs("har feilet")
                    .willReturn(
                        Scenarioer.behovIdOk.second
                    )
            )
        }
    }
}
