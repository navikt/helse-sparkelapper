package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.slf4j.LoggerFactory

internal class VedtaksperiodeForkastetRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: KafkaProducer<String, TrengerArbeidsgiveropplysningerDto>
) : River.PacketListener {
    private companion object {
        val logg = LoggerFactory.getLogger(this::class.java)
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val eventName = "vedtaksperiode_forkastet"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
            validate { it.rejectValues("organisasjonsnummer", listOf("ARBEIDSLEDIG", "SELVSTENDIG", "FRILANS")) }
            validate {
                it.requireKey(
                    "fødselsnummer",
                    "organisasjonsnummer",
                    "vedtaksperiodeId",
                    "tilstand",
                    "fom",
                    "tom",
                    "trengerArbeidsgiveropplysninger",
                    "sykmeldingsperioder",
                    "@opprettet"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val tilstand = packet["tilstand"].asText()
        val trengerArbeidsgiveropplysninger = packet["trengerArbeidsgiveropplysninger"].asBoolean()

        if(trengerArbeidsgiveropplysninger && (tilstand == "START" || tilstand == "AVVENTER_INFOTRYGDHISTORIKK")) {
            "Fant en forkastet periode som trenger forespørsel".let {
                logg.info(it)
                sikkerlogg.info("$it med data :\n{}", packet.toJson())
            }

            val payload = packet.toBegrensetTrengerArbeidsgiverDto()
            arbeidsgiverProducer.send(
                ProducerRecord(
                    "tbd.arbeidsgiveropplysninger",
                    null,
                    payload.fødselsnummer,
                    payload,
                    listOf(RecordHeader("type", payload.meldingstype))
                )
            ).get()

            "Publiserte begrenset forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger".let {
                logg.info(it)
                sikkerlogg.info("$it med data :\n{}", payload)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }
}