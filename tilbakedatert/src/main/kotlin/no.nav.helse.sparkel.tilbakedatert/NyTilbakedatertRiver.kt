package no.nav.helse.sparkel.tilbakedatert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue

internal class NyTilbakedatertRiver(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            precondition {
                it.requireValue("validation.status", "OK")
                it.requireKey("sykmelding.id")
                it.requireKey("sykmelding.pasient.id")
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
        sikkerlogg.error(problems.toExtendedReport())
    }

    override fun onSevere(error: MessageProblems.MessageException, context: MessageContext) {
        sikkerlogg.info(error.toString())
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("Leser melding {}", packet.toJson())
        val fødselsnummer = packet["sykmelding.pasient.id"].asText()
        val sykmeldingId = packet["sykmelding.id"].asText()

        val returEvent = lagReturEvent(fødselsnummer, sykmeldingId)
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
        sykmeldingId: String
    ) = JsonMessage.newMessage(
        eventName = "tilbakedatering_behandlet",
        map = mapOf(
            "fødselsnummer" to fødselsnummer,
            "sykmeldingId" to sykmeldingId,
        )
    ).toJson()

}
