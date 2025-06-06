package no.nav.helse.sparkel.tilbakedatert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import net.logstash.logback.argument.StructuredArguments.keyValue

internal class NyTilbakedatertRiver(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {

    private var ikkeSendMeldingFør: Instant = now()

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
                }
                it.requireValue("validation.status", "OK")
                it.require("validation.rules") { node ->
                    check(node.isArray)
                    check(node.any { rule ->
                        rule["name"].asText() == "TILBAKEDATERING_UNDER_BEHANDLING" &&
                            rule["type"].asText() == "OK"
                    })
                }
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("Melding oppfyller precondition, men ikke validations. " + problems.toExtendedReport())
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sikkerlogg.info(error.toString())
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("Leser melding {}", packet.toJson())
        val fødselsnummer = packet["sykmelding.pasient.fnr"].asText()
        val sykmeldingId = packet["sykmelding.id"].asText()
        val perioder = packet["sykmelding.aktivitet"].map {
            mapOf(
                "fom" to it["fom"].asLocalDate(),
                "tom" to it["tom"].asLocalDate(),
            )
        }

        val returEvent = lagReturEvent(fødselsnummer, sykmeldingId, perioder)

        // porsjoner ut publiseringen, så det ikke blir floke på rapiden (mest for gøy)
        while (now().isBefore(ikkeSendMeldingFør)) Thread.sleep(100.milliseconds.toJavaDuration())

        context.publish(fødselsnummer, returEvent).also {
            sikkerlogg.info(
                "sender tilbakedatering_behandlet for {}:\n{}",
                keyValue("sykmeldingId", sykmeldingId),
                returEvent
            )
            ikkeSendMeldingFør = now().plusMillis(333)
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
