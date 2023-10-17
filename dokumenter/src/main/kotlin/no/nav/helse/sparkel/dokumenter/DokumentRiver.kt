package no.nav.helse.sparkel.dokumenter

import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

internal class DokumentRiver(
    rapidsConnection: RapidsConnection,
    private val søknadClient: SøknadClient,
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(DokumentRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "hent-dokument") }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("dokumentid") }
            validate { it.requireKey("dokumenttype") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke hent-dokument:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info("Leser melding ${packet["@id"]}")
        val dokumenttype = packet["dokumenttype"].asText()
        when (dokumenttype) {
            "SØKNAD" -> håndter(packet, context, søknadClient)
            else -> sikkerlogg.info(
                "uhåndtert melding {}\n$packet",
                StructuredArguments.keyValue("dokumenttype", dokumenttype)
            )
        }
    }

    private fun håndter(packet: JsonMessage, context: MessageContext, dokumentClient: DokumentClient) {
        val dokumentid = packet["dokumentid"].asText()
        val id = packet["@id"].asText()

        packet["@løsning"] = mapOf<String, Any>(
            "dokument" to dokumentClient.hentDokument(
                dokumentid = dokumentid,
            )
        )
        context.publish(packet.toJson()).also {
            sikkerlogg.info(
                "sender {} som {}",
                StructuredArguments.keyValue("id", id),
                packet.toJson()
            )
        }
    }
}
