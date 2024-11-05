package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.azureTokenStub
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

internal class ArbeidsforholdbehovløserTest {
    private val testRapid = TestRapid()

    init {
        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            scope = "aareg-scope",
            tokenSupplier = azureTokenStub(),
            httpClient = aaregMockClient()
        )
        Arbeidsforholdbehovløser(testRapid, aaregClient)
    }

    @Test
    fun `tar imot behov`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsforhold"),
            "@id" to UUID.randomUUID(),
            "Arbeidsforhold" to mapOf(
                "fødselsnummer" to "fødselsnummer",
                "organisasjonsnummer" to "123456789",
                "aktørId" to "2840402937960",
                "fom" to "2003-08-03",
                "tom" to "2010-08-03"
            ),
        )
        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")

        assertFalse(løsning.path("Arbeidsforhold").isEmpty) { "Skal ha løsning for Arbeidsforhold" }
    }
}
