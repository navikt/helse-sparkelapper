package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.sparkel.personinfo.Vergemålløser.Resultat
import no.nav.helse.sparkel.personinfo.Vergemålløser.Vergemål
import no.nav.helse.sparkel.personinfo.Vergemålløser.VergemålType
import org.slf4j.LoggerFactory

internal object PdlOversetter {
    private val objectMapper = jacksonObjectMapper()
    private val log = LoggerFactory.getLogger("pdl-oversetter")
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

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
    fun oversetterAlleIdenter(pdlReply: JsonNode): Pair<Identer, List<Ident>> {
        håndterErrors(pdlReply)
        val (aktiveIdenter, historiske) = pdlReply.path("data").path("hentIdenter").path("identer").partition {
            !it.path("historisk").asBoolean()
        }

        val fnr: String = aktiveIdenter.single { it.path("gruppe").asText() == "FOLKEREGISTERIDENT" }.path("ident").asText()
        val aktørId: String = aktiveIdenter.single { it.path("gruppe").asText() == "AKTORID" }.path("ident").asText()

        val npid = aktiveIdenter.firstOrNull { it.path("gruppe").asText() == "NPID" }?.path("ident")?.asText()
        return Identer(
            fødselsnummer = fnr,
            aktørId = aktørId,
            npid = npid,
        ) to historiske.map {
            val ident = it.path("ident").asText()
            when (val gruppe = it.path("gruppe").asText()) {
                "FOLKEREGISTERIDENT" -> Ident.Fødselsnummer(ident)
                "AKTORID" -> Ident.AktørId(ident)
                "NPID" -> Ident.NPID(ident)
                else -> throw RuntimeException("ukjent identgruppe: $gruppe")
            }
        }
    }

    internal fun oversetterVergemålOgFullmakt(pdlReply: JsonNode): Resultat {
        håndterErrors(pdlReply)
        val vergemålNode = pdlReply["data"]["hentPerson"]["vergemaalEllerFremtidsfullmakt"]

        val (fremtidsfullmakter, vergemål) = vergemålNode
            .partition { it["type"].asText() == VergemålType.stadfestetFremtidsfullmakt.name }

        val (gyldigeVergemål, ugyldigeVergemål) = vergemål.partition { VergemålType.gyldig(it["type"].asText()) }

        ugyldigeVergemål.forEach {
            sikkerlogg.warn("Fant et vergemål vi ikke forstod: {}", it.toPrettyString())
        }

        return Resultat(
            vergemål = gyldigeVergemål.map { Vergemål(type = enumValueOf(it["type"].asText())) },
            fremtidsfullmakter = fremtidsfullmakter.map { Vergemål(type = enumValueOf(it["type"].asText())) }
        )
    }

    internal fun håndterErrors(pdlReply: JsonNode) {
        if (pdlReply["errors"] != null && pdlReply["errors"].isArray && !pdlReply["errors"].isEmpty) {
            val errors = pdlReply["errors"].map { it["message"]?.textValue() ?: "no message" }
            throw RuntimeException(errors.joinToString())
        }
    }
}

internal interface IdenterResultat
internal data class Identer(
    val fødselsnummer: String,
    val aktørId: String,
    val npid: String? = null
): IdenterResultat
internal class FantIkkeIdenter: IdenterResultat

internal sealed class Ident(val ident: String) {
    class Fødselsnummer(ident: String) : Ident(ident)
    class AktørId(ident: String) : Ident(ident)
    class NPID(ident: String) : Ident(ident)
}