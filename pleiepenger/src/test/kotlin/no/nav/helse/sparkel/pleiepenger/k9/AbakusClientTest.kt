package no.nav.helse.sparkel.pleiepenger.k9

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import java.net.URL
import java.time.LocalDate
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AbakusClientTest {

    private lateinit var server: WireMockServer
    private lateinit var client: AbakusClient

    @BeforeAll
    fun beforeAll() {
        server = WireMockServer(WireMockConfiguration.options().dynamicPort())
        server.start()
        WireMock.configureFor(server.port())
        mockAbakus(fnr, fom, tom)
        client = AbakusClient(
            url = server.abakusUrl(),
            accessTokenClient = object : AccessTokenClient {
                override fun accessToken() = "abakus-access-token"
            }
        )
    }

    @AfterAll
    fun afterAll() = server.stop()

    @Test
    fun `hente pleiepenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-06-01"), grad = 91),
            Stønadsperiode(fom = LocalDate.parse("2018-07-01"), tom = LocalDate.parse("2018-12-31"), grad = 50)
        ), client.pleiepenger(fnr, fom, tom))
    }

    @Test
    fun `hente omsorgspenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-12-31"), grad = 100),
            Stønadsperiode(fom = LocalDate.parse("2020-01-01"), tom = LocalDate.parse("2020-12-31"), grad = 69)
        ), client.omsorgspenger(fnr, fom, tom))
    }

    @Test
    fun `hente opplæringspenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-12-31"), grad = 12),
        ), client.opplæringspenger(fnr, fom, tom))
    }

    @Test
    fun `håndterer feil ved oppslag som en tom liste med stønadsperioder`() {
        val feilFnr = "2222222222"
        mock(feilFnr, fom, tom, "PSB", errorResponse, status = 500)
        assertEquals(emptySet<Stønadsperiode>(), client.pleiepenger(feilFnr, fom, tom))
    }

    @Test
    fun `håndterer feil ved mapping som en tom liste med stønadsperioder`() {
        val feilFnr = "2222222223"
        mock(feilFnr, fom, tom, "PSB", errorResponse)
        assertEquals(emptySet<Stønadsperiode>(), client.pleiepenger(feilFnr, fom, tom))
    }

    internal companion object {
        private val fnr = "11111111111"
        private val fom = LocalDate.parse("2018-01-01")
        private val tom = fom.plusMonths(3)

        @Language("JSON")
        private val pleiepengerResponse = """
        [
          {
            "ytelseStatus": "AVSLUTTET",
            "anvist": [
              {
                "periode": {
                  "fom": "2018-01-01",
                  "tom": "2018-06-01"
                },
                "utbetalingsgrad": {
                  "verdi": 90.5
                }
              }
            ]
          },
          {
            "ytelseStatus": "LØPENDE",
            "anvist": [
              {
                "periode": {
                  "fom": "2018-07-01",
                  "tom": "2018-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 50.4
                }
              }
            ]
          },
          {
            "ytelseStatus": "EN_ANNEN_STATUS",
            "anvist": [
              {
                "periode": {
                  "fom": "2019-01-01",
                  "tom": "2019-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          }
        ]
        """

        @Language("JSON")
        private val omsorgspengerResponse = """
        [
          {
            "ytelseStatus": "AVSLUTTET",
            "anvist": [
              {
                "periode": {
                  "fom": "2018-01-01",
                  "tom": "2018-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              },
              {
                "periode": {
                  "fom": "2020-01-01",
                  "tom": "2020-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 69
                }
              }
            ]
          }
        ]
        """

        @Language("JSON")
        private val opplæringspengerResponse = """
        [
          {
            "ytelseStatus": "AVSLUTTET",
            "anvist": [
              {
                "periode": {
                  "fom": "2018-01-01",
                  "tom": "2018-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 12
                }
              }
            ]
          }
        ]
        """

        @Language("JSON")
        private val errorResponse = """
        {
          "type": "GENERELL_FEIL",
          "feilmelding": "FP-328673:Det oppstod en valideringsfeil på felt [FeltFeilDto[navn=personident, melding=must not be null]]. Vennligst kontroller at alle feltverdier er korrekte.",
          "feltFeil": [
            {
              "navn": "personident",
              "melding": "must not be null"
            }
          ]
        }
        """

        private fun mock(fnr: String, fom: LocalDate, tom: LocalDate, ytelse: String, response: String, status: Int = 200) {
            WireMock.stubFor(
                WireMock.post(WireMock.urlEqualTo("/abakus"))
                    .withHeader("Authorization", equalTo("Bearer abakus-access-token"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withRequestBody(matchingJsonPath("$.person.identType", equalTo("FNR")))
                    .withRequestBody(matchingJsonPath("$.person.ident", equalTo(fnr)))
                    .withRequestBody(matchingJsonPath("$.periode.fom", equalTo("$fom")))
                    .withRequestBody(matchingJsonPath("$.periode.tom", equalTo("$tom")))
                    .withRequestBody(matchingJsonPath("$.ytelser[0].kode", equalTo(ytelse)))
                    .withRequestBody(matchingJsonPath("$.ytelser[0].kodeverk", equalTo("FAGSAK_YTELSE_TYPE")))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(status)
                            .withBody(response)
                    )
            )
        }

        internal fun WireMockServer.abakusUrl() = URL("${baseUrl()}/abakus")
        internal fun mockAbakus(fnr: String, fom: LocalDate, tom: LocalDate) {
            mock(fnr, fom, tom, "PSB", pleiepengerResponse)
            mock(fnr, fom, tom, "OMP", omsorgspengerResponse)
            mock(fnr, fom, tom, "OLP", opplæringspengerResponse)
        }
    }
}
