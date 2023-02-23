package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.oppgaveendret.oppgave.Identtype.FOLKEREGISTERIDENT
import no.nav.helse.sparkel.oppgaveendret.oppgave.Oppgave
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GosysOppgaveEndretProducerTest {

    private val rapid = TestRapid()
    private val oppgaveEndretProducer: GosysOppgaveEndretProducer = GosysOppgaveEndretProducer(rapid)

    @Test
    fun `køer opp meldinger og sender dem når det er klart`() {
        val oppgave = Oppgave(1, "SYK", "12345678910", FOLKEREGISTERIDENT)
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.shipIt()
        assertMeldingsinnhold(forventetMelding, rapid.inspektør.message(0))
    }

    @Test
    fun `dedupliserer fødselsnumre`() {
        val oppgave = Oppgave(1, "SYK", "12345678910", FOLKEREGISTERIDENT)
        val enOppgaveTil = Oppgave(2, "SYK", "12345678910", FOLKEREGISTERIDENT)
        val enUrelatertOppgave = Oppgave(3, "SYK", "10987654321", FOLKEREGISTERIDENT)
        val endaEnOppgave = Oppgave(4, "SYK", "12345678910", FOLKEREGISTERIDENT)
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.onPacket(enOppgaveTil)
        oppgaveEndretProducer.onPacket(enUrelatertOppgave)
        oppgaveEndretProducer.onPacket(endaEnOppgave)
        oppgaveEndretProducer.shipIt()

        assertEquals(2, rapid.inspektør.size)
    }

    @Test
    fun `tømmer duplikatene etter hver sending`() {
        val oppgave = Oppgave(1, "SYK", "fødselsnummer", FOLKEREGISTERIDENT)
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.shipIt()
        assertEquals(1, rapid.inspektør.size)
        oppgaveEndretProducer.shipIt()
        assertEquals(1, rapid.inspektør.size)
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.shipIt()
        assertEquals(2, rapid.inspektør.size)
    }

    private fun assertMeldingsinnhold(forventet: String, faktisk: JsonNode) {
        val forventetNode = objectMapper.readTree(forventet)
        setOf("@event_name", "fødselsnummer").forEach {
            assertEquals(forventetNode[it], faktisk[it])
        }
        setOf("@id", "@opprettet").forEach {
            assertTrue(faktisk[it].isValueNode)
        }
    }

    private val forventetMelding = """
        {
            "@event_name": "gosys_oppgave_endret",
            "fødselsnummer": "12345678910"
        }
    """.trimIndent()
}
