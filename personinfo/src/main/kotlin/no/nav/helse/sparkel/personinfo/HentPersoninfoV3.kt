package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import no.nav.helse.sparkel.personinfo.PdlOversetter.håndterErrors
import org.slf4j.LoggerFactory

internal object HentPersoninfoV3 {
    private val objectMapper = jacksonObjectMapper()
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    internal fun oversett(pdlReply: JsonNode, attributter: Set<String>) =
        objectMapper.createObjectNode().apply {
            håndterErrors(pdlReply)
            if (attributter.contains("aktørId")) put("aktørId", pdlReply.ident("AKTORID"))
            if (attributter.contains("folkeregisterident")) put("folkeregisterident", pdlReply.ident("FOLKEREGISTERIDENT"))
            if (attributter.contains("fødselsdato")) put("fødselsdato", "${pdlReply.fødselsdato()}")
        }

    private fun JsonNode.ident(type: String) = path("data")
        .path("hentIdenter")
        .path("identer")
        .firstOrNull { it.path("gruppe").asText() == type }
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

    private fun JsonNode.fantIkke(attributt: String): IllegalStateException {
        sikkerlogg.error("Fant ikke $attributt i svaret fra PDL:\n\t$this")
        return IllegalStateException("Fant ikke $attributt i svaret fra PDL")
    }
}

