package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.objectMapper
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

internal class ArbeidsgiverinformasjonsbehovløserTest {
    private val testRapid = TestRapid()
    private val organisasjonClient = mockk<OrganisasjonClient>()

    init {
        Arbeidsgiverinformasjonsbehovløser(testRapid, organisasjonClient)
        every { organisasjonClient.finnOrganisasjon("organisasjonsnummer") } returns OrganisasjonDto(
            "BEDRIFT",
            listOf("BRANSJE")
        )
    }

    @Test
    fun `tar imot behov`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsgiverinformasjon"),
            "@id" to UUID.randomUUID(),
            "Arbeidsgiverinformasjon" to mapOf(
                "organisasjonsnummer" to "organisasjonsnummer"
            ),
        )

        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")

        assertTrue(løsning.hasNonNull("Arbeidsgiverinformasjon")) { "Skal ha løsning for Arbeidsgiverinformasjon" }
    }
}
