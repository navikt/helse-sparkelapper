package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.sparkel.aareg.sikkerlogg
import java.time.LocalDate

class Arbeidsforholdbehovløser(
    rapidsConnection: RapidsConnection,
    private val arbeidsforholdClient: ArbeidsforholdClient
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsforhold"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireContains("@behov", behov) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.aktørId") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
            validate { it.require("$behov.fom", JsonNode::asLocalDate) }
            validate { it.require("$behov.tom", JsonNode::asLocalDate) }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val organisasjonsnummer = packet["$behov.organisasjonsnummer"].asText()
        val aktørId = packet["$behov.aktørId"].asText()
        val fom = packet["$behov.fom"].asLocalDate()
        val tom = packet["$behov.tom"].asLocalDate()

        løsBehov(
            aktørId = aktørId,
            fom = fom,
            tom = tom,
            organisasjonsnummer = organisasjonsnummer
        ).also {
            packet.setLøsning(behov, it)
        }

        sikkerlogg.info(
            "løser behov {}, {}",
            keyValue("id", packet["@id"].asText()),
            keyValue("løsning", packet["@løsning"])
        )

        context.publish(packet.toJson())
    }

    private fun løsBehov(
        aktørId: String,
        fom: LocalDate,
        tom: LocalDate,
        organisasjonsnummer: String
    ): List<LøsningDto> =
        arbeidsforholdClient.finnArbeidsforhold(
            organisasjonsnummer = organisasjonsnummer,
            aktørId = aktørId,
            fom = fom,
            tom = tom
        ).map { arbeidsforhold ->
            LøsningDto(
                stillingstittel = arbeidsforhold.stillingstittel,
                stillingsprosent = arbeidsforhold.stillingsprosent,
                startdato = arbeidsforhold.startdato,
                sluttdato = arbeidsforhold.sluttdato
            )
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
