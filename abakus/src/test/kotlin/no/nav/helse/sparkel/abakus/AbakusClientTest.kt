package no.nav.helse.sparkel.abakus

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.matching
import com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import no.nav.helse.sparkel.abakus.Fødselsnummer.Companion.fødselsnummer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

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
                override fun accessToken() = "ey-abakus-access-token"
            }
        )
    }

    @AfterAll
    fun afterAll() = server.stop()

    @Test
    fun `hente pleiepenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-06-01"), grad = 91, ytelse = Pleiepenger, vedtatt = vedtatt),
            Stønadsperiode(fom = LocalDate.parse("2018-07-01"), tom = LocalDate.parse("2018-12-31"), grad = 50, ytelse = Pleiepenger, vedtatt = vedtatt)
        ), client.hent(fnr, fom, tom, Pleiepenger))
    }

    @Test
    fun `hente omsorgspenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-12-31"), grad = 100, ytelse = Omsorgspenger, vedtatt = vedtatt),
            Stønadsperiode(fom = LocalDate.parse("2020-01-01"), tom = LocalDate.parse("2020-12-31"), grad = 69, ytelse = Omsorgspenger, vedtatt = vedtatt)
        ), client.hent(fnr, fom, tom, Omsorgspenger))

    }

    @Test
    fun `hente opplæringspenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-12-31"), grad = 12, ytelse = Opplæringspenger, vedtatt = vedtatt),
        ), client.hent(fnr, fom, tom, Opplæringspenger))
    }

    @Test
    fun `hente foreldrepenger og svangerskapspenger`() {
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-06-01"), grad = 51, ytelse = Foreldrepenger, vedtatt = vedtatt),
            Stønadsperiode(fom = LocalDate.parse("2018-06-02"), tom = LocalDate.parse("2018-12-31"), grad = 100, ytelse = Svangerskapspenger, vedtatt = vedtatt),
        ), client.hent(fnr, fom, tom, Foreldrepenger, Svangerskapspenger))
    }

    @Test
    fun `filtrerer bort urelevante stønadsperioder`() {
        val filterFnr = "2222222224".fødselsnummer
        mock(filterFnr, fom, tom, setOf(Pleiepenger), urelevanteStønadsperioderResponse)
        assertEquals(setOf(
            Stønadsperiode(fom = LocalDate.parse("2018-01-01"), tom = LocalDate.parse("2018-01-01"), grad = 100, ytelse = Pleiepenger, vedtatt = vedtatt),
            Stønadsperiode(fom = LocalDate.parse("2019-05-01"), tom = LocalDate.parse("2019-07-31"), grad = 100, ytelse = Pleiepenger, vedtatt = vedtatt),
            Stønadsperiode(fom = LocalDate.parse("2020-12-31"), tom = LocalDate.parse("2020-12-31"), grad = 100, ytelse = Pleiepenger, vedtatt = vedtatt),
        ), client.hent(filterFnr, fom, tom, Pleiepenger))
    }

    @Test
    fun `kaster feil om vi får error-response fra Abakus`() {
        val feilFnr = "2222222222".fødselsnummer
        mock(feilFnr, fom, tom, setOf(Pleiepenger), errorResponse, status = 500)
        assertThrows<IllegalStateException> { client.hent(feilFnr, fom, tom, Pleiepenger) }
    }

    @Test
    fun `kaster feil om vi får response vi ikke klarer å mappe til stønadsperioder`() {
        val feilFnr = "2222222223".fødselsnummer
        mock(feilFnr, fom, tom, setOf(Pleiepenger), errorResponse)
        assertThrows<IllegalStateException> { client.hent(feilFnr, fom, tom) }
    }

    internal companion object {
        private val vedtatt = LocalDateTime.parse("2023-02-16T09:52:35.255").truncatedTo(ChronoUnit.MILLIS)
        private val fnr = "11111111111".fødselsnummer
        private val fom = LocalDate.parse("2018-01-01")
        private val tom = LocalDate.parse("2020-12-31")

        @Language("JSON")
        private val pleiepengerResponse = """
        [
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
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
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "LØPENDE",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
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
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "EN_ANNEN_STATUS",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
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
            "ytelse": "OMSORGSPENGER",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
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
            "ytelse": "OPPLÆRINGSPENGER",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
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
        private val foreldrepengerOgSvangerskapspengerResponse = """
        [{
            "ytelse": "FORELDREPENGER",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [{
                "periode": {
                    "fom": "2018-01-01",
                    "tom": "2018-06-01"
                },
                "utbetalingsgrad": {
                    "verdi": 50.50
                }
            }]
        },
        {
            "ytelse": "SVANGERSKAPSPENGER",
            "ytelseStatus": "LØPENDE",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [{
                "periode": {
                    "fom": "2018-06-02",
                    "tom": "2018-12-31"
                }
            }]
        }]
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

        @Language("JSON")
        private val urelevanteStønadsperioderResponse = """
        [
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [
              {
                "periode": {
                  "@TestForklaring": "Skal filteres bort ettersom det er før fom",
                  "fom": "2017-12-31",
                  "tom": "2017-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          },
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [
              {
                "periode": {
                  "@TestForklaring": "Skal bli med ettersom det overlapper med fom",
                  "fom": "2018-01-01",
                  "tom": "2018-01-01"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          },
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [
              {
                "periode": {
                  "@TestForklaring": "Skal bli med ettersom det er midt i perioden",
                  "fom": "2019-05-01",
                  "tom": "2019-07-31"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          },
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [
              {
                "periode": {
                  "@TestForklaring": "Skal bli med ettersom det overlapper med tom",
                  "fom": "2020-12-31",
                  "tom": "2020-12-31"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          },
          {
            "ytelse": "PLEIEPENGER_SYKT_BARN",
            "ytelseStatus": "AVSLUTTET",
            "vedtattTidspunkt": "2023-02-16T09:52:35.255",
            "anvist": [
              {
                "periode": {
                  "@TestForklaring": "Skal filteres bort ettersom det er etter tom",
                  "fom": "2021-01-01",
                  "tom": "2021-01-01"
                },
                "utbetalingsgrad": {
                  "verdi": 100
                }
              }
            ]
          }
        ]
        """

        private fun mock(fnr: Fødselsnummer, fom: LocalDate, tom: LocalDate, ytelser: Set<Ytelse>, response: String, status: Int = 200) {
            WireMock.stubFor(
                WireMock.post(WireMock.urlEqualTo("/abakus"))
                    .withHeader("Authorization", matching("Bearer ey.*"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withHeader("Content-Type", equalTo("application/json"))
                    .withHeader("Nav-Consumer-Id", equalTo("Sykepenger"))
                    .withHeader("Nav-Callid", AnythingPattern())
                    .withRequestBody(matchingJsonPath("$.ident.verdi", equalTo("$fnr")))
                    .withRequestBody(matchingJsonPath("$.periode.fom", equalTo("$fom")))
                    .withRequestBody(matchingJsonPath("$.periode.tom", equalTo("$tom")))
                    .withRequestBody(matchingJsonPath("$.ytelser", equalTo(ytelser.joinToString(", ", prefix = "[ ", postfix = " ]") { "\"$it\"" })))
                    .willReturn(
                        WireMock.aResponse()
                            .withStatus(status)
                            .withBody(response)
                    )
            )
        }

        internal fun WireMockServer.abakusUrl() = URL("${baseUrl()}/abakus")
        internal fun mockAbakus(fnr: Fødselsnummer, fom: LocalDate, tom: LocalDate) {
            mock(fnr, fom, tom, setOf(Pleiepenger), pleiepengerResponse)
            mock(fnr, fom, tom, setOf(Omsorgspenger), omsorgspengerResponse)
            mock(fnr, fom, tom, setOf(Opplæringspenger), opplæringspengerResponse)
            mock(fnr, fom, tom, setOf(Foreldrepenger, Svangerskapspenger), foreldrepengerOgSvangerskapspengerResponse)
        }

        private val Pleiepenger = Ytelse("PLEIEPENGER_SYKT_BARN")
        private val Omsorgspenger = Ytelse("OMSORGSPENGER")
        private val Opplæringspenger = Ytelse("OPPLÆRINGSPENGER")
        private val Foreldrepenger = Ytelse("FORELDREPENGER")
        private val Svangerskapspenger = Ytelse("SVANGERSKAPSPENGER")
    }
}
