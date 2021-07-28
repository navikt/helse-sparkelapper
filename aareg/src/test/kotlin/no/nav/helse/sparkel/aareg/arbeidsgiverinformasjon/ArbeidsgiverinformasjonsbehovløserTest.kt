package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.util.KodeverkClient
import no.nav.helse.sparkel.ereg.EregClient
import no.nav.helse.sparkel.ereg.EregResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ArbeidsgiverinformasjonsbehovløserTest {
    private val testRapid = TestRapid()
    private val eregClient = mockk<EregClient>()
    private val kodeverkClient = mockk<KodeverkClient>()

    init {
        Arbeidsgiverinformasjonsbehovløser(testRapid, kodeverkClient, eregClient)

        coEvery { eregClient.hentOrganisasjon(any(), any()) } returns EregResponse("Plantasjen Gaming",
            listOf("123", "345", "567"))
        coEvery { eregClient.hentOrganisasjon("6966669", any()) } returns EregResponse("Grisesmugling",
            listOf("789", "890"))
        kodeverkClient.run {
            every { getNæring("123") } returns "Gartneri"
            every { getNæring("345") } returns "Elektronikk"
            every { getNæring("567") } returns "E-sport"
            every { getNæring("789") } returns "Bagasje"
            every { getNæring("890") } returns "Subtilitet"
        }
    }

    @Test
    fun `tar imot behov med ett orgnummer`() {
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
        assertFalse(løsning["Arbeidsgiverinformasjon"].isArray)
        assertEquals("organisasjonsnummer", løsning["Arbeidsgiverinformasjon"]["orgnummer"].asText())
        assertEquals("Plantasjen Gaming", løsning["Arbeidsgiverinformasjon"]["navn"].asText())
        assertEquals(listOf("Gartneri", "Elektronikk", "E-sport"), løsning["Arbeidsgiverinformasjon"]["bransjer"].map(JsonNode::asText))
    }

    @Test
    fun `tar imot behov med flere orgnummere`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsgiverinformasjon"),
            "@id" to UUID.randomUUID(),
            "Arbeidsgiverinformasjon" to mapOf(
                "organisasjonsnummer" to listOf("04201337", "6966669")
            ),
        )

        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")
        assertTrue(løsning["Arbeidsgiverinformasjon"].isArray)
        assertEquals(2, løsning["Arbeidsgiverinformasjon"].size())
    }
}
