package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import io.mockk.every
import java.util.UUID.randomUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class EregClientTest {

    @Test
    fun `ok - navn og næringer`() {
        val defaultOrganisasjonResponse = defaultOrganisasjonResponse()
        every { mockGenerator.organisasjonResponse() } returns defaultOrganisasjonResponse
        val eregClient = EregClient(
            baseUrl = "http://baseUrl.local",
            httpClient = eregMockClient(mockGenerator),
            appName = "appens navn",
        )

        val eregResponse = runBlocking { eregClient.hentNavnOgNæringForOrganisasjon("organisasjon", randomUUID()) }
        assertEquals("NAV FAMILIE- OG PENSJONSYTELSER, navn - linje 4, siste navn", eregResponse.navn)
        assertEquals(listOf("62.03"), eregResponse.næringer)
    }

    @Test
    fun `næringer mangler i responsen fra ereg`() {
        val organisasjonUtenNæringerResponse = organisasjonUtenNæringerResponse()
        every { mockGenerator.organisasjonResponse() } returns organisasjonUtenNæringerResponse

        val eregClient = EregClient(
            baseUrl = "http://baseUrl.local",
            httpClient = eregMockClient(mockGenerator),
            appName = "appens navn",
        )

        val eregResponse = runBlocking { eregClient.hentNavnOgNæringForOrganisasjon("organisasjon", randomUUID()) }
        assertEquals("NAV FAMILIE- OG PENSJONSYTELSER, navn - linje 4, siste navn", eregResponse.navn)
        assertEquals(emptyList<String>(), eregResponse.næringer)
    }

    @Test
    fun `underenhet henter juridisk overenhet`() {
        val organisasjonMedJuridiskEnhetResponse = organisasjonMedJuridiskEnhetResponse()
        every { mockGenerator.organisasjonResponse() } returns organisasjonMedJuridiskEnhetResponse

        val eregClient = EregClient(
            baseUrl = "http://baseUrl.local",
            httpClient = eregMockClient(mockGenerator),
            appName = "appens navn",
        )
        val eregResponse = runBlocking { eregClient.hentOverOgUnderenheterForOrganisasjon("organisasjon", randomUUID()) }
        assertEquals("orgnummerJuridiskEnhet", eregResponse.overenheter.single().orgnummer)
        assertEquals("navnOgnummerJuridisk", eregResponse.overenheter.single().navn)
        assertEquals("2000-09-11", eregResponse.overenheter.single().gyldighetsperiode.fom.toString())
        assertEquals(null, eregResponse.overenheter.single().gyldighetsperiode.tom)
        assertEquals(emptyList<Enhet>(), eregResponse.underenheter)
    }
}
