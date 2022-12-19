package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.oppgaveendret.oppgave.Ident
import no.nav.helse.sparkel.oppgaveendret.oppgave.IdentType
import no.nav.helse.sparkel.oppgaveendret.oppgave.Oppgave
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GosysOppgaveEndretProducerTest {

    private val rapid = TestRapid()
    private val oppgaveEndretProducer: GosysOppgaveEndretProducer = GosysOppgaveEndretProducer(rapid)

    @Test
    fun `køer opp meldinger og sender dem når det er klart`() {
        val oppgave = Oppgave(1, "tema", Ident(42, IdentType.AKTOERID, "verdi", "folkeregisterident"))
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.shipIt()
        assertMeldingsinnhold(forventetMelding, rapid.inspektør.message(0))
    }

    @Test
    fun `dedupliserer fødselsnumre`() {
        val oppgave = Oppgave(1, "tema", Ident(42, IdentType.AKTOERID, "aktørId", "folkeregisterident"))
        val enOppgaveTil = Oppgave(2, "tema", Ident(42, IdentType.AKTOERID, "aktørId", "folkeregisterident"))
        val enUrelatertOppgave = Oppgave(3, "tema", Ident(92, IdentType.AKTOERID, "en annen aktørId", "en annen folkeregisterident"))
        val endaEnOppgave = Oppgave(4, "tema", Ident(42, IdentType.AKTOERID, "aktørId", "folkeregisterident"))
        oppgaveEndretProducer.onPacket(oppgave)
        oppgaveEndretProducer.onPacket(enOppgaveTil)
        oppgaveEndretProducer.onPacket(enUrelatertOppgave)
        oppgaveEndretProducer.onPacket(endaEnOppgave)
        oppgaveEndretProducer.shipIt()

        assertEquals(2, rapid.inspektør.size)
    }

    @Test
    fun `tømmer duplikatene etter hver sending`() {
        val oppgave = Oppgave(1, "tema", Ident(42, IdentType.AKTOERID, "aktørId", "folkeregisterident"))
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
        setOf("@event_name", "fødselsnummer", "aktørId").forEach {
            assertEquals(forventetNode[it], faktisk[it])
        }
        setOf("@id", "@opprettet").forEach {
            assertTrue(faktisk[it].isValueNode)
        }
    }

    private val forventetMelding = """
        {
            "@event_name": "gosys_oppgave_endret",
            "fødselsnummer": "folkeregisterident",
            "aktørId": "verdi"
        }
    """.trimIndent()
}
