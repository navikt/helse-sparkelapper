package no.nav.helse.sparkel.vilkarsproving.opptjening

import io.ktor.client.features.ClientRequestException
import io.ktor.client.statement.readText
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory
import java.util.*

class OpptjeningLøser(rapidsConnection: RapidsConnection, private val aaregClient: AaregClient) : River.PacketListener {

    companion object {
        internal const val behov = "Opptjening"
    }

    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.requireContains("@behov", behov) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("Mottok melding: ${packet.toJson()}")
        val arbeidsforhold = try {
            runBlocking {
                aaregClient.hentArbeidsforhold(packet["fødselsnummer"].asText(), UUID.fromString(packet["@id"].asText()))
            }.also {
                log.info(
                    "løser behov={}",
                    keyValue("id", packet["@id"].asText())
                )
            }
        } catch (err: ClientRequestException) {
            emptyList<Arbeidsforhold>().also {
                log.warn(
                    "Feilmelding for behov={} ved oppslag i AAreg. Svarer med tom liste",
                    keyValue("id", packet["@id"].asText())
                )
                sikkerlogg.warn(
                    "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Svarer med tom liste. Response: {}",
                    keyValue("id", packet["@id"].asText()),
                    runBlocking { err.response.readText() },
                    err
                )
            }
        }

        packet.setLøsning(behov, arbeidsforhold)
        context.publish(packet.toJson())
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {}

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }
}
