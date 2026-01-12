package no.nav.helse.sparkel.aap

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class AapRiver(
    rapidsConnection: RapidsConnection,
    private val aapClient: AapClient,
    private val behov: String
) :
    River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(AapRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.periodeFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.periodeTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val behovId = packet["@id"].asText()
        val vedtaksperiodeId = packet["vedtaksperiodeId"].asText()
        withMDC(
            mapOf(
                "behovId" to behovId,
                "vedtaksperiodeId" to vedtaksperiodeId
            )
        ) {
            try {
                info("løser behov {} for {}", keyValue("vedtaksperiodeId", vedtaksperiodeId))
                håndter(packet, context)
            } catch (err: Exception) {
                warn("feil ved behov {} for {}: ${err.message}", err)
            }
        }
    }

    private fun håndter(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        val behovId = packet["@id"].asText()
        val fom = packet["$behov.periodeFom"].asLocalDate()
        val tom = packet["$behov.periodeTom"].asLocalDate()

        val json = runBlocking {
            aapClient.hentMaksimumUtenUtbetaling(fødselsnummer, fom, tom, behovId)
        }
        val aapRettigheter = json.mapCatching { maksimum ->
            maksimum.map {
                AapRettighet(
                    barnMedStonad = it["barnMedStonad"].asLong(),
                    barnetillegg = it["barnetillegg"].asLong(),
                    beregningsgrunnlag = it["beregningsgrunnlag"].asLong(),
                    dagsats = it["dagsats"].asLong(),
                    dagsatsEtterUføreReduksjon = it["dagsatsEtterUføreReduksjon"].asLong(),
                    kildesystem = it["kildesystem"].asText(),
                    opphorsAarsak = it["opphorsAarsak"].asText(),
                    periode = Periode(
                        fraOgMedDato = it["periode"]["fraOgMedDato"].asText(),
                        tilOgMedDato = it["periode"]["tilOgMedDato"].asText()
                    ),
                    rettighetsType = it["rettighetsType"].asText(),
                    saksnummer = it["saksnummer"].asText(),
                    samordningsId = it["samordningsId"].asText(),
                    status = it["status"].asText(),
                    vedtakId = it["vedtakId"].asText(),
                    vedtaksTypeKode = it["vedtaksTypeKode"].asText(),
                    vedtaksTypeNavn = it["vedtaksTypeNavn"].asText(),
                    vedtaksdato = it["vedtaksdato"].asText()
                )
            }
        }

        aapRettigheter.fold(
            onSuccess = { aapRettigheter: List<AapRettighet> ->
                packet["@løsning"] = mapOf("aapRettighetsperioder" to aapRettigheter.map { it.periode })
                context.publish(packet.toJson())
                log.info("Besvarte behov {}", behovId)
                sikkerlogg.info(
                    "Besvarte behov {}:\n{}",
                    kv("id", behovId),
                    packet.toJson()
                )
            },
            onFailure = { t: Throwable ->
                "Fikk feil ved oppslag mot representasjon".also { message ->
                    log.error("$message, se sikkerlogg for detaljer")
                    sikkerlogg.error("$message: {}", t, t)
                }
                throw t
            }
        )
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

    private fun info(format: String, vararg args: Any) {
        log.info(format, *args)
        sikkerlogg.info(format, *args)
    }

    private fun warn(format: String, vararg args: Any) {
        log.warn(format, *args)
        sikkerlogg.warn(format, *args)
    }

    data class AapRettighet(
        val barnMedStonad: Long,
        val barnetillegg: Long,
        val beregningsgrunnlag: Long,
        val dagsats: Long,
        val dagsatsEtterUføreReduksjon: Long,
        val kildesystem: String,
        val opphorsAarsak: String,
        val periode: Periode,
        val rettighetsType: String,
        val saksnummer: String,
        val samordningsId: String,
        val status: String,
        val vedtakId: String,
        val vedtaksTypeKode: String,
        val vedtaksTypeNavn: String,
        val vedtaksdato: String,
    )

    data class Periode(
        val fraOgMedDato: String,
        val tilOgMedDato: String,
    )
}
