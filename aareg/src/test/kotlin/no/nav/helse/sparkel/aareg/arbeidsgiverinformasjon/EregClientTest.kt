package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import io.mockk.every
import java.time.LocalDate
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
    fun `virksomhet med en juridisk enhet`() {
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

    @Test
    fun `bestaarAvOrganisasjonsledd med jurdisk enhet`() {
        val ogranisasjonsleddOgJuridiskEnhetResponse = ogranisasjonsleddOgJuridiskEnhetResponse()
        every { mockGenerator.organisasjonResponse() } returns ogranisasjonsleddOgJuridiskEnhetResponse

        val eregClient = EregClient(
            baseUrl = "http://baseUrl.local",
            httpClient = eregMockClient(mockGenerator),
            appName = "appens navn",
        )
        val eregResponse = runBlocking { eregClient.hentOverOgUnderenheterForOrganisasjon("organisasjon", randomUUID()) }

        val expected = listOf(
            Enhet(
                orgnummer = "981", navn = "KOMMUNE, UNDER SLETTING FRA 01.01.2020", gyldighetsperiode = Gyldighetsperiode(
                    fom = "2005-05-23".let { LocalDate.parse(it) },
                    tom = "2017-03-29".let { LocalDate.parse(it) })
            ),
            Enhet(
                orgnummer = "678", navn = "NAVN JURIDISK ENHET", gyldighetsperiode = Gyldighetsperiode(
                    fom = "2019-12-31".let { LocalDate.parse(it) },
                    tom = null)
            )
        )
        assertEquals(expected, eregResponse.overenheter)
        assertEquals(emptyList<Enhet>(), eregResponse.underenheter)

    }
    @Test
    fun `bestaarAvOrganisasjonsledd uten jurdisk enhet`() {

    }

    @Test
    fun `bestaarAvOrganisasjonsledd med organisasjonsleddOver`() {

    }

    @Test
    fun `bestaarAvOrganisasjonsledd med organisasjonsleddUnder`() {
        // Har ikke sett i loggene. Er dette mulig for juridisk enhet?
    }

    @Test
    fun `juridisk enhet`() {
        // Hvordan ser dette ut? Er det en egen type JuridiskEnhet? Må gjøre en get av juridisk enhet
    }

}
