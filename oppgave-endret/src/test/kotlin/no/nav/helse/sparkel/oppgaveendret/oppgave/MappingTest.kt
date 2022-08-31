package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.sparkel.oppgaveendret.objectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MappingTest {
    @Test
    fun `Verifiser at objectMapper mapper riktig fra consumer record`() {
        val temaSyk = "SYK"

        val record = "oppgave-endret-record.json".loadFromResources()
        val oppgave: Oppgave = objectMapper.readValue(record)

        assertEquals(34333333, oppgave.id)
        assertEquals(IdentType.AKTOERID, oppgave.ident!!.identType)
        assertEquals(temaSyk, oppgave.tema)
        assertEquals("100001231333", oppgave.ident!!.verdi)
        assertEquals("21312434234", oppgave.ident!!.folkeregisterident)
    }
}

private fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()
