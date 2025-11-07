package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.getOrThrow
import com.github.navikt.tbd_libs.retry.retryBlocking
import com.github.navikt.tbd_libs.spedisjon.SpedisjonClient
import io.micrometer.core.instrument.MeterRegistry
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.sparkel.arbeidsgiver.ArbeidsgiveropplysningerProducer
import org.slf4j.LoggerFactory
import org.slf4j.MDC

internal class InntektsmeldingHåndertRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: ArbeidsgiveropplysningerProducer,
    private val spedisjonClient: SpedisjonClient
) : River.PacketListener {
    private companion object {
        val logg = LoggerFactory.getLogger(this::class.java)
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val eventName = "inntektsmelding_håndtert"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", eventName) }
            validate {
                it.requireKey(
                    "@id",
                    "fødselsnummer",
                    "organisasjonsnummer",
                    "vedtaksperiodeId",
                    "inntektsmeldingId",
                    "@opprettet"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        MDC.putCloseable("meldingId", packet["@id"].asText()).use {
            logg.info("Melding mottatt")
            sikkerlogg.info("Melding mottatt:\n${packet.toJson()}")

            val hendelseId = UUID.fromString(packet["inntektsmeldingId"].asText())

            val callId = UUID.randomUUID().toString()
            sikkerlogg.info("Henter dokument for callId=$callId, inntektsmeldingId=$hendelseId")
            logg.info("Henter dokument for {}", kv("callId", callId))
            val dokumentId = try {
                retryBlocking {
                    spedisjonClient.hentMelding(hendelseId, callId).getOrThrow()
                }.eksternDokumentId
            } catch (e: Exception) {
                if (System.getenv("NAIS_CLUSTER_NAME") == "dev-gcp") {
                    logg.warn("Feil ved henting av melding fra spedisjon. Er i dev, så vi dropper eventet")
                    sikkerlogg.warn("Feil ved henting av melding fra spedisjon. Er i dev, så vi dropper eventet", e)
                    return
                } else {
                    logg.error("Feil ved henting av melding fra spedisjon")
                    sikkerlogg.error("Feil ved henting av melding fra spedisjon", e)
                    throw e
                }
            }

            val payload = packet.toInntektsmeldingHåndtertDto(dokumentId)
            arbeidsgiverProducer.send(payload)

            "Publiserte inntektsmelding_håndtert-event til helsearbeidsgiver-bro-sykepenger".let {
                logg.info(it)
                sikkerlogg.info("$it med data :\n{}", payload)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }
}
