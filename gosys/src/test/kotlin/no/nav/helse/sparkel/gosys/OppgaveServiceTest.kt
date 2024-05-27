package no.nav.helse.sparkel.gosys

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
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
        ).leggPåOpprettetTidspunkter()
        val oppgaverSomIkkeSkalTellesMed = listOf(
            objectNode().run {
                put("behandlingstype", "ae0046") // Anke
            },
        ).leggPåOpprettetTidspunkter()
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomSkalTellesMed)
                    addAll(oppgaverSomIkkeSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId", LocalDate.now())

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
            },
            objectNode().run {
                put("behandlingstema", "ab0446")
            },
        ).leggPåOpprettetTidspunkter()
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomIkkeSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId", LocalDate.now())

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
        ).leggPåOpprettetTidspunkter()
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId", LocalDate.now())

        assertEquals(forventetAntall, svar)
    }

    @Test
    fun `Teller ikke med feilutbetaling og ikke opprettet t-sak-oppgaver som er mer enn et år gamle`() {
        val forventetAntall = 3
        val oppgaverSomSkalTellesMed = listOf(
            objectNode().run {
                put("behandlingstema", "ab0455") // Overgangssak fra Speil (skal gi varsel - ikke i enum)
                put("opprettetTidspunkt", OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            },
            objectNode().run {
                put("behandlingstype", "ae0161") // Feilutbetaling
                put("opprettetTidspunkt", OffsetDateTime.now().minusYears(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            },
            objectNode().run {
                put("behandlingstema", "ab0449") // Ikke opprettet T-sak
                put("opprettetTidspunkt", OffsetDateTime.now().minusYears(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
            },
        )
        val oppgaverSomIkkeSkalTellesMed = listOf(
                objectNode().run {
                    put("behandlingstype", "ae0161") // Feilutbetaling
                },
                objectNode().run {
                    put("behandlingstype", "ae0160") // Feilutbetaling - utland
                },
                objectNode().run {
                    put("behandlingstema", "ab0449") // Ikke opprettet T-sak
                },
            ).map { it.put("opprettetTidspunkt", OffsetDateTime.now().minusYears(1).minusDays(1).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)) }
        val oppgavehenter = Oppgavehenter { _, _ ->
            objectNode().run {
                put("antallTreffTotalt", 3)
                set("oppgaver", jacksonObjectMapper().createArrayNode().run {
                    addAll(oppgaverSomSkalTellesMed)
                    addAll(oppgaverSomIkkeSkalTellesMed)
                })
            }
        }

        val service = OppgaveService(oppgavehenter)

        val svar = service.løsningForBehov("behovId", "aktørId", LocalDate.now().minusYears(1))

        assertEquals(forventetAntall, svar)
    }

    @Test
    fun `oppgaver-feltet mangler i responsen`() {
        val oppgavehenter = Oppgavehenter { _, _ -> objectNode() }
        val service = OppgaveService(oppgavehenter)
        val svar = service.løsningForBehov("behovId", "aktørId", LocalDate.now())
        assertEquals(null, svar)
    }

    private fun objectNode() = jacksonObjectMapper().createObjectNode()
}

private fun List<ObjectNode>.leggPåOpprettetTidspunkter() =
    map { it.put("opprettetTidspunkt", OffsetDateTime.now().format(DateTimeFormatter.ISO_ZONED_DATE_TIME)) }

