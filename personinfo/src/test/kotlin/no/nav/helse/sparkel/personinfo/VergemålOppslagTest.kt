package no.nav.helse.sparkel.personinfo

import PdlStubber
import no.nav.helse.sparkel.personinfo.Vergemålløser.Resultat
import no.nav.helse.sparkel.personinfo.Vergemålløser.Vergemål
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType.stadfestetFremtidsfullmakt
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType.voksen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VergemålOppslagTest : PdlStubber() {

    @Test
    fun `oppslag med vergemål`() {
        stubPdlRespons(medVergemål())
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = listOf(Vergemål(voksen)),
                fremtidsfullmakter = emptyList()
            ), resultat
        )
    }

    @Test
    fun `oppslag uten vergemål`() {
        stubPdlRespons(utenVergemålEllerFremtidsfullmakt())
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList()
            ), resultat
        )
    }

    @Test
    fun `oppslag fremtidig fullmakt`() {
        stubPdlRespons(medVergemål(type = stadfestetFremtidsfullmakt.name))
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = listOf(Vergemål(stadfestetFremtidsfullmakt))
            ), resultat
        )
    }

    @Test
    fun `håndterer ukjente vergemålstyper`() {
        stubPdlRespons(medVergemål(type = "ukjent rar type"))
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList()
            ), resultat
        )
    }
}