package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import no.nav.helse.sparkel.personinfo.PdlOversetter.håndterErrors
import org.slf4j.LoggerFactory

internal object PdlReplyOversetter {
    private val objectMapper = jacksonObjectMapper()
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    internal fun oversett(pdlReply: JsonNode, attributter: Set<Attributt>) =
        objectMapper.createObjectNode().apply {
            håndterErrors(pdlReply)
            if (attributter.contains(Attributt.aktørId)) put("aktørId", pdlReply.ident("AKTORID"))
            if (attributter.contains(Attributt.folkeregisterident)) put("folkeregisterident", pdlReply.ident("FOLKEREGISTERIDENT"))
            if (attributter.contains(Attributt.fødselsdato)) put("fødselsdato", "${pdlReply.fødselsdato()}")
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

