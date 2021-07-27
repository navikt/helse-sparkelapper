package no.nav.helse.sparkel.ereg

import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.aareg.util.mockStsRestClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID.randomUUID

internal class EregClientTest {

    @Test
    fun ok() {
        val eregClient = EregClient(
            baseUrl = "http://baseUrl.local",
            stsRestClient = mockStsRestClient,
            httpClient = eregMockClient(mockGenerator),
            appName = "appens navn",
        )

        val eregResponse = runBlocking { eregClient.hentOrganisasjon("organisasjon", randomUUID()) }
        assertEquals("NAV FAMILIE- OG PENSJONSYTELSER, navn - linje 4, siste navn", eregResponse.navn)
        assertEquals(listOf("62.03"), eregResponse.næringer)
    }
}
