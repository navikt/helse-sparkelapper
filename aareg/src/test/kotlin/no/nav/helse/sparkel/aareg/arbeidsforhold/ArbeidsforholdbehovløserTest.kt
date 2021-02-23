package no.nav.helse.sparkel.aareg.arbeidsforhold

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.OrganisasjonClient
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.OrganisasjonDto
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class ArbeidsforholdbehovløserTest {
    private val testRapid = TestRapid()
    private val arbeidsforholdClient = mockk<ArbeidsforholdClient>()
    private val organisasjonClient = mockk<OrganisasjonClient>()

    init {
        Arbeidsforholdbehovløser(testRapid, arbeidsforholdClient)
        every { organisasjonClient.finnOrganisasjon("organisasjonsnummer") } returns OrganisasjonDto(
            "BEDRIFT",
            listOf("BRANSJE")
        )
        every {
            arbeidsforholdClient.finnArbeidsforhold(
                organisasjonsnummer = "organisasjonsnummer",
                aktørId = "aktørId",
                fom = LocalDate.of(2020, 1, 1),
                tom = LocalDate.of(2020, 2, 1)
            )
        } returns listOf(
            ArbeidsforholdDto(
                stillingstittel = "STILLING",
                stillingsprosent = 100,
                startdato = LocalDate.of(2000, 1, 1)
            )
        )
    }

    @Test
    fun `tar imot behov`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsforhold"),
            "@id" to UUID.randomUUID(),
            "Arbeidsforhold" to mapOf(
                "organisasjonsnummer" to "organisasjonsnummer",
                "aktørId" to "aktørId",
                "fom" to "2020-01-01",
                "tom" to "2020-02-01"
            ),
        )
        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")

        assertTrue(løsning.hasNonNull("Arbeidsforhold")) { "Skal ha løsning for Arbeidsforhold" }
    }
}
