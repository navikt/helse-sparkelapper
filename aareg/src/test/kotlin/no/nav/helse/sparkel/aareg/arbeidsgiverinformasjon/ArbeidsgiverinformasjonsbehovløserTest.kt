package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.*
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.ereg.EregClient
import no.nav.helse.sparkel.ereg.EregResponse
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class ArbeidsgiverinformasjonsbehovløserTest {
    private val testRapid = TestRapid()
    private val eregClient = mockk<EregClient>()
    private val kodeverkClient = mockk<KodeverkClient>()
    private val gyldigOrganisasjonsnummer1 = "123456789"
    private val gyldigOrganisasjonsnummer2 = "987654321"

    init {
        Arbeidsgiverinformasjonsbehovløser(testRapid, kodeverkClient, eregClient)

        coEvery { eregClient.hentOrganisasjon(any(), any()) } returns EregResponse("Plantasjen Gaming",
            listOf("123", "345", "567"))
        coEvery { eregClient.hentOrganisasjon(gyldigOrganisasjonsnummer1, any()) } returns EregResponse("Grisesmugling",
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
                "organisasjonsnummer" to gyldigOrganisasjonsnummer2
            ),
        )

        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")

        assertTrue(løsning.hasNonNull("Arbeidsgiverinformasjon")) { "Skal ha løsning for Arbeidsgiverinformasjon" }
        assertFalse(løsning["Arbeidsgiverinformasjon"].isArray)
        assertEquals(gyldigOrganisasjonsnummer2, løsning["Arbeidsgiverinformasjon"]["orgnummer"].asText())
        assertEquals("Plantasjen Gaming", løsning["Arbeidsgiverinformasjon"]["navn"].asText())
        assertEquals(listOf("Gartneri", "Elektronikk", "E-sport"), løsning["Arbeidsgiverinformasjon"]["bransjer"].map(JsonNode::asText))
    }

    @Test
    fun `tar imot behov med flere orgnummere`() {
        val melding = mapOf(
            "@behov" to listOf("Arbeidsgiverinformasjon"),
            "@id" to UUID.randomUUID(),
            "Arbeidsgiverinformasjon" to mapOf(
                "organisasjonsnummer" to listOf(gyldigOrganisasjonsnummer1, gyldigOrganisasjonsnummer2)
            ),
        )

        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))

        val løsning = testRapid.inspektør.message(testRapid.inspektør.size - 1).path("@løsning")
        assertTrue(løsning["Arbeidsgiverinformasjon"].isArray)
        assertEquals(2, løsning["Arbeidsgiverinformasjon"].size())
    }

    @Test
    fun `dropp meldinger med ugyldig orgnummer`(){
        val melding = mapOf(
            "@behov" to listOf("Arbeidsgiverinformasjon"),
            "@id" to UUID.randomUUID(),
            "Arbeidsgiverinformasjon" to mapOf(
                "organisasjonsnummer" to listOf("01010112345")
            ),
        )
        testRapid.sendTestMessage(objectMapper.writeValueAsString(melding))
        assertEquals(0, testRapid.inspektør.size)
    }

    @Test
    fun `validering av organsisasjonsnummer`() {
        val ugyldigOrganisasjonsnummer = "01010112345"

        val ettFnr = JsonNodeFactory.instance.arrayNode().add(ugyldigOrganisasjonsnummer)
        assertThrows<RuntimeException> { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(ettFnr) }
        val mixedBag = JsonNodeFactory.instance.arrayNode().add(gyldigOrganisasjonsnummer1).add(ugyldigOrganisasjonsnummer)
        assertThrows<RuntimeException> { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(mixedBag) }

        val ettOrgnummer = JsonNodeFactory.instance.arrayNode().add(gyldigOrganisasjonsnummer1)
        assertDoesNotThrow { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(ettOrgnummer) }
        val toOrgnummer = JsonNodeFactory.instance.arrayNode().add(gyldigOrganisasjonsnummer2).add(gyldigOrganisasjonsnummer1)
        assertDoesNotThrow { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(toOrgnummer) }

        val ettOrgnummerAlene = JsonNodeFactory.instance.textNode(gyldigOrganisasjonsnummer1)
        assertDoesNotThrow { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(ettOrgnummerAlene) }
        val ettFnrAlene = JsonNodeFactory.instance.textNode(ugyldigOrganisasjonsnummer)
        assertThrows<RuntimeException>  { Arbeidsgiverinformasjonsbehovløser.validateOrganisasjonsnummer(ettFnrAlene) }
    }
}
