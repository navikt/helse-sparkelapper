package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveServiceTest {

    @Test
    fun `kan hente oppgaver`() {
        val forventetAntall = 0

        val oppgavehenter = Oppgavehenter { aktørId, behovId -> jacksonObjectMapper().createObjectNode().run {
            put("antallTreffTotalt", forventetAntall)
        } }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(forventetAntall, svar)
    }
}
