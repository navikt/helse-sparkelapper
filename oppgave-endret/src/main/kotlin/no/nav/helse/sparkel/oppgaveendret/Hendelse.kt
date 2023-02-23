package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.JsonNode

data class Hendelse(
    private val hendelsestype: Hendelsetype
) {
    internal fun erRelevant() = hendelsestype in Hendelsetype.RELEVANTE_HENDELSER

    companion object {
        fun fromJson(jsonNode: JsonNode): Hendelse {
            return Hendelse(
                enumValueOf(jsonNode.path("hendelse").path("hendelsestype").asText())
            )
        }
    }
}

enum class Hendelsetype {
    OPPGAVE_OPPRETTET, OPPGAVE_ENDRET, OPPGAVE_FERDIGSTILT, OPPGAVE_FEILREGISTRERT;

    companion object {
        internal val RELEVANTE_HENDELSER = listOf(OPPGAVE_OPPRETTET, OPPGAVE_FERDIGSTILT, OPPGAVE_FEILREGISTRERT)
    }
}