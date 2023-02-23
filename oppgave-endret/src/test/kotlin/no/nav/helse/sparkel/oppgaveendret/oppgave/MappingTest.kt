package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.sparkel.oppgaveendret.objectMapper
import no.nav.helse.sparkel.oppgaveendret.oppgave.Identtype.FOLKEREGISTERIDENT
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MappingTest {
    @Test
    fun `Verifiser at vi mapper riktig fra consumer record`() {
        val temaSyk = "SYK"

        val record = "gosysoppgave-record.json".loadFromResources()
        val oppgave = Oppgave.fromJson(objectMapper.readTree(record))

        assertEquals(
            Oppgave(
                1000000,
                temaSyk,
                "1000001234567",
                FOLKEREGISTERIDENT
            ),
            oppgave
        )
    }
    @Test
    fun `Mapping returnerer null dersom bruker-objektet ikke er satt`() {
        val jsonNode = testJson.apply {
            this as ObjectNode
            val oppgaveNode = path("oppgave") as ObjectNode
            oppgaveNode.remove("bruker")
        }
        val oppgave = Oppgave.fromJson(jsonNode)

        assertEquals(null, oppgave)
    }

    @Language("JSON")
    private val testJson = """
        {
            "hendelse": {
                "hendelsestype": "OPPGAVE_OPPRETTET"
            },
            "oppgave": {
                "oppgaveId": 123,
                "kategorisering": {
                    "tema": "SYK"
                },
                "bruker": {
                    "ident": "12345678911",
                    "identType": "FOLKEREGISTERIDENT"
                }
            }
        } 
        """.let { jacksonObjectMapper().readTree(it) }
}


private fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()
