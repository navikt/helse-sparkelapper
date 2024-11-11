package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.util.UUID
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory

internal class TrengerIkkeArbeidsgiveropplysningerRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: KafkaProducer<String, TrengerIkkeArbeidsgiveropplysningerDto>
) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        val logg = LoggerFactory.getLogger(this::class.java)
        const val eventName = "trenger_ikke_opplysninger_fra_arbeidsgiver"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
            validate { it.require("@opprettet", JsonNode::asLocalDateTime) }
            validate { it.require("vedtaksperiodeId", JsonNode::asUuid) }
            validate { it.requireKey("organisasjonsnummer", "fødselsnummer") }
        }.register(this)
    }

    private fun loggVennligPacket(packet: JsonMessage): Map<String, Any> =
        mapOf(
            "id" to packet.id,
            "@event_name" to packet["@event_name"]
        )

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        "Mottok trenger_ikke_opplysninger_fra_arbeidsgiver-event fra spleis".let {
            logg.info("$it:\n{}", loggVennligPacket(packet))
            sikkerlogg.info("$it med data:\n{}", packet.toJson())
        }
        val payload = packet.toTrengerIkkeArbeidsgiverDto()
        arbeidsgiverProducer.send(
            ProducerRecord(
                "tbd.arbeidsgiveropplysninger",
                null,
                payload.fødselsnummer,
                payload,
                listOf(RecordHeader("type", payload.meldingstype))
            )
        ).get()

        "Publiserte at vi ikke trenger forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger".let {
            logg.info(it)
            sikkerlogg.info("$it med data :\n{}", payload)
        }
    }
}

fun JsonNode.asUuid(): UUID =
    asText().let { UUID.fromString(it) }

