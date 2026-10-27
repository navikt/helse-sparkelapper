package no.nav.helse.sparkel.medlemskapmock

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory
import tools.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class Medlemskap(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private const val behov = "Medlemskap"
    }

    private val medlemskapvurderinger = Medlemskapvurderinger()

    init {
        River(rapidsConnection)
            .apply {
                precondition { it.requireValue("@event_name", "mock_medlemskap_avklaring") }
                validate {
                    it.requireKey("ident", "medlemskapVerdi")
                    it.interestedIn("aarsaker")
                }
            }.register(medlemskapvurderinger)

        River(rapidsConnection)
            .apply {
                precondition { it.requireAll("@behov", listOf(behov)) }
                precondition { it.forbid("@løsning") }
                validate { it.requireKey("@id") }
                validate { it.requireKey("fødselsnummer") }
                validate { it.requireKey("vedtaksperiodeId") }
                validate { it.require("$behov.medlemskapPeriodeFom", JsonNode::asLocalDate) }
                validate { it.require("$behov.medlemskapPeriodeTom", JsonNode::asLocalDate) }
            }.register(this)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        sikkerlogg.error("forstod ikke $behov:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val fødselsnummer = packet["fødselsnummer"].asString()
        val vurdering = medlemskapvurderinger.vurderMedlemskap(fødselsnummer)
        packet["@løsning"] = mapOf<String, Any>(behov to vurdering.somLøsning())
        context.publish(packet.toJson())
        sikkerlogg.info(
            "Sender hardkodet svar for behov {}:\n{}",
            keyValue("id", packet["@id"].asString()),
            packet.toJson(),
        )
    }
}

private class Medlemskapvurderinger : River.PacketListener {
    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val formatter = DateTimeFormatter.ofPattern("ddMMyy")
    }

    private val vurderinger = mutableMapOf<String, Medlemskapvurdering>()

    fun vurderMedlemskap(ident: String) = vurderinger.remove(ident) ?: gyldigFødselsnummer(ident)

    private fun gyldigFødselsnummer(fødselsnummer: String): Medlemskapvurdering =
        try {
            LocalDate.parse(fødselsnummer.substring(0, 6), formatter)
            Medlemskapvurdering("JA")
        } catch (_: Exception) {
            Medlemskapvurdering("UAVKLART")
        }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        sikkerlogg.error("forstod ikke mock_medlemskap_avklaring:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
        val ident = packet["ident"].asString()
        val vurdering =
            Medlemskapvurdering(
                svar = packet["medlemskapVerdi"].asString(),
                aarsaker =
                    packet["aarsaker"].values().map {
                        Årsak(
                            regelId = it.path("regelId").asString(),
                            forklaringPaaUavklart = it.path("forklaringPaaUavklart").asString(),
                        )
                    },
            )
        sikkerlogg.info("forbereder medlemskapvurdering for $ident=$vurdering")
        vurderinger[ident] = vurdering
    }
}

private data class Medlemskapvurdering(
    val svar: String,
    val aarsaker: List<Årsak> = emptyList(),
) {
    fun somLøsning(): Map<String, Any> =
        mapOf(
            "resultat" to
                buildMap {
                    put("svar", svar)
                    if (svar != "JA") {
                        put(
                            "aarsaker",
                            aarsaker.map { mapOf("regelId" to it.regelId, "forklaringPaaUavklart" to it.forklaringPaaUavklart) },
                        )
                    }
                },
        )
}

private data class Årsak(
    val regelId: String,
    val forklaringPaaUavklart: String,
)
