package no.nav.helse.sparkel.sputnik

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.abakus.Stønadsperiode
import no.nav.helse.sparkel.sputnik.abakus.Ytelse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class SputnikTest {

    private val testRapid = TestRapid().also {
        Sputnik(it, testAbakusClient)
    }

    @BeforeEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `Behov for Foreldrepenger - Har foreldrepenger, men ikke svangerskapspenger`() {
        testRapid.sendTestMessage(foreldrepengerBehov(AltUntattSvangerskapspengerFødselsnummer))
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-01-16"
                    }, {
                        "fom": "2018-01-17",
                        "tom": "2018-01-31"
                    }]
                },
                "Svangerskapsytelse": null
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())
    }

    @Test
    fun `Behov for Foreldrepenger - Har ikke foreldrepenger, men har svangerskapspenger`() {
        testRapid.sendTestMessage(foreldrepengerBehov(AltUntattForeldrepengerFødselsnummer))
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": null,
                "Svangerskapsytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-01-16"
                    },{
                        "fom": "2018-01-17",
                        "tom": "2018-01-31"
                    }]
                }
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())
    }

    @Test
    fun `Behov for Foreldrepenger - Har foreldrepenger og svangerskapspenger`() {
        testRapid.sendTestMessage(foreldrepengerBehov())
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-01-16"
                    }, {
                        "fom": "2018-01-17",
                        "tom": "2018-01-31"
                    }]
                },
                "Svangerskapsytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-01-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-01-16"
                    }, {
                        "fom": "2018-01-17",
                        "tom": "2018-01-31"
                    }]
                }
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())
    }

    @Test
    fun `Behov for Foreldrepenger - Har hverken foreldrepenger eller svangerskapspenger`() {
        testRapid.sendTestMessage(foreldrepengerBehov(IngenStønadstyperFødselsnummer))
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": null,
                "Svangerskapsytelse": null
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())
    }

    @Test
    fun `Behov for alle stønadstyper - har alle`() {
        testRapid.sendTestMessage(behovForAlleStønadstyper())
        val løsninger = (0 until testRapid.inspektør.size).map { testRapid.løsning(it) }
        assertEquals(4, løsninger.size)

        @Language("JSON")
        val forventetForeldrepenger = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-07-16",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-07-16"
                    }]
                },
                "Svangerskapsytelse": {
                    "fom": "2018-01-01",
                    "tom": "2018-07-16",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2018-01-01",
                        "tom": "2018-07-16"
                    }]
                }
            }
        }
        """
        assertJsonEquals(forventetForeldrepenger, løsninger.single { it.contains("Foreldrepenger") })
        @Language("JSON")
        val forventetPleiepenger = """
        {
            "Pleiepenger": [{
                "fom": "2018-01-01",
                "tom": "2018-07-16",
                "grad": 100
            }]
        }
        """
        assertJsonEquals(forventetPleiepenger, løsninger.single { it.contains("Pleiepenger") })
        @Language("JSON")
        val forventetOmsorgspenger = """
        {
            "Omsorgspenger": [{
                "fom": "2018-01-01",
                "tom": "2018-07-16",
                "grad": 100
            }]
        }
        """
        assertJsonEquals(forventetOmsorgspenger, løsninger.single { it.contains("Omsorgspenger") })
        @Language("JSON")
        val forventetOpplæringspenger = """
        {
            "Opplæringspenger": [{
                "fom": "2018-01-01",
                "tom": "2018-07-16",
                "grad": 100
            }, {
                "fom": "2018-07-17",
                "tom": "2018-07-31",
                "grad": 50
            }]
        }
        """
        assertJsonEquals(forventetOpplæringspenger, løsninger.single { it.contains("Opplæringspenger") })
    }

    @Test
    fun `Behov for alle stønadstyper - har ingen`() {
        testRapid.sendTestMessage(behovForAlleStønadstyper(IngenStønadstyperFødselsnummer))
        val løsninger = (0 until testRapid.inspektør.size).map { testRapid.løsning(it) }
        assertEquals(4, løsninger.size)

        @Language("JSON")
        val forventetForeldrepenger = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": null,
                "Svangerskapsytelse": null
            }
        }
        """
        assertJsonEquals(forventetForeldrepenger, løsninger.single { it.contains("Foreldrepenger") })
        @Language("JSON")
        val forventetPleiepenger = """
        {
            "Pleiepenger": []
        }
        """
        assertJsonEquals(forventetPleiepenger, løsninger.single { it.contains("Pleiepenger") })
        @Language("JSON")
        val forventetOmsorgspenger = """
        {
            "Omsorgspenger": []
        }
        """
        assertJsonEquals(forventetOmsorgspenger, løsninger.single { it.contains("Omsorgspenger") })
        @Language("JSON")
        val forventetOpplæringspenger = """
        {
            "Opplæringspenger": []
        }
        """
        assertJsonEquals(forventetOpplæringspenger, løsninger.single { it.contains("Opplæringspenger") })
    }

    private fun TestRapid.løsning(index: Int = 0) = inspektør.message(index).path("@løsning").toString()

    private companion object {
        private val vedtatt = LocalDateTime.parse("2023-02-16T09:52:35.255").truncatedTo(ChronoUnit.MILLIS)
        @Language("JSON")
        private fun foreldrepengerBehov(fødselsnummer: String = "fødselsnummer") = """
        {
            "@event_name":"behov",
            "@behov": ["Foreldrepenger"],
            "Foreldrepenger": {
                "foreldrepengerFom": "2018-01-01",
                "foreldrepengerTom": "2018-01-31"
            },
            "fødselsnummer": "$fødselsnummer"
        }
        """

        @Language("JSON")
        private fun behovForAlleStønadstyper(fødselsnummer: String = "fødselsnummer") = """
        {
            "@event_name":"behov",
            "@behov": ["Foreldrepenger", "Pleiepenger", "Omsorgspenger", "Opplæringspenger"],
            "Foreldrepenger": {
                "foreldrepengerFom": "2018-01-01",
                "foreldrepengerTom": "2018-01-31"
            },
            "Pleiepenger": {
                "pleiepengerFom": "2018-03-01",
                "pleiepengerTom": "2018-03-31"
            },
            "Omsorgspenger": {
                "omsorgspengerFom": "2018-05-01",
                "omsorgspengerTom": "2018-05-31"
            },
            "Opplæringspenger": {
                "opplæringspengerFom": "2018-07-01",
                "opplæringspengerTom": "2018-07-31"
            },
            "fødselsnummer": "$fødselsnummer"
        }
        """

        private fun assertJsonEquals(forventet: String, faktisk: String) = JSONAssert.assertEquals(forventet, faktisk, true)


        private const val IngenStønadstyperFødselsnummer = "0"
        private const val AltUntattSvangerskapspengerFødselsnummer = "1"
        private const val AltUntattForeldrepengerFødselsnummer = "2"

        private val testAbakusClient = object : AbakusClient {
            private val AlleYtelser = setOf("FORELDREPENGER", "SVANGERSKAPSPENGER", "PLEIEPENGER", "OMSORGSPENGER", "OPPLÆRINGSPENGER")
            override fun hent(fødselsnummer: String, fom: LocalDate, tom: LocalDate, vararg ytelser: Ytelse): Set<Stønadsperiode> {
               val medILøsning = when (fødselsnummer) {
                   IngenStønadstyperFødselsnummer -> emptySet()
                   AltUntattSvangerskapspengerFødselsnummer -> AlleYtelser.minus("SVANGERSKAPSPENGER")
                   AltUntattForeldrepengerFødselsnummer -> AlleYtelser.minus("FORELDREPENGER")
                   else -> AlleYtelser
               }

                val (snuteFom, snuteTom) = fom.minusMonths(1) to fom.minusDays(1)
                val (haleFom, haleTom) = tom.plusDays(1) to tom.plusMonths(1)
                return medILøsning.map { Ytelse(it) }.flatMap { listOf(
                    Stønadsperiode(snuteFom, snuteTom, 100, it, vedtatt),
                    Stønadsperiode(fom, tom.minusDays(15), 100, it, vedtatt),
                    Stønadsperiode(tom.minusDays(14), tom, 50, it, vedtatt),
                    Stønadsperiode(haleFom, haleTom, 100, it, vedtatt),
                )}.toSet()
            }
        }
    }

}