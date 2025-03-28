package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforholdMedDetaljer
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.Arbeidsstedtype.Underenhet
import no.nav.helse.sparkel.aareg.sikkerlogg
import org.slf4j.LoggerFactory

class Arbeidsforholdbehovløser(
    rapidsConnection: RapidsConnection,
    private val aaregClient: AaregClient,
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsforhold"

        internal fun List<AaregArbeidsforholdMedDetaljer>.toLøsningDto(): List<LøsningDto> = this.map { arbeidsforhold ->
            LøsningDto(
                startdato = arbeidsforhold.ansettelsesperiode.startdato,
                sluttdato = arbeidsforhold.ansettelsesperiode.sluttdato,
                stillingsprosent = arbeidsforhold.ansettelsesdetaljer.first().avtaltStillingsprosent,
                stillingstittel = arbeidsforhold.ansettelsesdetaljer.first().yrke.beskrivelse,
            )
        }
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.fødselsnummer") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
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
                val arbeidsforholdFraAareg = aaregClient.hentFraAareg<AaregArbeidsforholdMedDetaljer>(fnr, id)
                    .filter { arbeidsforhold -> arbeidsforhold.arbeidssted.run { type == Underenhet && getOrgnummer() == organisasjonsnummer } }
                val løsning = arbeidsforholdFraAareg.toLøsningDto()

                if (løsning.isEmpty())
                    sikkerlogg.info("Fant ingen arbeidsforhold for fnr $fnr på orgnummer $organisasjonsnummer i aareg, fikk svar:\n$arbeidsforholdFraAareg")

                løsning
            }
        } catch (err: AaregException) {
            log.error(
                "Feilmelding for behov={} ved oppslag i AAreg, fikk status={}, se sikkerlogg for detaljer. Svarer med tom liste",
                keyValue("id", id),
                err.statusFromAareg()
            )
            sikkerlogg.error(
                "Feilmelding for behov={} ved oppslag i AAreg: ${err.message}. Svarer med tom liste. Response:\n\t{}",
                keyValue("id", id),
                err.responseValue(),
                err,
            )
            emptyList()
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
            sikkerlogg.info("What now? \n${packet.toJson()}", err)
            emptyList()
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
