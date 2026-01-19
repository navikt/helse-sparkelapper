package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.sparkel.arbeidsgiver.ArbeidsgiveropplysningerProducer
import org.slf4j.LoggerFactory

internal class TrengerArbeidsgiveropplysningerBegrensetRiver(
    rapidsConnection: RapidsConnection,
    private val arbeidsgiverProducer: ArbeidsgiveropplysningerProducer
) : River.PacketListener {
    private companion object {
        val logg = LoggerFactory.getLogger(this::class.java)
        val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val eventName = "vedtaksperiode_forkastet"
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireValue("@event_name", eventName) }
            precondition { it.requireValue("trengerArbeidsgiveropplysninger", true) }
            precondition { it.requireValue("yrkesaktivitetstype", "ARBEIDSTAKER") }
            validate {
                it.requireKey(
                    "fødselsnummer",
                    "organisasjonsnummer",
                    "vedtaksperiodeId",
                    "fom",
                    "tom",
                    "sykmeldingsperioder",
                    "@opprettet"
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        "Fant en forkastet periode som trenger forespørsel".let {
            logg.info(it)
            sikkerlogg.info("$it med data :\n{}", packet.toJson())
        }

        val payload = packet.toBegrensetTrengerArbeidsgiveropplysningerDto()
        arbeidsgiverProducer.send(payload)

        "Publiserte begrenset forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger".let {
            logg.info(it)
            sikkerlogg.info("$it med data :\n{}", payload)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $eventName:\n${problems.toExtendedReport()}")
    }
}
