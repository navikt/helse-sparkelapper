package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.AaregClient
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.asLocalDate
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.asOptionalLocalDate
import no.nav.helse.sparkel.aareg.sikkerlogg
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient

class Arbeidsforholdbehovløser(
    rapidsConnection: RapidsConnection,
    private val aaregClient: AaregClient,
    private val kodeverkClient: KodeverkClient,
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsforhold"
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            validate { it.requireContains("@behov", behov) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.fødselsnummer") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        packet.setLøsning(behov, løsBehov(packet))

        sikkerlogg.info(
            "løser behov {}, {}",
            keyValue("id", packet["@id"].asText()),
            keyValue("løsning", packet["@løsning"])
        )

        context.publish(packet.toJson())
    }

    private fun løsBehov(packet: JsonMessage): List<LøsningDto> {
        val fnr = packet["$behov.fødselsnummer"].asText()
        val id = UUID.fromString(packet["@id"].asText())
        val organisasjonsnummer = packet["$behov.organisasjonsnummer"].asText()

        return try {
            log.info("løser behov={}", keyValue("id", id))
            runBlocking {
                aaregClient.hentFraAareg(fnr, id)
                    .filter { arbeidsforhold ->
                        arbeidsforhold["arbeidsgiver"].path("organisasjonsnummer").asText() == organisasjonsnummer
                    }
                    .filter { it.path("innrapportertEtterAOrdningen").asBoolean() }
                    .also {
                        if (it.any { arbeidsforhold ->
                                !arbeidsforhold.path("arbeidsavtaler").any { arbeidsavtale ->
                                    arbeidsavtale.path("gyldighetsperiode").path("tom")
                                        .isMissingOrNull()
                                }
                            }) {
                            sikkerlogg.info("RESTen av svaret {}", it)
                        }
                    }
                    .toLøsningDto()
            }
        } catch (err: ClientRequestException) {
            log.warn(
                "Feilmelding for behov={} ved oppslag i AAreg. Svarer med tom liste",
                keyValue("id", id)
            )
            sikkerlogg.warn(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Svarer med tom liste. Response: {}",
                keyValue("id", id),
                runBlocking { err.response.bodyAsText() },
                err
            )
            emptyList()
        } catch (err: Exception) {
            sikkerlogg.info("What now? \n${packet.toJson()} \n{}", err)
            emptyList()
        }
    }

    private fun List<JsonNode>.toLøsningDto(): List<LøsningDto> = this.flatMap { arbeidsforhold ->
        arbeidsforhold.path("arbeidsavtaler").map {
            LøsningDto(
                startdato = it.path("gyldighetsperiode").path("fom").asLocalDate(),
                sluttdato = it.path("gyldighetsperiode").path("tom")?.asOptionalLocalDate(),
                stillingsprosent = it.path("stillingsprosent")?.asInt() ?: 0,
                stillingstittel = kodeverkClient.getYrke(it.path("yrke").asText())
            )
        }
    }

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }

    data class LøsningDto(
        val stillingstittel: String,
        val stillingsprosent: Int,
        val startdato: LocalDate,
        val sluttdato: LocalDate?
    )
}
