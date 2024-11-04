package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.PersonResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class PersoninfoService(private val pdlClient: PdlClient, private val speedClient: SpeedClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForPersoninfo(callId: String, ident: String): Result<PersonResponse> {
        return speedClient.hentPersoninfo(ident, callId)
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
