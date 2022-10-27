package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import org.slf4j.LoggerFactory

internal class ArbeidsgiveropplysningerRiver(
    private val rapidsConnection: RapidsConnection

) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        val logg = LoggerFactory.getLogger(this::class.java)
        const val eventName = "opplysninger_fra_arbeidsgiver"
        val objectMapper = jacksonObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModules(JavaTimeModule())
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
            validate { it.require("@opprettet", JsonNode::asLocalDateTime) }
            validate { it.require("fom", JsonNode::asLocalDate) }
            validate { it.require("tom", JsonNode::asLocalDate) }
            validate { it.requireKey("@id", "organisasjonsnummer", "fødselsnummer", "arbeidsgiveropplysninger") }
            validate {
                it.interestedIn(
                    "arbeidsgiveropplysninger.periode",
                    "arbeidsgiveropplysninger.refusjon",
                    "arbeidsgiveropplysninger.inntekt"
                )
            }
        }.register(this)
    }

    private fun loggVennligPacket(packet: JsonMessage): Map<String, Any> =
        mapOf(
            "id" to packet.id,
            "@event_name" to packet["@event_name"]
        )

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        "Mottok $eventName-event fra helsearbeidsgiver-bro-sykepenger".let {
            logg.info("$it:\n{}", loggVennligPacket(packet))
            sikkerlogg.info("$it med data:\n{}", packet.toJson())
        }
        val payload = ArbeidsgiveropplysningerDTO(
            organisasjonsnummer = packet["organisasjonsnummer"].asText(),
            fødselsnummer = packet["fødselsnummer"].asText(),
            fom = packet["fom"].asLocalDate(),
            tom = packet["tom"].asLocalDate(),
            arbeidsgiveropplysninger = OpplysningerDTO(packet["arbeidsgiveropplysninger"]),
            opprettet = packet["@opprettet"].asLocalDateTime()
        ).toMap().also {
            it["@id"] = packet["@id"]
            it["@event_name"] = "opplysninger_fra_arbeidsgiver_2"
        }

        rapidsConnection.publish(payload.toJson())
        "Publiserte $eventName-event til Spleis".let {
            logg.info("$it:\n{}", loggVennligPacket(packet))
            sikkerlogg.info("$it med data:\n{}", packet.toJson())
        }

    }

    private fun Map<String, Any>.toJson(): String = objectMapper.valueToTree<JsonNode>(this).toString()

    private fun ArbeidsgiveropplysningerDTO.toMap(): MutableMap<String, Any> = mutableMapOf(
        "organisasjonsnummer" to organisasjonsnummer,
        "fødselsnummer" to fødselsnummer,
        "fom" to fom,
        "tom" to tom,
        "arbeidsgiveropplysninger" to arbeidsgiveropplysninger,
        "@opprettet" to opprettet
    )

}
