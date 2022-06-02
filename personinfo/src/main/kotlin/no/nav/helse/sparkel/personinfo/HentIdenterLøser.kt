package no.nav.helse.sparkel.personinfo

import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
            try {
                val kildeIdent = packet["ident"].asText()
                when (val identer: IdenterResultat = pdlClient.hentIdenter(kildeIdent, hendelseId)) {
                    is Identer -> sendSvar(packet, identer, context)
                    is FantIkkeIdenter -> sikkerLogg.warn("klarte ikke finne identer: $kildeIdent behov=HentIdenter melding:\n${packet.toJson()}")
                }

            } catch (err: Exception) {
                sikkerLogg.warn("klarte ikke finne identer: ${err.message} behov=HentIdenter melding:\n${packet.toJson()}", err)
            }
        }
    }

    private fun sendSvar(
        packet: JsonMessage,
        identer: Identer,
        context: MessageContext
    ) {
        packet["@løsning"] = mapOf(
            "HentIdenter" to mapOf(
                "fødselsnummer" to identer.fødselsnummer,
                "aktørId" to identer.aktørId
            )
        )
        sikkerLogg.info("løser behov=HentIdenter melding:\n${packet.toJson()}")
        context.publish(identer.fødselsnummer, packet.toJson())
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentIdenter-behov:\n${problems.toExtendedReport()}")
    }
}
