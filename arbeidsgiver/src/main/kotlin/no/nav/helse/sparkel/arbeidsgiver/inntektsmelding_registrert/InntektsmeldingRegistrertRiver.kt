package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.util.UUID
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class InntektsmeldingRegistrertRiver(
    rapidsConnection: RapidsConnection,
    private val inntektsmeldingRegistrertRepository: InntektsmeldingRegistrertRepository
) : River.PacketListener {

    private companion object {
        private val logg: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", "inntektsmelding") }
            validate { it.requireKey("@id", "inntektsmeldingId", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {

        val hendelseId = UUID.fromString(packet["@id"].asText())
        val dokumentId = UUID.fromString(packet["inntektsmeldingId"].asText())
        val opprettet = packet["@opprettet"].asLocalDateTime()

        val inntektsmeldingRegistrertDto = InntektsmeldingRegistrertDto(
            hendelseId = hendelseId,
            dokumentId = dokumentId,
            opprettet = opprettet
        )

        inntektsmeldingRegistrertRepository.lagre(inntektsmeldingRegistrertDto)
        logg.info("Lagret kobling mellom hendelseId og dokumentId til inntektsmelding: {}, {}, {}",
            StructuredArguments.keyValue("hendelseId", hendelseId),
            StructuredArguments.keyValue("dokumentId", dokumentId),
            StructuredArguments.keyValue("opprettet", opprettet)
        )
    }
}
