package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert

import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert.InntektsmeldingRegistrertRepository
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory

internal class InntektsmeldingHåndertRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: KafkaProducer<String, InntektsmeldingHåndtertDto>,
    private val inntektsmeldingRegistrertRepository: InntektsmeldingRegistrertRepository
) : River.PacketListener {
    private companion object {
        val logg = LoggerFactory.getLogger(this::class.java)
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val eventName = "inntektsmelding_håndtert"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
            validate {
                it.requireKey(
                    "fødselsnummer",
                    "organisasjonsnummer",
                    "vedtaksperiodeId",
                    "inntektsmeldingId"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
            val hendelseId = UUID.fromString(packet["inntektsmeldingId"].asText())
            val dokumentId = inntektsmeldingRegistrertRepository.finnDokumentId(hendelseId)

            if (dokumentId == null ) {
                logg.error("Klarte ikke å finne en dokumentId som er knyttet til inntektsmeldinges hendelsesId i inntektsmelding_håndtert-eventet.")
                sikkerlogg.error("Klarte ikke å finne en dokumentId som er knyttet til inntektsmeldinges hendelsesId i inntektsmelding_håndtert-eventet.\n ${packet.toJson()}")
            }

            val payload = packet.toInntektsmeldingHåndtertDto(dokumentId)
            arbeidsgiverProducer.send(
                ProducerRecord(
                    "tbd.arbeidsgiveropplysninger",
                    null,
                    payload.fødselsnummer,
                    payload,
                    listOf(RecordHeader("type", payload.meldingstype))
                )
            ).get()

            "Publiserte inntektsmelding_håndtert-event til helsearbeidsgiver-bro-sykepenger".let {
                logg.info(it)
                sikkerlogg.info("$it med data :\n{}", payload)
            }
        }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }
}