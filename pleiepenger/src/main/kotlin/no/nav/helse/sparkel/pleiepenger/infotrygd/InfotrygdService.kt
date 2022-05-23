package no.nav.helse.sparkel.pleiepenger.infotrygd

import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.pleiepenger.Stønadsperiode
import no.nav.helse.sparkel.pleiepenger.infotrygd.InfotrygdClient.Companion.infotrygdResponseSomStønadsperioder
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class InfotrygdService(private val infotrygdClient: InfotrygdClient) {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    fun løsningForBehov(
        stønadstype: Stønadstype,
        behovId: String,
        vedtaksperiodeId: String,
        fødselsnummer: String,
        fom: LocalDate,
        tom: LocalDate
    ): List<Stønadsperiode>? = withMDC("id" to behovId, "vedtaksperiodeId" to vedtaksperiodeId) {
        try {
            val pleiepenger = infotrygdClient.hent(
                stønadstype = stønadstype,
                fnr = fødselsnummer,
                fom = fom,
                tom = tom
            )?.infotrygdResponseSomStønadsperioder() ?: return@withMDC null
            log.info(
                "løser behov: {} for {}",
                keyValue("id", behovId),
                keyValue("vedtaksperiodeId", vedtaksperiodeId)
            )
            sikkerlogg.info(
                "løser behov: {} for {}",
                keyValue("id", behovId),
                keyValue("vedtaksperiodeId", vedtaksperiodeId)
            )
            pleiepenger
        } catch (err: Exception) {
            arrayOf(log, sikkerlogg).forEach {
                it.warn(
                    "Feil ved henting av data for {}: ${err.message}",
                    keyValue("vedtaksperiodeId", vedtaksperiodeId),
                    err
                )
            }
            null
        }
    }
}

private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
    values.forEach { (key, value) -> MDC.put(key, value) }
    block()
} finally {
    values.forEach { (key, _) -> MDC.remove(key) }
}
