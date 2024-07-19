package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import java.util.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import no.nav.helse.sparkel.aareg.sikkerlogg

class Arbeidsgiverinformasjonsbehovløser(
    rapidsConnection: RapidsConnection,
    private val kodeverkClient: KodeverkClient,
    private val eregClient: EregClient,
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsgiverinformasjon"
        fun validateOrganisasjonsnummer(node:JsonNode) {
            when(node) {
                is ArrayNode -> node.forEach { n -> valider(n) }
                else -> valider(node)
            }
        }
        private fun valider(node: JsonNode) {
            if (!node.asText().matches("\\d{9}".toRegex())) {
                sikkerlogg.error("Kunne ikke gjenkjenne melding; organisasjonsnummer er ugyldig: ${node.asText()}")
                throw RuntimeException("${node.asText()} er ikke et gyldig organisasjonsnummer.")
            }
        }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
            validate { it.require("$behov.organisasjonsnummer") { validateOrganisasjonsnummer(it) } }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val organisasjonsnummer = packet["$behov.organisasjonsnummer"]

        runBlocking {
            try {
                løsBehov(organisasjonsnummer, UUID.fromString(packet["@id"].asText()))
                    .also { packet.setLøsning(behov, it) }
                sikkerlogg.info("løser $behov-behov {}", keyValue("id", packet["@id"].asText()))

                context.publish(packet.toJson())
            } catch (ex: FeilVedHenting) {
                sikkerlogg.error("Oppslag av organisasjon feilet: ${ex.message}")
            }
        }
    }

    private suspend fun løsBehov(organisasjonsnummer: JsonNode, callId: UUID): Any =
        if (organisasjonsnummer.isArray) {
            organisasjonsnummer.map { løsningFor(it.asText(), callId) }
        } else {
            løsningFor(organisasjonsnummer.asText(), callId)
        }

    private suspend fun løsningFor(organisasjonsnummer: String, callId: UUID) =
        eregClient.hentNavnOgNæringForOrganisasjon(organisasjonsnummer, callId)
            .let { eregResponse ->
                LøsningDto(
                    orgnummer = organisasjonsnummer,
                    navn = eregResponse.navn,
                    bransjer = eregResponse.næringer.map(kodeverkClient::getNæring),
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
