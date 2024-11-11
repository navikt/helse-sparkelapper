package no.nav.helse.sparkel.gosys

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.getOrThrow
import com.github.navikt.tbd_libs.result_object.tryCatch
import com.github.navikt.tbd_libs.retry.retryBlocking
import com.github.navikt.tbd_libs.speed.SpeedClient
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import net.logstash.logback.argument.StructuredArguments.kv
import org.slf4j.LoggerFactory

internal class Oppgaveløser(
    rapidsConnection: RapidsConnection,
    private val oppgaveService: OppgaveService,
    private val speedClient: SpeedClient
) : River.PacketListener {

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

    companion object {
        const val behov = "ÅpneOppgaver"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
            validate { it.requireKey("ÅpneOppgaver.ikkeEldreEnn") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val behovId = packet["@id"].asText()
        val fnr = packet["fødselsnummer"].asText()
        withMDC("callId" to behovId) {
            when (val result = tryCatch {
                retryBlocking {
                    speedClient.hentFødselsnummerOgAktørId(fnr, behovId).getOrThrow()
                }
            }) {
                is Result.Error -> sikkerlogg.error("Feil ved løsning av behov: ${result.error}", result.cause)
                is Result.Ok -> {
                    val ikkeEldreEnn = packet["ÅpneOppgaver.ikkeEldreEnn"].asLocalDate()
                    sikkerlogg.info("slår opp åpne oppgaver for {} ikke eldre enn $ikkeEldreEnn", kv("aktørId", result.value.aktørId))
                    val antall = oppgaveService.løsningForBehov(behovId, result.value.aktørId, ikkeEldreEnn)

                    packet["@løsning"] = mapOf(
                        behov to mapOf(
                            "antall" to antall,
                            "oppslagFeilet" to (antall == null)
                        )
                    )
                    context.publish(packet.toJson().also { json ->
                        sikkerlogg.info(
                            "sender svar {} for {}",
                            keyValue("id", behovId),
                            json
                        )
                    })
                }
            }
        }
    }
}
