package no.nav.helse.sparkel.aareg.arbeidsforhold

import io.mockk.every
import io.mockk.mockk
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClientV1
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class ArbeidsforholdbehovløserTest {
    private val testRapid = TestRapid()
    private val kodeverkClient = mockk<KodeverkClient>()

    init {
        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            tokenSupplier = { "superToken" },
            httpClient = aaregMockClientV1()
        )
        every { kodeverkClient.getYrke(any()) } returns "SAUETELLER"
        Arbeidsforholdbehovløser(testRapid, aaregClient, kodeverkClient)
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
