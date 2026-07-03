package no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.util.*
import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import no.nav.helse.sparkel.aareg.sikkerlogg
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ArrayNode

class Arbeidsgiverinformasjonsbehovløser(
    rapidsConnection: RapidsConnection,
    private val kodeverkClient: KodeverkClient,
    private val eregClient: EregClient,
) : River.PacketListener {
    companion object {
        internal const val behov = "Arbeidsgiverinformasjon"
        fun validateOrganisasjonsnummer(node: JsonNode) {
            when(node) {
                is ArrayNode -> node.forEach { n -> valider(n) }
                else -> valider(node)
            }
        }
        private fun valider(node: JsonNode) {
            if (!node.asString().matches("\\d{9}".toRegex())) {
                sikkerlogg.error("Kunne ikke gjenkjenne melding; organisasjonsnummer er ugyldig: ${node.asString()}")
                throw RuntimeException("${node.asString()} er ikke et gyldig organisasjonsnummer.")
            }
        }
    }

    init {
        River(rapidsConnection).apply {
            precondition { it.requireAll("@behov", listOf(behov)) }
            validate { it.forbid("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("$behov.organisasjonsnummer") }
            validate { it.require("$behov.organisasjonsnummer") { validateOrganisasjonsnummer(it) } }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")

        val organisasjonsnummer = packet["$behov.organisasjonsnummer"]

        runBlocking {
            try {
                løsBehov(organisasjonsnummer, UUID.fromString(packet["@id"].asString()))
                    .also { packet.setLøsning(behov, it) }
                sikkerlogg.info("løser $behov-behov {}", keyValue("id", packet["@id"].asString()))

                context.publish(packet.toJson())
            } catch (ex: FeilVedHenting) {
                sikkerlogg.error("Oppslag av organisasjon feilet: ${ex.message}")
            }
        }
    }

    private suspend fun løsBehov(organisasjonsnummer: JsonNode, callId: UUID): Any =
        if (organisasjonsnummer.isArray) {
            organisasjonsnummer.toList().map { løsningFor(it.asString(), callId) }
        } else {
            løsningFor(organisasjonsnummer.asString(), callId)
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
