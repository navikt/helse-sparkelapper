package no.nav.helse.sparkel.sputnik

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.sputnik.abakus.AbakusClient
import no.nav.helse.sparkel.sputnik.Stønad.Companion.abakusYtelser
import no.nav.helse.sparkel.sputnik.Stønad.Companion.omsluttendePeriode
import org.slf4j.LoggerFactory

internal class Sputnik(
    rapidsConnection: RapidsConnection,
    private val abakusClient: AbakusClient
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.requireKey("fødselsnummer", "@behov") }
            validate { it.rejectKey("@løsning") }
            validate { packet -> packet.demand("@behov") { Stønad.harRelevanteBehov(packet) } }
            validate { Stønad.validate(it) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = try {
        val stønader = Stønad.stønaderSomSkalLøses(packet)
        sikkerlogg.info("Mottok behov for $stønader:\n ${packet.toJson()}")
        val (fom, tom) = stønader.omsluttendePeriode(packet)
        val abakusYtelser = stønader.abakusYtelser()

        val fødselsnummer = packet["fødselsnummer"].asText()
        val stønadsperioder = abakusClient.hent(fødselsnummer, fom, tom, *abakusYtelser)

        stønader.forEach { stønad ->
            stønad.leggTilLøsning(packet, stønadsperioder)
            packet.toJson().let { json ->
                sikkerlogg.info("Sender løsning for $stønad:\n $json")
                //context.publish(fødselsnummer, json)
            }
        }
    } catch (exception: Exception) {
        sikkerlogg.error("Feil ved løsing av behov:\n ${packet.toJson()}", exception)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("Forstod ikke behovet: \n${problems.toExtendedReport()}")
    }

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
}