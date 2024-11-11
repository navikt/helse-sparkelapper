package no.nav.helse.sparkel.sputnik

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
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

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) = try {
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
                context.publish(fødselsnummer, json)
            }
        }
    } catch (ex: Exception) {
        sikkerlogg.error("Feil ved løsing av behov:\n ${packet.toJson()}", ex)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("Forstod ikke behovet: \n${problems.toExtendedReport()}")
    }

    private companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    }
}