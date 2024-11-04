package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
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
    private val speedClient: SpeedClient
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

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val hendelseId = packet["@id"].asText()
        withMDC(mapOf(
            "id" to hendelseId
        )) {
            sikkerLogg.info("løser HentIdenter")
            val kildeIdent = packet["ident"].asText()
            try {
                when (val identer = speedClient.hentFødselsnummerOgAktørId(kildeIdent, hendelseId)) {
                    is Result.Error -> {
                        sikkerLogg.warn("klarte ikke finne identer kildeIdent=$kildeIdent: ${identer.error} behov=HentIdenter melding:\n${packet.toJson()}", identer.cause)
                    }
                    is Result.Ok -> sendSvar(packet, identer.value, context)
                }
            } catch (err: Exception) {
                sikkerLogg.warn("klarte ikke finne identer kildeIdent=$kildeIdent: ${err.message} behov=HentIdenter melding:\n${packet.toJson()}", err)
            }
        }
    }

    private fun sendSvar(
        packet: JsonMessage,
        identer: IdentResponse,
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
