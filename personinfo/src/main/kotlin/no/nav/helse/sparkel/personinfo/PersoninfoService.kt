package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class PersoninfoService(private val pdlClient: PdlClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForPersoninfo(
            callId: String,
            ident: String
    ): JsonNode {
        val pdlRespons = pdlClient.hentPersoninfo(ident, callId)
        return PdlOversetter.oversettPersoninfo(ident, pdlRespons)
    }

    fun løsningForVergemål(
        behovId: String,
        fødselsnummer: String
    ): Vergemålløser.Resultat =
        withMDC("id" to behovId, "hendelseId" to behovId) {
            val pdlRespons = pdlClient.hentVergemål(fødselsnummer, behovId)
            log.info(
                "løser behov Vergemål {}",
                keyValue("id", behovId),
            )
            PdlOversetter.oversetterVergemålOgFullmakt(pdlRespons)
        }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
