package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class PersoninfoService(private val pdlClient: PdlClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForDødsinfo(
        behovId: String,
        vedtaksperiodeId: String,
        fødselsnummer: String
    ): JsonNode = withMDC("id" to behovId, "vedtaksperiodeId" to vedtaksperiodeId) {
        val pdlRespons = pdlClient.hentDødsdato(fødselsnummer, behovId)
        log.info(
            "løser behov Dødsinfo: {} for {}",
            keyValue("id", behovId),
            keyValue("vedtaksperiodeId", vedtaksperiodeId)
        )
        sikkerlogg.info(
            "løser behov Dødsinfo: {} for {} svarFraPDL=$pdlRespons",
            keyValue("id", behovId),
            keyValue("vedtaksperiodeId", vedtaksperiodeId)
        )
        PdlOversetter.oversettDødsdato(pdlRespons)
    }

    fun løsningForPersoninfo(
        behovId: String,
        spleisBehovId: String,
        fødselsnummer: String
    ): JsonNode = withMDC("id" to behovId, "spleisBehovId" to spleisBehovId) {
        val pdlRespons = pdlClient.hentPersoninfo(fødselsnummer, behovId)
        log.info(
            "løser behov HentPersoninfoV2: {} for {}",
            keyValue("id", behovId),
            keyValue("spleisBehovId", spleisBehovId)
        )
        PdlOversetter.oversettPersoninfo(pdlRespons)
    }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
