package no.nav.helse.sparkel.stoppknapp

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

internal class StoppknappRiver(rapidsConnection: RapidsConnection, private val mediator: Mediator) :
    River.PacketListener {
    private companion object {
        private val logg = LoggerFactory.getLogger(this::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAny("@event_name", listOf("test_stoppknapp_river"))
//                it.demandKey("veilederIdent")
//                it.demandKey("sykmeldtFnr")
//                it.demandKey("status")
//                it.demandKey("virksomhetNr")
//                it.demandKey("enhetNr")
//                it.demandKey("opprettet")
            }
        }.register(this)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        logg.error("Forstod ikke stoppknapp-melding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        logg.info("Leser stoppknapp-melding: ${packet.toJson()}")
    }
}
