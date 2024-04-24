package no.nav.helse.sparkel.stoppknapp.kafka

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.stoppknapp.Mediator
import no.nav.helse.sparkel.stoppknapp.kafka.StoppknappMessage.Companion.tilDatabase
import org.slf4j.LoggerFactory

internal class StoppknappRiver(rapidsConnection: RapidsConnection, private val mediator: Mediator) :
    River.PacketListener {
    private val logg = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandKey("sykmeldtFnr")
                it.demandKey("status")
                it.interestedIn("arsakList")
                it.demandKey("opprettet")
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
        sikkerlogg.info("Leser stoppknapp-melding: ${packet.toJson()}")
        mediator.lagre(StoppknappMessage(packet).tilDatabase())
    }
}
