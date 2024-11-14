package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.IdentResponse
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class HentIdenterLøser(
    rapidsConnection: RapidsConnection,
    private val speedClient: SpeedClient
) : River.PacketListener {
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireAll("@behov", listOf("HentIdenter"))
                it.forbid("@løsning")
            }
            validate {
                it.requireKey("@id", "ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
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

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerLogg.error("Forstod ikke HentIdenter-behov:\n${problems.toExtendedReport()}")
    }
}
