package no.nav.helse.sparkel.aareg.arbeidsforholdV2

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory
import java.util.*

class ArbeidsforholdLøserV2(rapidsConnection: RapidsConnection, private val aaregClient: AaregClient) : River.PacketListener {

    companion object {
        internal const val behov = "ArbeidsforholdV2"
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
            log.info("løser behov={}", keyValue("id", packet["@id"].asText()))
            runBlocking {
                aaregClient
                    .hentFraAareg(packet["fødselsnummer"].asText(), UUID.fromString(packet["@id"].asText()))
                    .map { it.toArbeidsforhold() }
            }
        } catch (err: AaregException) {
            log.error(
                "Feilmelding for behov={} ved oppslag i AAreg. Ignorerer behov",
                keyValue("id", packet["@id"].asText())
            )
            sikkerlogg.error(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Ignorerer behov. Response:\n\t{}",
                keyValue("id", packet["@id"].asText()),
                err.responseValue().toString(),
                err
            )
            return
        } catch (err: ClientRequestException) {
            log.warn(
                "Feilmelding for behov={} ved oppslag i AAreg. Svarer med tom liste",
                keyValue("id", packet["@id"].asText())
            )
            sikkerlogg.warn(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Svarer med tom liste. Response: {}",
                keyValue("id", packet["@id"].asText()),
                runBlocking { err.response.bodyAsText() },
                err
            )
            emptyList()
        }

        packet.setLøsning(behov, arbeidsforhold)
        context.publish(packet.toJson())
    }

    private fun JsonNode.toArbeidsforhold() = Arbeidsforhold(
        ansattSiden = this.path("ansettelsesperiode").path("periode").path("fom").asLocalDate(),
        ansattTil = this.path("ansettelsesperiode").path("periode").path("tom").asOptionalLocalDate(),
        orgnummer = this["arbeidsgiver"].path("organisasjonsnummer").asText()
    )

    override fun onError(problems: MessageProblems, context: MessageContext) {}

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }
}
