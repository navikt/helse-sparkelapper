package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveServiceTest {

    @Test
    fun `kan hente oppgaver`() {
        val forventetAntall = 2

        val oppgavehenter = Oppgavehenter { _, _ ->
            jacksonObjectMapper().createObjectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    add(jacksonObjectMapper().createObjectNode().run {
                        putNull("behandlingstema")
                        put("behandlingstype", "ae0046") // Anke
                    })
                    add(jacksonObjectMapper().createObjectNode().run {
                        put("behandlingstype", "ae0046") //
                    })
                    add(jacksonObjectMapper().createObjectNode().run {
                        put("behandlingstema", "ab0455") // Overgangssak fra Speil (skal gi varsel - ikke i enum)
                        putNull("behandlingstype")
                    })
                    add(jacksonObjectMapper().createObjectNode().run {
                        putNull("behandlingstema")
                        putNull("behandlingstype")
                    })
                    add(jacksonObjectMapper().createObjectNode().run {
                        put("behandlingstema", "ab0338") // KLAGE_UNNTAK_FRA_ARBEIDSGIVERPERIODE
                        put("behandlingstype", "ae0058")
                    })
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(forventetAntall, svar)
    }
}

