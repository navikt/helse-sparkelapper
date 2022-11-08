package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import no.nav.helse.sparkel.personinfo.PdlOversetter
import no.nav.helse.sparkel.personinfo.PdlOversetter.håndterErrors
import org.slf4j.LoggerFactory

internal object PdlReplyOversetter {
    private val objectMapper = jacksonObjectMapper()
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    internal fun fiskUtFolkeregisterident(pdlReply: JsonNode) = pdlReply.ident("FOLKEREGISTERIDENT")

    internal fun oversett(pdlReply: JsonNode, attributter: Set<Attributt>) =
        objectMapper.createObjectNode().apply {
            håndterErrors(pdlReply)
            if (attributter.contains(Attributt.aktørId)) put("aktørId", pdlReply.ident("AKTORID"))
            if (attributter.contains(Attributt.folkeregisterident)) put("folkeregisterident", fiskUtFolkeregisterident(pdlReply))
            if (attributter.contains(Attributt.fødselsdato)) put("fødselsdato", "${pdlReply.fødselsdato()}")
            if (attributter.contains(Attributt.historiskeFolkeregisteridenter)) putArray("historiskeFolkeregisteridenter").also {
                arrayNode -> pdlReply.historiskeFolkeregisteridenter().forEach { arrayNode.add(it) }
            }
            if (attributter.contains(Attributt.dødsdato)) put("dødsdato", PdlOversetter.fiskUtDødsdato(pdlReply))
            if (attributter.contains(Attributt.kjønn)) put("kjønn", PdlOversetter.fiskUtKjønn(pdlReply))
            if (attributter.contains(Attributt.adressebeskyttelse)) put("adressebeskyttelse", PdlOversetter.fiskUtAdressebeskyttelse(pdlReply))
            if (attributter.contains(Attributt.støttes)) put("støttes", PdlOversetter.fiskUtStøttes(pdlReply))
            if (attributter.contains(Attributt.navn)) {
                val navn = pdlReply["data"]["hentPerson"]["navn"].first()
                put("fornavn", navn["fornavn"].asText())
                put("mellomnavn", navn["mellomnavn"]?.textValue())
                put("etternavn", navn["etternavn"].asText())
            }
        }

    private fun JsonNode.ident(type: String) = path("data")
        .path("hentIdenter")
        .path("identer")
        .firstOrNull { it.path("gruppe").asText() == type && !it.path("historisk").asBoolean() }
        ?.path("ident")
        ?.asText()
        ?: throw fantIkke(type)

    private fun JsonNode.fødselsdato() = path("data")
        .path("hentPerson")
        .path("foedsel")
        .firstOrNull()
        ?.path("foedselsdato")
        ?.asText()
        ?.let { LocalDate.parse(it) }
        ?: throw fantIkke("foedselsdato")

    private fun JsonNode.historiskeFolkeregisteridenter() = path("data")
        .path("hentIdenter")
        .path("identer")
        .filter { it.path("gruppe").asText() == "FOLKEREGISTERIDENT" && it.path("historisk").asBoolean() }
        .map { it.path("ident").asText() }

    private fun JsonNode.fantIkke(attributt: String): IllegalStateException {
        sikkerlogg.error("Fant ikke $attributt i svaret fra PDL:\n\t$this")
        return IllegalStateException("Fant ikke $attributt i svaret fra PDL")
    }
}

