package no.nav.helse.sparkel.dokumenter

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

internal class DokumentRiver(
    rapidsConnection: RapidsConnection,
    private val søknadClient: SøknadClient,
    private val inntektsmeldingClient: InntektsmeldingClient,
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(DokumentRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "hent-dokument") }
            precondition { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("dokumentId") }
            validate { it.requireKey("dokumentType") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke hent-dokument:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        log.info("Leser melding med @id=${packet["@id"].asText()}")
        sikkerlogg.info("Leser melding med @id=${packet["@id"].asText()}\n${packet.toJson()}")
        when (val dokumentType = packet["dokumentType"].asText()) {
            "SØKNAD" -> håndter(packet, context, søknadClient)
            "INNTEKTSMELDING" -> håndter(packet, context, inntektsmeldingClient)
            else -> sikkerlogg.info(
                "Uhåndtert melding, dokumentType={}, @id={}",
                dokumentType,
                packet["@id"].asText(),
            )
        }
    }

    private fun håndter(packet: JsonMessage, context: MessageContext, dokumentClient: DokumentClient) {
        val dokumentId = packet["dokumentId"].asText()
        val id = packet["@id"].asText()

        dokumentClient.hentDokument(dokumentId).fold(
            onSuccess = {
                packet["@løsning"] = mapOf("dokument" to it)
                packet.toJson().let {
                    context.publish(it)
                    log.info("Besvarte behov {} for dokumentId {}", id, dokumentId)
                    sikkerlogg.info("Svarte på behov {} for dokumentId {} med:\n{}", kv("id", id), dokumentId, it)
                }
            },
            onFailure = {
                log.warn("Gir opp å hente dokument, svarer ikke på behov $id for dokumentId $dokumentId")
            }
        )
    }
}
