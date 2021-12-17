package no.nav.helse.sparkel.personinfo

import PdlStubber
import no.nav.helse.sparkel.personinfo.Vergemålløser.*
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType.stadfestetFremtidsfullmakt
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType.voksen
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class VergemålOppslagTest : PdlStubber() {

    @Test
    fun `oppslag med vergemål`() {
        stubPdlRespons(medVergemål())
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = listOf(Vergemål(voksen)),
                fremtidsfullmakter = emptyList(),
                fullmakter = emptyList()
            ), resultat
        )
    }

    @Test
    fun `oppslag uten vergemål`() {
        stubPdlRespons(utenVergemålOgFullmakt())
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList(),
                fullmakter = emptyList()
            ), resultat
        )
    }

    @Test
    fun `oppslag med fullmakt`() {
        stubPdlRespons(medFullmakt())
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList(),
                fullmakter = listOf(
                    Fullmakt(
                        områder = listOf(Område.Syk),
                        gyldigFraOgMed = LocalDate.of(2021, 12, 1),
                        gyldigTilOgMed = LocalDate.of(2022, 12, 30)
                    )
                )
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
                fremtidsfullmakter = listOf(Vergemål(stadfestetFremtidsfullmakt)),
                fullmakter = emptyList()
            ), resultat
        )
    }

    @Test
    fun `sparkle over den stjerna`() {
        // Registeret bruker tydeligvis * from å signalisere at fullmakten gjelder alle.
        // TODO: Verifiser om dette kun gjelder dolly?
        stubPdlRespons(medFullmakt(områder = listOf("*")))
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList(),
                fullmakter = listOf(
                    Fullmakt(
                        områder = listOf(Område.Alle),
                        gyldigFraOgMed = LocalDate.of(2021, 12, 1),
                        gyldigTilOgMed = LocalDate.of(2022, 12, 30)
                    )
                )
            ), resultat
        )
    }

    @Test
    fun `håndterer ignorerer vergemålstyper`() {
        stubPdlRespons(medVergemål(type = "ukjent rar type"))
        val resultat = personinfoService.løsningForVergemål("behovId", "fnr")
        assertEquals(
            Resultat(
                vergemål = emptyList(),
                fremtidsfullmakter = emptyList(),
                fullmakter = emptyList()
            ), resultat
        )
    }




}