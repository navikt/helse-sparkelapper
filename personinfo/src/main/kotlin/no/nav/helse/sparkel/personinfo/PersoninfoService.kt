package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class PersoninfoService(private val pdlClient: PdlClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForBehov(
        behovId: String,
        vedtaksperiodeId: String,
        fødselsnummer: String
    ): JsonNode = withMDC("id" to behovId, "vedtaksperiodeId" to vedtaksperiodeId) {
        val pdlRespons = pdlClient.hentPersoninfo(fødselsnummer, behovId)
        log.info(
            "løser behov: {} for {}",
            keyValue("id", behovId),
            keyValue("vedtaksperiodeId", vedtaksperiodeId)
        )
        sikkerlogg.info(
            "løser behov: {} for {}",
            keyValue("id", behovId),
            keyValue("vedtaksperiodeId", vedtaksperiodeId),
            keyValue("svarFraPDL", pdlRespons.toString())
        )
        PdlInterpreter().interpret(pdlRespons)
    }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
