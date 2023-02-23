package no.nav.helse.sparkel.sputnik

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.abakus.Stønadsperiode
import no.nav.helse.sparkel.sputnik.abakus.Ytelse
import org.intellij.lang.annotations.Language
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
    fun `en test`() {
        testRapid.sendTestMessage(behov())
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
                        "tom": "2018-01-31"
                    }]
                },
                "Svangerskapsytelse": null
            }
        }
        """
        assertJsonEquals(forventet, testRapid.løsning())
    }

    private fun TestRapid.løsning() = inspektør.message(0).path("@løsning").toString()

    private companion object {
        private val vedtatt = LocalDateTime.parse("2023-02-16T09:52:35.255").truncatedTo(ChronoUnit.MILLIS)
        @Language("JSON")
        private fun behov() = """
        {
            "@id": "${UUID.randomUUID()}",
            "@event_name":"behov",
            "@behov": ["Foreldrepenger"],
            "Foreldrepenger": {
                "foreldrepengerFom": "2018-01-01",
                "foreldrepengerTom": "2018-01-31"
            },
            "fødselsnummer": "fødselsnummer"
        }
        """

        private fun assertJsonEquals(forventet: String, faktisk: String) = JSONAssert.assertEquals(forventet, faktisk, true)

        private val testAbakusClient = object : AbakusClient {
            override fun hent(
                fødselsnummer: String,
                fom: LocalDate,
                tom: LocalDate,
                vararg ytelser: Ytelse
            ): Set<Stønadsperiode> = setOf(
                Stønadsperiode(fom, tom, 100, Ytelse("FORELDREPENGER"), vedtatt)
            )
        }
    }

}