package no.nav.helse.sparkel.sputnik

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert

internal class ForeldrepengerRapidTest {

    private val testRapid = TestRapid().also {
        Foreldrepenger(it, testLøser)
    }

    @BeforeEach
    fun reset() {
        testRapid.reset()
    }

    @Test
    fun `hverken foreldrepenger eller svangerskapspenger`() {
        testRapid.sendTestMessage(IngenYtelser.behov())
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
    fun `både foreldrepenger og svangerskapspenger`() {
        testRapid.sendTestMessage(BeggeYtelser.behov())
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": {
                    "aktørId": "1",
                    "fom": "2001-01-01",
                    "tom": "2001-12-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2001-01-01",
                        "tom": "2001-05-01"
                    }, {
                        "fom": "2001-06-01",
                        "tom": "2001-12-31"
                    }]
                },
                "Svangerskapsytelse": {
                    "aktørId": "1",
                    "fom": "2001-01-05",
                    "tom": "2001-12-25",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2001-01-05",
                        "tom": "2001-05-01"
                    }, {
                        "fom": "2001-06-01",
                        "tom": "2001-12-25"
                    }]
                }
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())

    }

    @Test
    fun `kun foreldrepenger`() {
        testRapid.sendTestMessage(KunForeldrepenger.behov())
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": {
                    "aktørId": "2",
                    "fom": "2002-01-01",
                    "tom": "2002-12-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2002-01-01",
                        "tom": "2002-05-01"
                    }, {
                        "fom": "2002-06-01",
                        "tom": "2002-12-31"
                    }]
                },
                "Svangerskapsytelse": null
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())

    }

    @Test
    fun `kun svangerskapspenger`() {
        testRapid.sendTestMessage(KunSvangerskapspenger.behov())
        @Language("JSON")
        val forventet = """
        {
            "Foreldrepenger": {
                "Foreldrepengeytelse": null,
                "Svangerskapsytelse": {
                    "aktørId": "3",
                    "fom": "2003-01-01",
                    "tom": "2003-12-31",
                    "vedtatt": "2023-02-16T09:52:35.255",
                    "perioder": [{
                        "fom": "2003-01-01",
                        "tom": "2003-05-01"
                    }, {
                        "fom": "2003-06-01",
                        "tom": "2003-12-31"
                    }]
                }
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())

    }

    private companion object {
        private val vedtatt = LocalDateTime.parse("2023-02-16T09:52:35.255").truncatedTo(MILLIS)
        private const val BeggeYtelser = "1"
        private const val KunForeldrepenger = "2"
        private const val KunSvangerskapspenger = "3"
        private const val IngenYtelser = "4"

        @Language("JSON")
        private fun String.behov() = """
            {
                "@id": "${UUID.randomUUID()}",
                "@event_name":"behov",
                "@behov": ["Foreldrepenger"],
                "Foreldrepenger": {
                    "foreldrepengerFom": "2018-01-01",
                    "foreldrepengerTom": "2018-01-31"
                },
                "aktørId": "$this",
                "vedtaksperiodeId": "${UUID.randomUUID()}"
            }
        """
        private fun TestRapid.løsning() = inspektør.message(0).path("@løsning").toString()

        private fun assertJsonEquals(forventet: String, faktisk: String) = JSONAssert.assertEquals(forventet, faktisk, true)

        private val testLøser = object: Foreldrepengerløser {
            override suspend fun hent(aktørId: String, fom: LocalDate, tom: LocalDate) = when (aktørId) {
                BeggeYtelser -> Foreldrepengerløsning(foreldrepengeytelse = BeggeYtelser.ytelse(), svangerskapsytelse = BeggeYtelser.ytelse(fomDag = "05", tomDag = "25"))
                KunForeldrepenger -> Foreldrepengerløsning(foreldrepengeytelse = KunForeldrepenger.ytelse(), svangerskapsytelse = null)
                KunSvangerskapspenger -> Foreldrepengerløsning(foreldrepengeytelse = null, svangerskapsytelse = KunSvangerskapspenger.ytelse())
                else -> Foreldrepengerløsning(foreldrepengeytelse = null, svangerskapsytelse = null)
            }
            private fun String.ytelse(fomDag: String = "01", tomDag: String = "31"):YtelseDto {
                val perioder = listOf(
                    Periode(LocalDate.parse("200$this-01-$fomDag"), LocalDate.parse("200$this-05-01")),
                    Periode(LocalDate.parse("200$this-06-01"), LocalDate.parse("200$this-12-$tomDag")),
                )
                return YtelseDto(
                    aktørId = this,
                    fom = perioder.minOf { it.fom },
                    tom = perioder.maxOf { it.tom },
                    vedtatt = vedtatt,
                    perioder = perioder
                )
            }
        }
    }
}