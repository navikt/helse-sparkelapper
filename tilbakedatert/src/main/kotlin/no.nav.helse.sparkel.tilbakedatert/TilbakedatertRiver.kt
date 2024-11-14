package no.nav.helse.sparkel.tilbakedatert

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

internal class TilbakedatertRiver(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(TilbakedatertRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireKey("sykmelding")
                it.requireKey("personNrPasient")
                it.requireKey("sykmelding.signaturDato")
            }
            validate {
                it.requireKey("sykmelding.id")
                it.requireKey("sykmelding.syketilfelleStartDato")
                it.requireArray("sykmelding.perioder") {
                    requireKey("fom", "tom")
                }
                it.interestedIn("merknader")
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke sykmelding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        log.info("Leser melding for sykmeldingId={}", packet["sykmelding.id"].asText())
        håndter(packet, context)
    }

    private fun håndter(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["personNrPasient"].asText()
        val sykmeldingId = packet["sykmelding.id"].asText()
        val syketilfelleStartDato = packet["sykmelding.syketilfelleStartDato"].asLocalDate()
        val signaturDato = packet["sykmelding.signaturDato"].asLocalDateTime()
        val erTilbakedatert = syketilfelleStartDato < LocalDate.from(signaturDato.minusDays(3))
        val perioder = packet["sykmelding.perioder"].map {
            mapOf(
                "fom" to it["fom"].asLocalDate(),
                "tom" to it["tom"].asLocalDate(),
            )
        }
        val erUnderManuellBehandling = packet.harMerknad("UNDER_BEHANDLING")
        val erUgyldigTilbakedaterting = packet.harMerknad("UGYLDIG_TILBAKEDATERING")
        val flereOpplysninger = packet.harMerknad("TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER")
        val delvisGodkjent = packet.harMerknad("DELVIS_GODKJENT")
        val ferdigbehandlet =
            !erUnderManuellBehandling && !erUgyldigTilbakedaterting && !flereOpplysninger && !delvisGodkjent

        loggDebugInfo(sykmeldingId, packet)

        sikkerlogg.info(
            "Leser melding {}, {}, {}, {}, {}, {}, {}",
            kv("fødselsnummer", fødselsnummer),
            kv("sykmeldingId", sykmeldingId),
            kv("erTilbakedatert", erTilbakedatert),
            kv("erUnderManuellBehandling", erUnderManuellBehandling),
            kv("erUgyldigTilbakedatering", erUgyldigTilbakedaterting),
            kv("flereOpplysninger", flereOpplysninger),
            kv("delvisGodkjent", delvisGodkjent)
        )

        if (erTilbakedatert && ferdigbehandlet) {
            val returEvent = JsonMessage.newMessage(
                eventName = "tilbakedatering_behandlet",
                map = mapOf(
                    "fødselsnummer" to fødselsnummer,
                    "sykmeldingId" to sykmeldingId,
                    "syketilfelleStartDato" to syketilfelleStartDato,
                    "perioder" to perioder,
                )
            ).toJson()

            context.publish(fødselsnummer, returEvent).also {
                sikkerlogg.info(
                    "sender tilbakedatering_behandlet for {}: {}",
                    keyValue("sykmeldingId", sykmeldingId),
                    returEvent
                )
            }
        }
    }

    private fun loggDebugInfo(sykmeldingId: String, packet: JsonMessage) {
        val tekst = when (val merknaderNode = packet["merknader"]) {
            is NullNode -> "<feltet er null i meldingen>"
            is MissingNode -> "<feltet mangler i meldingen>"
            is ArrayNode -> merknaderNode.toString()
            else -> "<ukjent>"
        }
        sikkerlogg.info("Merknader for $sykmeldingId: $tekst")
    }

    private fun JsonMessage.harMerknad(type: String): Boolean = this["merknader"].any { it["type"].asText() == type }
}
