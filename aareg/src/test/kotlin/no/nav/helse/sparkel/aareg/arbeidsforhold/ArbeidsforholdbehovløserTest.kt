package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.AaregClient
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.arbeidsforholdV2Response
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.util.KodeverkClient
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class ArbeidsforholdbehovløserTest {
    private val testRapid = TestRapid()
    private val aaregClient = mockk<AaregClient>()
    private val kodeverkClient = mockk<KodeverkClient>()

    init {
        Arbeidsforholdbehovløser(testRapid, aaregClient, kodeverkClient)
        coEvery {
            aaregClient.hentFraAareg(any(), any())
        } returns objectMapper.readValue(arbeidsforholdV2Response())
        every { kodeverkClient.getYrke(any() )} returns "SAUETELLER"
    }

    @Test
    fun `tar imot behov`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsforhold"),
            "@id" to UUID.randomUUID(),
            "Arbeidsforhold" to mapOf(
                "fødselsnummer" to "fødselsnummer",
                "organisasjonsnummer" to "organisasjonsnummer",
                "aktørId" to "aktørId",
                "fom" to "2020-01-01",
                "tom" to "2020-02-01"
            ),
        )
        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")

        assertFalse(løsning.path("Arbeidsforhold").isEmpty) { "Skal ha løsning for Arbeidsforhold" }
    }
}
