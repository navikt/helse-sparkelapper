package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface HentPersoninfoV3PDLClient {
    fun hentPersoninfoV3(ident: String, callId: String): JsonNode
}

internal class HentPersoninfoV3Løser(
    rapidsConnection: RapidsConnection,
    private val pdl: HentPersoninfoV3PDLClient
) : River.PacketListener {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("HentPersoninfoV3"))
                it.rejectKey("@løsning")
                it.requireKey("HentPersoninfoV3.ident", "HentPersoninfoV3.attributter", "@behovId", "@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = runBlocking {
        val behovId = packet["@behovId"].asText()
        val id = packet["@id"].asText()
        withMDC(mapOf(
                "id" to id,
                "behovId" to behovId
        )) {
            sikkerLogg.info("mottok HentPersoninfoV3-melding:\n${packet.toJson()}")

            try {
                val ident = packet["HentPersoninfoV3.ident"].asText()
                val attributter = packet["HentPersoninfoV3.attributter"].map { node ->
                    node.asText()
                }
                val personInfo = pdl.hentPersoninfoV3(ident = ident, callId = behovId)
                val løsning = HentPersoninfoV3.oversett(personInfo, attributter.toSet())

                packet["@løsning"] = mapOf("HentPersoninfoV3" to løsning)
                sikkerLogg.info("Løsning for HentPersoninfoV3:\n${packet.toJson()}")
                context.publish(packet.toJson())

            } catch (e: Exception) {
                sikkerLogg.warn("Feil under løsing av HentPersoninfoV3: ${e.message}", e)
                log.warn("Feil under løsing av HentPersoninfoV3: ${e.message}", e)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentPersoninfoV3:\n${problems.toExtendedReport()}")
    }
}
