package no.nav.helse.sparkel.personinfo.v3

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal interface HentPersoninfoV3PDLClient {
    fun hent(ident: String, callId: String, attributter: Set<Attributt>): JsonNode
}

internal class HentPersoninfoV3Løser(
    rapidsConnection: RapidsConnection,
    private val pdl: HentPersoninfoV3PDLClient
) : River.PacketListener {


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
            sikkerLogg.info("Mottok HentPersoninfoV3:\n${packet.toJson()}")

            try {
                val ident = packet["HentPersoninfoV3.ident"].asText()
                val attributter = packet["HentPersoninfoV3.attributter"].somAttributter()

                val pdlReply = pdl.hent(
                    ident = ident,
                    callId = behovId,
                    // 'folkeregisterident' må alltid hentes selv om det hverken er etterspurt eller mappes ut i 'løsning'.
                    // Løsningen på kafka skal keyes på 'folkeregisterident' som ikke nødvendigvis == 'HentPersoninfoV3.ident'
                    attributter = attributter.plus(Attributt.folkeregisterident)
                )
                val folkeregisterident = PdlReplyOversetter.fiskUtFolkeregisterident(pdlReply)
                val løsning = PdlReplyOversetter.oversett(pdlReply, attributter)

                packet["@løsning"] = mapOf("HentPersoninfoV3" to løsning)
                sikkerLogg.info("Løsning for HentPersoninfoV3:\n${packet.toJson()}")
                context.publish(folkeregisterident, packet.toJson())
            } catch (e: Exception) {
                sikkerLogg.warn("Feil under løsing av HentPersoninfoV3: ${e.message}", e)
                log.warn("Feil under løsing av HentPersoninfoV3: ${e.message}", e)
            }
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentPersoninfoV3:\n${problems.toExtendedReport()}")
    }

    private companion object {
        private val log: Logger = LoggerFactory.getLogger(HentPersoninfoV3Løser::class.java)
        private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")
        private fun JsonNode.somAttributter() = mapNotNull { Attributt.fromString(it.asText()) }.toSet()
    }
}
