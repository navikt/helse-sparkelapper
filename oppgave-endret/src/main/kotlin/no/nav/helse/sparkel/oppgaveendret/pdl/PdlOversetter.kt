package no.nav.helse.sparkel.oppgaveendret.pdl

import com.fasterxml.jackson.databind.JsonNode

object PdlOversetter {

    private fun håndterErrors(pdlReply: JsonNode) {
        if (pdlReply["errors"] != null && pdlReply["errors"].isArray && !pdlReply["errors"].isEmpty) {
            val errors = pdlReply["errors"].map { it["message"]?.textValue() ?: "no message" }
            throw RuntimeException(errors.joinToString())
        }
    }

    fun oversetterIdenter(pdlReply: JsonNode): Identer {
        håndterErrors(pdlReply)
        val pdlPerson = pdlReply["data"]["hentIdenter"]["identer"]
        fun identAvType(type: String) = pdlPerson.single { it["gruppe"].asText() == type }["ident"].asText()
        return Identer(identAvType("FOLKEREGISTERIDENT"), identAvType("AKTORID"))
    }

    data class Identer(
        val fødselsnummer: String,
        val aktørId: String
    )

}