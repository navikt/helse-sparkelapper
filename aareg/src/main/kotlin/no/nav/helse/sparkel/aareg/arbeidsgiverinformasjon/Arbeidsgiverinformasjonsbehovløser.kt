package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.aareg.objectMapper
import no.nav.helse.sparkel.aareg.sikkerlogg
import no.nav.helse.sparkel.aareg.util.KodeverkClient
import no.nav.helse.sparkel.ereg.EregClient
import java.util.*

class Arbeidsgiverinformasjonsbehovløser(
    rapidsConnection: RapidsConnection,
    private val organisasjonClient: OrganisasjonClient,
    private val kodeverkClient: KodeverkClient,
    private val eregClient: EregClient,
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsgiverinformasjon"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireContains("@behov", behov) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val organisasjonsnummer = packet["$behov.organisasjonsnummer"]

        soapLøsBehov(organisasjonsnummer = organisasjonsnummer).also { packet.setLøsning(behov, it) }
        try {
            runBlocking {
                val fraREST = løsBehov(organisasjonsnummer, UUID.fromString(packet["@id"].asText()))
                sikkerlogg.info("Hentet via REST:\n{}\nHentet via SOAP:\n{}", objectMapper.valueToTree(fraREST), packet["@løsning"][behov])
            }
        } catch (e: Exception) {
            sikkerlogg.info("Feil mot Ereg", e)
        }


        sikkerlogg.info(
            "løser $behov-behov {}",
            keyValue("id", packet["@id"].asText()),
        )

        context.publish(packet.toJson())
    }

    private suspend fun løsBehov(organisasjonsnummer: JsonNode, callId: UUID): Any =
        if (organisasjonsnummer.isArray) {
            organisasjonsnummer.map { løsningFor(it.asText(), callId) }
        } else {
            løsningFor(organisasjonsnummer.asText(), callId)
        }

    private suspend fun løsningFor(organisasjonsnummer: String, callId: UUID) =
        eregClient.hentOrganisasjon(organisasjonsnummer, callId)
            .let { eregResponse ->
                LøsningDto(
                    orgnummer = organisasjonsnummer,
                    navn = eregResponse.navn,
                    bransjer = eregResponse.næringer.map(kodeverkClient::getNæring),
                )
            }

    private fun soapLøsBehov(organisasjonsnummer: JsonNode): Any =
        if (organisasjonsnummer.isArray) {
            organisasjonsnummer.map { soapLøsningFor(it.asText()) }
        } else {
            soapLøsningFor(organisasjonsnummer.asText())
        }

    private fun soapLøsningFor(organisasjonsnummer: String) = organisasjonClient.finnOrganisasjon(organisasjonsnummer)
        .let { organisasjon ->
            LøsningDto(
                orgnummer = organisasjonsnummer,
                navn = organisasjon.navn,
                bransjer = organisasjon.bransjer
            )
        }

    private fun JsonMessage.setLøsning(nøkkel: String, data: Any) {
        this["@løsning"] = mapOf(
            nøkkel to data
        )
    }

    data class LøsningDto(
        val orgnummer: String,
        val navn: String,
        val bransjer: List<String>
    )
}
