package no.nav.helse.sparkel.medlemskapmock

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

internal class Medlemskap(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private const val behov = "Medlemskap"
    }

    private val medlemskapvurderinger = Medlemskapvurderinger()

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "mock_medlemskap_avklaring") }
            validate {
                it.requireKey("ident", "medlemskapVerdi")
            }
        }.register(medlemskapvurderinger)

        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("vedtaksperiodeId") }
            validate { it.require("$behov.medlemskapPeriodeFom", JsonNode::asLocalDate) }
            validate { it.require("$behov.medlemskapPeriodeTom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val fødselsnummer = packet["fødselsnummer"].asText()
        packet["@løsning"] = mapOf<String, Any>(
            behov to mapOf(
                "resultat" to mapOf(
                    "svar" to medlemskapvurderinger.vurderMedlemskap(fødselsnummer)
                )
            )
        )
        context.publish(packet.toJson())
        sikkerlogg.info(
            "Sender hardkodet svar for behov {}:\n{}",
            keyValue("id", packet["@id"].asText()),
            packet.toJson()
        )
    }
}

private class Medlemskapvurderinger : River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val formatter = DateTimeFormatter.ofPattern("ddMMyy")
    }

    private val vurderinger = mutableMapOf<String, String>()

    fun vurderMedlemskap(ident: String) = vurderinger.remove(ident) ?: gyldigFødselsnummer(ident)

    private fun gyldigFødselsnummer(fødselsnummer: String): String = try {
        LocalDate.parse(fødselsnummer.substring(0, 6), formatter)
        "JA"
    } catch (e: Exception) {
        "UAVKLART"
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke mock_medlemskap_avklaring:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val ident = packet["ident"].asText()
        val svar = packet["medlemskapVerdi"].asText()
        sikkerlogg.info("forbereder medlemskapvurdering for $ident=$svar")
        vurderinger[ident] = svar
    }
}
