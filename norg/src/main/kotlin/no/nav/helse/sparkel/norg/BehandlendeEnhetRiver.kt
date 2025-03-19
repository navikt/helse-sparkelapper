package no.nav.helse.sparkel.norg

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BehandlendeEnhetRiver(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireAll("@behov", listOf("HentEnhet"))
                it.forbid("@løsning")
            }
            validate {
                it.requireKey("@id")
                it.requireKey("fødselsnummer")
                it.interestedIn("hendelseId")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) = runBlocking {
        log.info(
            "Henter behandlende enhet for {}, {}",
            keyValue("hendelseId", packet["hendelseId"].asText()),
            keyValue("@id", packet["@id"].asText())
        )
        try {
            val enhet = personinfoService.finnBehandlendeEnhet(
                fødselsnummer = packet["fødselsnummer"].asText(),
                callId = packet["@id"].asText()
            )
            packet["@løsning"] = mapOf(
                "HentEnhet" to enhet
            )
            context.publish(packet.toJson())
        } catch (err: Exception) {
            log.error(
                "Feil ved håndtering av behov {} for {}: ${err.message}",
                keyValue("hendelseId", packet["hendelseId"].asText()),
                keyValue("@id", packet["@id"].asText()),
                err
            )
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerLogg.error("Forstod ikke HentEnhet-behov:\n${problems.toExtendedReport()}")
    }
}
