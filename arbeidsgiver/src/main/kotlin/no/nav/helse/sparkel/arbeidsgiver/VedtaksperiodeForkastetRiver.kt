package no.nav.helse.sparkel.arbeidsgiver

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
                    "trengerArbeidsgiveropplysninger"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val trengerArbeidsgiveropplysninger = packet["trengerArbeidsgiveropplysninger"].asBoolean()
        val tilstand = packet["tilstand"].asText()
        val fom = packet["fom"].asLocalDate()
        val tom = packet["tom"].asLocalDate()
        val fnr = packet["fødselsnummer"].asText()
        val vedtaksperiodeId = UUID.fromString(packet["vedtaksperiodeId"].asText())

        if(trengerArbeidsgiveropplysninger && (tilstand == "START" || tilstand == "AVVENTER_INFOTRYGDHISTORIKK")) {

            sikkerlogg.info("Fant en forkastet periode som trenger forespørsel. \n" +
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