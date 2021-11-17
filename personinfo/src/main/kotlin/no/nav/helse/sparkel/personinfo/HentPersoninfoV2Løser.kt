package no.nav.helse.sparkel.personinfo

import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class HentPersoninfoV2Løser(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("HentPersoninfo"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer", "spleisBehovId", "@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = runBlocking {
        sikkerLogg.info("mottok melding: ${packet.toJson()}")
        val behovId = packet["@id"].asText()
        val spleisBehovId = packet["spleisBehovId"].asText()
        val fnr = packet["fødselsnummer"].asText()
        try {
            packet["@løsning"] = mapOf("HentPersoninfo" to personinfoService.løsningForPersoninfo(behovId, spleisBehovId, fnr))
            val løsningJson = packet.toJson()
            sikkerLogg.info(
                "sender svar {} for {}:\n\t{}",
                keyValue("id", behovId),
                keyValue("spleisBehovId", spleisBehovId),
                keyValue("løsning", løsningJson)
            )
        } catch (e: Exception) {
            log.warn(
                "Feil under løsing av personinfo-behov {} for {}: ${e.message}",
                keyValue("id", behovId),
                keyValue("spleisBehovId", spleisBehovId),
                e
            )
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentPersoninfo-behov:\n${problems.toExtendedReport()}")
    }
}
