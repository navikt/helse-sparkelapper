package no.nav.helse.sparkel.tilbakedatert

import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.isMissingOrNull
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
            validate {
                it.demandKey("sykmelding")
                it.demandKey("personNrPasient")
                it.demandKey("sykmelding.signaturDato")
                it.requireKey("sykmelding.syketilfelleStartDato")
                it.requireArray("sykmelding.perioder") {
                    requireKey("fom", "tom")
                }
                it.interestedIn("merknader")
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke sykmelding:\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        log.info("Leser melding ${packet["sykmelding"]["id"]}")
        håndter(packet, context)
    }

    private fun håndter(packet: JsonMessage, context: MessageContext) {
        val fødselsnummer = packet["personNrPasient"].asText()
        val sykmeldingId = packet["sykmelding"]["id"].asText()
        val syketilfelleStartDato = packet["sykmelding"]["syketilfelleStartDato"].asLocalDate()
        val signaturDato = packet["sykmelding"]["signaturDato"].asLocalDateTime()
        val erTilbakedatert = syketilfelleStartDato < LocalDate.from(signaturDato.minusDays(3))
        val perioder = packet["sykmelding"]["perioder"].map {
            mapOf(
                "fom" to it["fom"].asLocalDate(),
                "tom" to it["tom"].asLocalDate(),
            )
        }
        val erUnderManuellBehandling = packet["merknader"].takeUnless { it.isMissingOrNull() }?.find {
            it["type"].asText() == "UNDER_BEHANDLING"
        } ?: false
        val erUgyldigTilbakedaterting = packet["merknader"].takeUnless { it.isMissingOrNull() }?.find {
            it["type"].asText() == "UGYLDIG_TILBAKEDATERING"
        } ?: false
        val flereOpplysninger = packet["merknader"].takeUnless { it.isMissingOrNull() }?.find {
            it["type"].asText() == "TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER"
        } ?: false
        val delvisGodkjent = packet["merknader"].takeUnless { it.isMissingOrNull() }?.find {
            it["type"].asText() == "DELVIS_GODKJENT"
        } ?: false

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

        if (erTilbakedatert && erUnderManuellBehandling == false && erUgyldigTilbakedaterting == false && flereOpplysninger == false && delvisGodkjent == false) {
            val returEvent = JsonMessage.newMessage(
                eventName = "tilbakedatering_behandlet",
                map = mapOf(
                    "fødselsnummer" to fødselsnummer,
                    "sykmeldingId" to sykmeldingId,
                    "syketilfelleStartDato" to syketilfelleStartDato,
                    "perioder" to perioder,
                )
            )

            context.publish(returEvent.toJson()).also {
                sikkerlogg.info(
                    "sender tilbakedatering_behandlet for {}: {}",
                    keyValue("sykmeldingId", sykmeldingId),
                    returEvent
                )
            }
        }
    }
}
