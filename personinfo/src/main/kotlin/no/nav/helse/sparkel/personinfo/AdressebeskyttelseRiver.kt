package no.nav.helse.sparkel.personinfo

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import org.slf4j.LoggerFactory

class AdressebeskyttelseRiver(rapidsConnection: RapidsConnection) : River.PacketListener {
    companion object {
        private val sikkerLog = LoggerFactory.getLogger("tjenestekall")
    }

    init {
        River(rapidsConnection).register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerLog.info("Fabs debugmelding:  ${packet.toJson()}")
    }


}
