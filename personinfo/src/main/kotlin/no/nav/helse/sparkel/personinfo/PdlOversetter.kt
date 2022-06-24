package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.sparkel.personinfo.PdlOversetter.Adressebeskyttelse.Companion.somAdressebeskyttelse
import no.nav.helse.sparkel.personinfo.PdlOversetter.Kjønn.Companion.somKjønn
import no.nav.helse.sparkel.personinfo.Vergemålløser.Fullmakt
import no.nav.helse.sparkel.personinfo.Vergemålløser.Område
import no.nav.helse.sparkel.personinfo.Vergemålløser.Område.Alle
import no.nav.helse.sparkel.personinfo.Vergemålløser.Område.Syk
import no.nav.helse.sparkel.personinfo.Vergemålløser.Område.Sym
import no.nav.helse.sparkel.personinfo.Vergemålløser.Resultat
import no.nav.helse.sparkel.personinfo.Vergemålløser.Vergemål
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType
import org.slf4j.LoggerFactory

internal object PdlOversetter {
    private val log = LoggerFactory.getLogger("pdl-oversetter")
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")


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

    fun oversettPersoninfo(ident: String, pdlReply: JsonNode): JsonNode {
        håndterErrors(pdlReply)
        val pdlPerson = pdlReply["data"]["hentPerson"]
        return ObjectMapper().createObjectNode()
            .put("ident", ident)
            .put("fornavn", pdlPerson["navn"].first()["fornavn"].asText())
            .put("mellomnavn", pdlPerson["navn"].first()["mellomnavn"]?.textValue())
            .put("etternavn", pdlPerson["navn"].first()["etternavn"].asText())
            .put("fødselsdato", pdlPerson["foedsel"].first()["foedselsdato"].asText())
            .put("kjønn", pdlPerson["kjoenn"].first()["kjoenn"].somKjønn().name)
            .put("adressebeskyttelse", pdlPerson["adressebeskyttelse"].firstOrNull().somAdressebeskyttelse().name)
    }

    fun oversetterIdenter(pdlReply: JsonNode): IdenterResultat {
        håndterErrors(pdlReply)
        val pdlPerson = pdlReply["data"]["hentIdenter"]["identer"]
        fun identAvType(type: String): String {
            val identgruppe = pdlPerson.firstOrNull { it["gruppe"].asText() == type }
                ?: run {
                    sikkerlogg.info("Finner ikke ident av type=$type i svaret fra PDL\n$pdlReply")
                    throw NoSuchElementException("Finner ikke ident av type=$type i svaret fra PDL")
                }
            return identgruppe["ident"].asText()
        }
        return try {
            Identer(identAvType("FOLKEREGISTERIDENT"), identAvType("AKTORID"))
        } catch (e: NoSuchElementException) {
            FantIkkeIdenter()
        }
    }

    internal fun oversetterVergemålOgFullmakt(pdlReply: JsonNode): Resultat {
        håndterErrors(pdlReply)
        val vergemålNode = pdlReply["data"]["hentPerson"]["vergemaalEllerFremtidsfullmakt"]
        val fullmaktNode = pdlReply["data"]["hentPerson"]["fullmakt"]

        val (fremtidsfullmakter, vergemål) = vergemålNode
            .partition { it["type"].asText() == VergemålType.stadfestetFremtidsfullmakt.name }

        val (gyldigeVergemål, ugyldigeVergemål) = vergemål.partition { VergemålType.gyldig(it["type"].asText()) }

        ugyldigeVergemål.forEach {
            sikkerlogg.warn("Fant et vergemål vi ikke forstod: {}", it.toPrettyString())
        }

        val interessanteFullmakter = fullmaktNode
            .map {
                Fullmakt(
                    områder = it["omraader"].map { område -> Område.fra(område.asText()) },
                    gyldigFraOgMed = it["gyldigFraOgMed"].asLocalDate(),
                    gyldigTilOgMed = it["gyldigTilOgMed"].asLocalDate()
                )
            }
            .filter { it.områder.any { område -> område in listOf(Syk, Sym, Alle) } }

        return Resultat(
            vergemål = gyldigeVergemål.map { Vergemål(type = enumValueOf(it["type"].asText())) },
            fremtidsfullmakter = fremtidsfullmakter.map { Vergemål(type = enumValueOf(it["type"].asText())) },
            fullmakter = interessanteFullmakter
        )
    }

    internal fun håndterErrors(pdlReply: JsonNode) {
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

    private enum class Adressebeskyttelse {
        Fortrolig,
        StrengtFortrolig,
        StrengtFortroligUtland,
        Ugradert,
        Ukjent;

        companion object {
            fun JsonNode?.somAdressebeskyttelse() =
                when (val adressebeskyttelse = this?.get("gradering")?.asText()) {
                    null, "UGRADERT" -> Ugradert
                    "FORTROLIG" -> Fortrolig
                    "STRENGT_FORTROLIG" -> StrengtFortrolig
                    "STRENGT_FORTROLIG_UTLAND" -> StrengtFortroligUtland
                    else -> {
                        log.error("Mottok ukjent adressebeskyttelse: $adressebeskyttelse fra PDL")
                        Ukjent
                    }
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

internal interface IdenterResultat
internal data class Identer(
    val fødselsnummer: String,
    val aktørId: String
): IdenterResultat
internal class FantIkkeIdenter: IdenterResultat