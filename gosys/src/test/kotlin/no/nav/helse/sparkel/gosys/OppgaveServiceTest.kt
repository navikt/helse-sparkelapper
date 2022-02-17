package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveServiceTest {

    @Test
    fun `kan hente oppgaver`() {
        val oppgavehenter = Oppgavehenter { aktørId, behovId -> jacksonObjectMapper().createObjectNode() }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(jacksonObjectMapper().createObjectNode(), svar)
    }
}
