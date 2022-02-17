package no.nav.helse.sparkel.personinfo

import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

internal class HentIdenterLøser(
    rapidsConnection: RapidsConnection,
    private val pdlClient: PdlClient,
) : River.PacketListener {
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("HentIdenter"))
                it.rejectKey("@løsning")
                it.requireKey("@id", "ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = runBlocking {
        val hendelseId = packet["@id"].asText()
        withMDC(mapOf(
            "id" to hendelseId
        )) {
            val identer = pdlClient.hentIdenter(packet["ident"].asText(), hendelseId)
            packet["@løsning"] = mapOf(
                "HentIdenter" to mapOf(
                    "fødselsnummer" to identer.fødselsnummer,
                    "aktørId" to identer.aktørId
                )
            )
            sikkerLogg.info("løser behov=HentIdenter melding:\n${packet.toJson()}")
            context.publish(identer.fødselsnummer, packet.toJson())
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentIdenter-behov:\n${problems.toExtendedReport()}")
    }
}
