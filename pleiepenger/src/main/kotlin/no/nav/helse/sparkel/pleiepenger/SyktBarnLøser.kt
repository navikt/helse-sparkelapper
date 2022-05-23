package no.nav.helse.sparkel.pleiepenger

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal abstract class SyktBarnLøser(
    rapidsConnection: RapidsConnection,
    private val behov: String,
    private val fomKey: String,
    private val tomKey: String,
    private vararg val kilder: SyktBarnKilde
): River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("@behovId")}
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.$fomKey", JsonNode::asLocalDate) }
            validate { it.require("$behov.$tomKey", JsonNode::asLocalDate) }
        }.register(this)
    }

    abstract fun stønadsperioder(
        fnr: String,
        fom: LocalDate,
        tom: LocalDate,
        kilde: SyktBarnKilde
    ): Set<Stønadsperiode>

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val id = packet.id()
        val behovId = packet.behovId()
        val vedtaksperiodeId = packet.vedtaksperiodeId()
        val fødselsnummer = packet.fnr()
        val fom = packet.fom()
        val tom = packet.tom()
        val sporing = arrayOf("behov" to behov, "behovId" to behovId, "vedtaksperiodeId" to vedtaksperiodeId, "id" to id)
        val sikkerSporing = sporing.map { (key, value) -> keyValue(key, value) }.plus(keyValue("fødselsnummer", fødselsnummer)).toTypedArray()

        withMDC(*sporing) {
            sikkerlogg.info("Løser {}, {}, {}, {}, {} ($fom til $tom):\n\t${packet.toJson()}", *sikkerSporing)
            try {
                val stønadsperioder = kilder.flatMap { kilde -> stønadsperioder(
                    fnr = fødselsnummer,
                    fom = fom,
                    tom = tom,
                    kilde = kilde
                ).also {
                    sikkerlogg.info("Hentet ${it.size} stønadsperiode(r) fra ${kilde.javaClass.simpleName} for {}, {}, {}, {}, {}", *sikkerSporing)
                }}.toSet()

                packet["@løsning"] = mapOf(
                    behov to stønadsperioder
                )
                context.publish(packet.toJson().also { json ->
                    sikkerlogg.info("Sender løsning for {}, {}, {}, {}, {} ($fom til $tom):\n\t$json", *sikkerSporing)
                })
            } catch (exception: Exception) {
                sikkerlogg.error("Feil ved løsing av {}, {}, {}, {}, {}: ${exception.message}", *sikkerSporing, exception)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("Forstod ikke $behov med melding:\n${problems.toExtendedReport()}")
    }

    private fun JsonMessage.fom() = get("$behov.$fomKey").asLocalDate()
    private fun JsonMessage.tom() = get("$behov.$tomKey").asLocalDate()

    private companion object {
        private fun JsonMessage.id() = get("@id").asText()
        private fun JsonMessage.behovId() = get("@behovId").asText()
        private fun JsonMessage.vedtaksperiodeId() = get("vedtaksperiodeId").asText()
        private fun JsonMessage.fnr() = get("fødselsnummer").asText()

        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private fun <T> withMDC(vararg values: Pair<String, String>, block: () -> T): T = try {
            values.forEach { (key, value) -> MDC.put(key, value) }
            block()
        } finally {
            values.forEach { (key, _) -> MDC.remove(key) }
        }
    }
}