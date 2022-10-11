package no.nav.helse.sparkel.arbeidsgiver

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

internal class ArbeidsgiveropplysningerRiver(
    rapidsConnection: RapidsConnection
) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        val logg = LoggerFactory.getLogger(this::class.java)
        const val eventName = "trenger_opplysninger_fra_arbeidsgiver"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
        }.register(this)
    }

    private fun loggVennligPacket(packet: JsonMessage): Map<String, Any> =
        mapOf(
            "id" to packet.id,
            "@event_name" to packet["@event_name"]
        )

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        logg.info("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis:\n{}", loggVennligPacket(packet))
        sikkerlogg.info("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis med data:\n{}", packet.toJson())
    }
}
