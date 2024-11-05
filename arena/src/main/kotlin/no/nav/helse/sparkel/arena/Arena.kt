package no.nav.helse.sparkel.arena

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class Arena(
    rapidsConnection: RapidsConnection,
    private val ytelsekontraktClient: YtelsekontraktClient,
    private val meldekortUtbetalingsgrunnlagClient: MeldekortUtbetalingsgrunnlagClient,
    private val ytelsetype: String,
    private val tematype: String,
    private val behov: String
) :
    River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(Arena::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.periodeFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.periodeTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behovId = packet["@id"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText()
        withMDC(mapOf(
            "behovId" to behovId,
            "vedtaksperiodeId" to vedtaksperiodeId
        )) {
            try {
                packet.info("løser behov {} for {}", keyValue("id", behovId), keyValue("vedtaksperiodeId", vedtaksperiodeId))
                håndter(packet, context)
            } catch (err: Exception) {
                packet.warn("feil ved behov {} for {}: ${err.message}", keyValue("id", behovId), keyValue("vedtaksperiodeId", vedtaksperiodeId), err)
            }
        }
    }

    private fun håndter(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val søkevindu = packet["$behov.periodeFom"].asLocalDate() to packet["$behov.periodeTom"].asLocalDate()

        packet["@løsning"] = mapOf(
            behov to mapOf(
                "vedtaksperioder" to hentYtelsekontraktV2(fødselsnummer, søkevindu),
                "meldekortperioder" to (hentMeldekortUtbetalingsgrunnlagV2(fødselsnummer, søkevindu, tematype) ?: emptyList())
            )
        )
        context.publish(packet.toJson()).also {
            sikkerlogg.info("sender {} som {}", keyValue("id", packet["@id"].asText()), packet.toJson())
        }
    }

    private fun hentYtelsekontraktV2(fødselsnummer: String, søkevindu: Pair<LocalDate, LocalDate>) =
        ytelsekontraktClient.hentYtelsekontrakt(fødselsnummer, søkevindu.first, søkevindu.second)
            .hentYtelseskontraktListeResponse
            .response
            .ytelseskontraktListe
            .filter { ytelsetype == it.ytelsestype }
            .flatMap {
                it.vedtaksliste
                    .filter { it.periodetypeForYtelse != "Stans" }
                    .filter { it.vedtaksperiode.fom != null }
                    .filterNot { it.vedtaksperiode.tom != null && it.vedtaksperiode.tom isOneDayBefore it.vedtaksperiode.fom!! }
                    .map {
                        mapOf(
                            "fom" to it.vedtaksperiode.fom,
                            "tom" to (it.vedtaksperiode.tom ?: LocalDate.now())
                        )
                    }
            }

    private infix fun LocalDate.isOneDayBefore(other: LocalDate) = this.plusDays(1L) == other

    private fun hentMeldekortUtbetalingsgrunnlagV2(fødselsnummer: String, søkevindu: Pair<LocalDate, LocalDate>, tematype: String) =
        meldekortUtbetalingsgrunnlagClient.hentMeldekortutbetalingsgrunnlag(tematype, fødselsnummer, søkevindu.first, søkevindu.second)
            .finnMeldekortResponse
            .response
            ?.meldekortUtbetalingsgrunnlagListe
            ?.flatMap { sak ->
                sak.vedtaksliste.flatMap { vedtak ->
                    vedtak.meldekortliste.map { meldekort ->
                        mapOf(
                            "fom" to meldekort.meldekortperiode.fom,
                            "tom" to meldekort.meldekortperiode.tom,
                            "dagsats" to meldekort.dagsats,
                            "beløp" to meldekort.beløp,
                            "utbetalingsgrad" to meldekort.utbetalingsgrad
                        )
                    }
                }
            }
    private fun withMDC(context: Map<String, String>, block: () -> Unit) {
        val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
        try {
            MDC.setContextMap(contextMap + context)
            block()
        } finally {
            MDC.setContextMap(contextMap)
        }
    }

    private fun JsonMessage.info(format: String, vararg args: Any) {
        log.info(format, *args)
        sikkerlogg.info(format, *args)
    }

    private fun JsonMessage.warn(format: String, vararg args: Any) {
        log.warn(format, *args)
        sikkerlogg.warn(format, *args)
    }
}
