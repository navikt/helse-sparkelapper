package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import kotlinx.coroutines.runBlocking
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
                it.demandAll("@behov", listOf("HentPersoninfoV2"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer")
                it.interestedIn("HentPersoninfoV2.ident", "spleisBehovId", "@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = runBlocking {
        val behovId = packet["@id"].asText()
        val spleisBehovId = packet["spleisBehovId"].asText()
        withMDC(mapOf(
                "spleisBehovId" to spleisBehovId,
                "behovId" to behovId
        )) {
            sikkerLogg.info("mottok melding: ${packet.toJson()}")
            val ident = packet["HentPersoninfoV2.ident"].takeUnless { it.isMissingOrNull() } ?: packet["fødselsnummer"]

            try {
                val løsning = if (ident.isArray) {
                    ObjectMapper().createArrayNode().apply {
                        (ident as ArrayNode).forEach {
                            this.add(personinfoService.løsningForPersoninfo(behovId, it.asText()))
                        }
                    }
                } else {
                    personinfoService.løsningForPersoninfo(behovId, ident.asText())
                }

                packet["@løsning"] = mapOf("HentPersoninfoV2" to løsning)
                context.publish(packet.toJson())

            } catch (e: Exception) {
                sikkerLogg.warn("Feil under løsing av personinfo-behov: ${e.message}", e)
                log.warn("Feil under løsing av personinfo-behov: ${e.message}", e)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentPersoninfoV2-behov:\n${problems.toExtendedReport()}")
    }
}
