package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.sparkel.arbeidsgiver.ArbeidsgiveropplysningerProducer
import org.slf4j.LoggerFactory

internal class TrengerArbeidsgiveropplysningerRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: ArbeidsgiveropplysningerProducer
) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        val logg = LoggerFactory.getLogger(this::class.java)
        const val eventName = "trenger_opplysninger_fra_arbeidsgiver"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", eventName) }
            precondition { it.forbidValues("organisasjonsnummer", listOf("ARBEIDSLEDIG", "SELVSTENDIG", "FRILANS")) }
            validate { it.require("@opprettet", JsonNode::asLocalDateTime) }
            validate { it.require("skjæringstidspunkt", JsonNode::asLocalDate) }
            validate { it.requireArray("sykmeldingsperioder") {
                require("fom", JsonNode::asLocalDate)
                require("tom", JsonNode::asLocalDate)
            }}
            validate { it.requireArray("egenmeldingsperioder") {
                require("fom", JsonNode::asLocalDate)
                require("tom", JsonNode::asLocalDate)
            }}
            validate { it.requireArray("førsteFraværsdager") {
                require("organisasjonsnummer", JsonNode::asText)
                require("førsteFraværsdag", JsonNode::asLocalDate)
            }}
            validate { it.require("forespurteOpplysninger", JsonNode::validateForespurteOpplysninger) }
            validate { it.requireKey("organisasjonsnummer", "fødselsnummer", "vedtaksperiodeId") }
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
        "Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis".let {
            logg.info("$it:\n{}", loggVennligPacket(packet))
            sikkerlogg.info("$it med data:\n{}", packet.toJson())
        }
        val payload = packet.toKomplettTrengerArbeidsgiveropplysningerDto()
        arbeidsgiverProducer.send(payload)

        "Publiserte komplett forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger".let {
            logg.info(it)
            sikkerlogg.info("$it med data :\n{}", payload)
        }
    }
}

private fun JsonNode.validateForespurteOpplysninger() = all { it.asForespurtOpplysning() != null }
