package no.nav.helse.sparkel.tilbakedatert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

internal class TilbakedatertRiver(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {

    val log = LoggerFactory.getLogger(TilbakedatertRiver::class.java)

    // Hvis det kommer OK på en rule med et av disse navnene, skal spesialist få vite om godkjenningen.
    // Derimot skal OK med name: TILBAKEDATERING_DELVIS_GODKJENT ikke videreformidles - future feature å håndtere dem
    val statuser = setOf("TILBAKEDATERING_KREVER_FLERE_OPPLYSNINGER", "TILBAKEDATERING_UNDER_BEHANDLING")

    init {
        River(rapidsConnection).apply {
            precondition {
                it.forbid("@event_name", "personNrPasient")
            }
            validate {
                it.requireKey("sykmelding.id")
                it.requireKey("sykmelding.pasient.fnr")
                it.requireArray("sykmelding.aktivitet") {
                    requireKey("fom", "tom")
                    require("fom") { node -> check(node.asLocalDate() > LocalDate.of(2024, 1, 1)) }
                }
                it.requireValue("validation.status", "OK")
                it.require("validation.rules") { node ->
                    check(node.isArray)
                    check(node.any { rule -> rule["name"].asText() in statuser && rule["type"].asText() == "OK" })
                }
            }
        }.register(this)
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sikkerlogg.info(error.toString())
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val fødselsnummer = packet["sykmelding.pasient.fnr"].asText()
        val sykmeldingId = packet["sykmelding.id"].asText()
        log.info("Leser melding for {}", keyValue("sykmeldingId", sykmeldingId))
        val perioder = packet["sykmelding.aktivitet"].map {
            mapOf(
                "fom" to it["fom"].asLocalDate(),
                "tom" to it["tom"].asLocalDate(),
            )
        }

        val returEvent = lagReturEvent(fødselsnummer, sykmeldingId, perioder)
        context.publish(fødselsnummer, returEvent).also {
            sikkerlogg.info(
                "sender tilbakedatering_behandlet for {}:\n{}",
                keyValue("sykmeldingId", sykmeldingId),
                returEvent
            )
        }
    }

    private fun lagReturEvent(
        fødselsnummer: String,
        sykmeldingId: String,
        perioder: List<Map<String, LocalDate>>
    ) = JsonMessage.newMessage(
        eventName = "tilbakedatering_behandlet",
        map = mapOf(
            "fødselsnummer" to fødselsnummer,
            "sykmeldingId" to sykmeldingId,
            "perioder" to perioder,
        )
    ).toJson()
}
