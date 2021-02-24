package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

internal class PdlInterpreter {

    fun interpret(pdlReply: JsonNode): JsonNode {

        if (pdlReply["errors"] != null && pdlReply["errors"].isArray && !pdlReply["errors"].isEmpty) {
            val errors = pdlReply["errors"].map { it["message"]?.textValue() ?: "no message" }
            throw RuntimeException(errors.joinToString())
        }

        val dødsfall = pdlReply["data"]["hentPerson"].let { hentPerson ->
            hentPerson["doedsfall"]?.let { dødsfall ->
                when (dødsfall.size()) {
                    0 -> null
                    1 -> dødsfall[0]["doedsdato"].asText()
                    else -> håndterFlereMastere(dødsfall)
                }
            }
        }
        return ObjectMapper().createObjectNode().put("dødsdato", dødsfall)
    }

    private fun håndterFlereMastere(dødsfall: JsonNode): String {
        return dødsfall.first { jsonNode ->
            jsonNode.path("metadata").path("master").asText().toLowerCase() == "pdl"
        }.path("doedsdato").asText()
    }

}

