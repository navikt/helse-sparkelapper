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
    fun `sender en melding per mottatt oppgave`() {
        val oppgave = Oppgave(1, "tema", Ident(42, IdentType.AKTOERID, "verdi", "folkeregisterident"))
        oppgaveEndretProducer.onPacket(oppgave)

        assertMeldingsinnhold(forventetMelding, rapid.inspektør.message(0))
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
