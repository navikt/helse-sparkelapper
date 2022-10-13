package no.nav.helse.sparkel.arbeidsgiver

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.arbeidsgiver.ArbeidsgiveropplysningerDTO.Companion.tilArbeidsgiveropplysningerDTO
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory

internal class ArbeidsgiveropplysningerRiver(
    private val rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: KafkaProducer<String, ArbeidsgiveropplysningerDTO>

) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        val logg = LoggerFactory.getLogger(this::class.java)
        const val eventName = "trenger_opplysninger_fra_arbeidsgiver"
    }


    //TODO: oppdater validering med orgnr etc
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
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
        "Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis".let {
            logg.info("$it:\n{}", loggVennligPacket(packet))
            sikkerlogg.info("$it med data:\n{}", packet.toJson())
        }

        val payload = packet.tilArbeidsgiveropplysningerDTO()
        arbeidsgiverProducer.send(
            ProducerRecord(
                "arbeidsgiveropplysninger",
                null,
                payload.fødselsnummer,
                payload,
                listOf(RecordHeader("type", payload.meldingstype))
            )
        ).get()

        "Publiserte forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger".let {
            logg.info(it)
            sikkerlogg.info("$it med data :\n{}", payload)
        }

        rapidsConnection.publish(
            JsonMessage.newMessage(
                mapOf(
                    "@event_name" to "publisert_forespørsel_om_arbeidsgiveropplyninger"
                )
            ).toJson()
        )
    }
}
