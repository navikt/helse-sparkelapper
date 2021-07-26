package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.ktor.client.features.*
import io.ktor.client.statement.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.AaregClient
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.asLocalDate
import no.nav.helse.sparkel.aareg.arbeidsforholdV2.asOptionalLocalDate
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.aareg.util.KodeverkClient
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.util.*

class Arbeidsforholdbehovløser(
    rapidsConnection: RapidsConnection,
    private val arbeidsforholdClient: ArbeidsforholdClient,
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

        val arbeidsforholdSoap = løsBehovSoap(
            aktørId = aktørId,
            fom = fom,
            tom = tom,
            organisasjonsnummer = organisasjonsnummer
        ).also {
            packet.setLøsning(behov, it)
        }

        val arbeidsforholdRest = løsBehovRest(packet)

        sammenlignLister(arbeidsforholdSoap, arbeidsforholdRest)

        sikkerlogg.info(
            "løser behov {}, {}",
            keyValue("id", packet["@id"].asText()),
            keyValue("løsning", packet["@løsning"])
        )

        context.publish(packet.toJson())
    }

    private fun løsBehovRest(packet: JsonMessage): List<LøsningDto> {
        val fnr = packet["$behov.fødselsnummer"].asText()
        val id = UUID.fromString(packet["@id"].asText())

        return try {
            log.info("løser behov={}", keyValue("id", id))
            runBlocking {
                aaregClient.hentFraAareg(fnr, id).toLøsningDto()
            }
        } catch (err: ClientRequestException) {
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
            emptyList()
        } catch (err: Exception) {
            sikkerlogg.debug("What now? \n${packet.toJson()} \n{}", err)
            emptyList()
        }
    }

    private fun sammenlignLister(soapListe: List<LøsningDto>, restListe: List<LøsningDto>) {
        val sortertRestListe = restListe.sortedBy { it.startdato }
        val sortertSoapListe = soapListe.sortedBy { it.startdato }

        if (sortertRestListe == sortertSoapListe) log.debug("Dæm va lik")
        else log.debug("Her er det ikke helt likt, REST vs SOAP: \n{} \n{}", sortertRestListe, sortertSoapListe)
    }

    private fun løsBehovSoap(
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

    private fun ArrayNode.toLøsningDto(): List<LøsningDto> = this.flatMap { arbeidsforhold ->
        arbeidsforhold.path("arbeidsavtaler").map {
            LøsningDto(
                startdato = it.path("bruksperiode").path("fom").asLocalDate(),
                sluttdato = it.path("bruksperiode").path("tom")?.asOptionalLocalDate(),
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
