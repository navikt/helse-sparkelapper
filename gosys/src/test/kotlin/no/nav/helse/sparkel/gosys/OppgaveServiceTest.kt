package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveServiceTest {

    @Test
    fun `kan hente oppgaver`() {
        val forventetAntall = 2

        val oppgaverSomSkalTellesMed = listOf(
            objectNode().run {
                put("behandlingstema", "ab0455") // Overgangssak fra Speil (skal gi varsel - ikke i enum)
            },
            objectNode().run {
                putNull("behandlingstema")
                putNull("behandlingstype")
            }
        )
        val oppgaverSomIkkeSkalTellesMed = listOf(
            objectNode().run {
                put("behandlingstype", "ae0046") // Anke
            },
        )
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomSkalTellesMed)
                    addAll(oppgaverSomIkkeSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(forventetAntall, svar)
    }

    @Test
    fun `håndterer manglende type eller tema som null når oppgaven ikke skal telles med`() {
        val forventetAntall = 0
        val oppgaverSomIkkeSkalTellesMed = listOf(
            objectNode().run {
                putNull("behandlingstema")
                put("behandlingstype", "ae0046")
            },
            objectNode().run {
                put("behandlingstype", "ae0046")
            },
            objectNode().run {
                put("behandlingstema", "ab0200")
            }
        )
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomIkkeSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(forventetAntall, svar)
    }

    @Test
    fun `håndterer manglende type eller tema som null også når oppgaven skal telles med`() {
        val forventetAntall = 5
        val oppgaverSomSkalTellesMed = listOf(
            objectNode().run {
                put("behandlingstema", "ab0455")
                putNull("behandlingstype")
            },
            objectNode().run {
                put("behandlingstema", "ab0455")
            },
            objectNode().run {
                put("behandlingstype", "ab0333")
            },
            objectNode().run {
                putNull("behandlingstema")
                putNull("behandlingstype")
            },
            objectNode()
        )
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId")

        assertEquals(forventetAntall, svar)
    }

    private fun objectNode() = jacksonObjectMapper().createObjectNode()
}

