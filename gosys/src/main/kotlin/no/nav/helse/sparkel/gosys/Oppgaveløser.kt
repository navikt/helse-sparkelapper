package no.nav.helse.sparkel.gosys

import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory

internal class Oppgaveløser(
    rapidsConnection: RapidsConnection,
    private val oppgaveService: OppgaveService
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
            validate { it.requireKey("ÅpneOppgaver.aktørId") }
            validate { it.requireKey("ÅpneOppgaver.ikkeEldreEnn") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val behovId = packet["@id"].asText()

        val aktørId = packet["ÅpneOppgaver.aktørId"].asText()
        val ikkeEldreEnn = packet["ÅpneOppgaver.ikkeEldreEnn"].asLocalDate()
        val antall = oppgaveService.løsningForBehov(behovId, aktørId, ikkeEldreEnn)

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
