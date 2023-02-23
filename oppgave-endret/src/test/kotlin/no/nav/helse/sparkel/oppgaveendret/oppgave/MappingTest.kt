package no.nav.helse.sparkel.oppgaveendret.oppgave

import no.nav.helse.sparkel.oppgaveendret.objectMapper
import no.nav.helse.sparkel.oppgaveendret.oppgave.Identtype.FOLKEREGISTERIDENT
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
}

private fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()
