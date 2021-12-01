package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.sparkel.personinfo.PdlOversetter.Adressebeskyttelse.Companion.somAdressebeskyttelse
import no.nav.helse.sparkel.personinfo.PdlOversetter.Kjønn.Companion.somKjønn

object PdlOversetter {

    fun oversettDødsdato(pdlReply: JsonNode): JsonNode {
        håndterErrors(pdlReply)

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

    fun oversettPersoninfo(pdlReply: JsonNode): JsonNode {
        håndterErrors(pdlReply)
        val pdlPerson = pdlReply["data"]["hentPerson"]
        return ObjectMapper().createObjectNode()
            .put("fornavn", pdlPerson["navn"].first()["fornavn"].asText())
            .put("mellomnavn", pdlPerson["navn"].first()["mellomnavn"]?.textValue())
            .put("etternavn", pdlPerson["navn"].first()["etternavn"].asText())
            .put("fødselsdato", pdlPerson["foedsel"].first()["foedselsdato"].asText())
            .put("kjønn", pdlPerson["kjoenn"].first()["kjoenn"].somKjønn().name)
            .put("adressebeskyttelse", pdlPerson["adressebeskyttelse"].firstOrNull().somAdressebeskyttelse().name)
    }

    fun oversetterIdenter(pdlReply: JsonNode): Identer {
        håndterErrors(pdlReply)
        val pdlPerson = pdlReply["data"]["hentIdenter"]["identer"]
        fun identAvType(type: String) = pdlPerson.single { it["gruppe"].asText() == type }["ident"].asText()
        return Identer(identAvType("FOLKEREGISTERIDENT"), identAvType("AKTORID"))
    }

    private fun håndterErrors(pdlReply: JsonNode) {
        if (pdlReply["errors"] != null && pdlReply["errors"].isArray && !pdlReply["errors"].isEmpty) {
            val errors = pdlReply["errors"].map { it["message"]?.textValue() ?: "no message" }
            throw RuntimeException(errors.joinToString())
        }
    }

    private fun håndterFlereMastere(dødsfall: JsonNode): String {
        return dødsfall.first { jsonNode ->
            jsonNode.path("metadata").path("master").asText().lowercase() == "pdl"
        }.path("doedsdato").asText()
    }

    data class Identer(
        val fødselsnummer: String,
        val aktørId: String
    )

    private enum class Adressebeskyttelse {
        Fortrolig,
        StrengtFortrolig,
        StrengtFortroligUtland,
        Ugradert,
        Ukjent;

        companion object {
            fun JsonNode?.somAdressebeskyttelse() = when (this?.get("gradering")?.asText()) {
                null, "UGRADERT" -> Ugradert
                "FORTROLIG" -> Fortrolig
                "STRENGT_FORTROLIG" -> StrengtFortrolig
                "STRENGT_FORTROLIG_UTLAND" -> StrengtFortroligUtland
                else -> Ukjent
            }
        }
    }


    private enum class Kjønn {
        Mann,
        Kvinne,
        Ukjent;

        companion object {
            fun JsonNode.somKjønn() = when (asText()) {
                "KVINNE" -> Kvinne
                "MANN" -> Mann
                else -> Ukjent
            }
        }
    }

}

