package no.nav.helse.sparkel.aareg.util

import io.mockk.every
import io.mockk.mockk
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.OrganisasjonClient
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.OrganisasjonDto
import no.nav.tjeneste.virksomhet.organisasjon.v5.binding.OrganisasjonV5
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.Naering
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.Naeringskoder
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.Organisasjon
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.OrganisasjonsDetaljer
import no.nav.tjeneste.virksomhet.organisasjon.v5.informasjon.UstrukturertNavn
import no.nav.tjeneste.virksomhet.organisasjon.v5.meldinger.HentOrganisasjonResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrganisasjonClientTest {
    private val mockOrganisasjon = mockk<OrganisasjonV5>()
    private val mockKodeverkClient = mockk<KodeverkClient>()

    @Test
    fun `prase finn organisasjon response`() {
        val response = HentOrganisasjonResponse().also { response ->
            response.organisasjon = Organisasjon().also { organisasjon ->
                organisasjon.navn = UstrukturertNavn().also { navn ->
                    navn.navnelinje.addAll(listOf("NAVN", " ", " ", " ", " "))
                }
                organisasjon.organisasjonDetaljer = OrganisasjonsDetaljer().also { organisasjonsDetaljer ->
                    organisasjonsDetaljer.naering.add(Naering().also { naering ->
                        naering.naeringskode = Naeringskoder().also {
                            it.kodeRef = "ref"
                        }
                    })
                }
            }
        }
        every { mockOrganisasjon.hentOrganisasjon(any()) } returns response
        every { mockKodeverkClient.getNæring("ref") } returns "NÆRING"

        val organisasjonClient = OrganisasjonClient(mockOrganisasjon, mockKodeverkClient)

        assertEquals(OrganisasjonDto("NAVN", listOf("NÆRING")), organisasjonClient.finnOrganisasjon("orgnummer"))
    }
}
