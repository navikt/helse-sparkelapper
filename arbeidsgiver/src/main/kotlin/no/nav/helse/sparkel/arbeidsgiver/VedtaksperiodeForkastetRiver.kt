package no.nav.helse.sparkel.arbeidsgiver

import java.time.temporal.ChronoUnit
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import org.slf4j.LoggerFactory

internal class VedtaksperiodeForkastetRiver(rapidsConnection: RapidsConnection) : River.PacketListener {
    private companion object {
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val eventName = "vedtaksperiode_forkastet"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", eventName) }
            validate {
                it.requireKey(
                    "fødselsnummer",
                    "vedtaksperiodeId",
                    "tilstand",
                    "fom",
                    "tom",
                    "forlengerSpleisEllerInfotrygd"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val forlengerSpleisEllerInfotrygd = packet["forlengerSpleisEllerInfotrygd"].asBoolean()
        val tilstand = packet["tilstand"].asText()
        val fom = packet["fom"].asLocalDate()
        val tom = packet["tom"].asLocalDate()
        val fnr = packet["fødselsnummer"].asText()
        val vedtaksperiodeId = UUID.fromString(packet["vedtaksperiodeId"].asText())

        if(forlengerSpleisEllerInfotrygd && (tilstand == "START" || tilstand == "AVVENTER_INFOTRYGDHISTORIKK")) {
            val dagerMellom = ChronoUnit.DAYS.between(fom, tom)
            if(dagerMellom <= 15) {
                sikkerlogg.info("Se her ja, dette kan være en kort periode som ikke trenger å sende ut forespørsel. \n" +
                    "dager mellom: $dagerMellom, \n" +
                    "fnr: $fnr, \n" +
                    "vedtaksperiode: $vedtaksperiodeId, \n" +
                    "tilstand: $tilstand"
                )
            }

            sikkerlogg.info("Fant en forkastet periode som vi tror trenger forespørsel. \n" +
                "dager mellom: $dagerMellom, \n" +
                "fnr: $fnr, \n" +
                "vedtaksperiode: $vedtaksperiodeId, \n" +
                "tilstand: $tilstand"
            )
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }
}