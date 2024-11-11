package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.isMissingOrNull
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.result_object.flatten
import com.github.navikt.tbd_libs.result_object.map
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.speed.PersonResponse
import com.github.navikt.tbd_libs.speed.PersonResponse.Adressebeskyttelse.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class HentPersoninfoV2Løser(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService,
    private val objectMapper: ObjectMapper
) : River.PacketListener {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("HentPersoninfoV2"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer")
                it.interestedIn("HentPersoninfoV2.ident", "hendelseId", "@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) = runBlocking {
        val behovId = packet["@id"].asText()
        val hendelseId = packet["hendelseId"].asText()
        withMDC(mapOf(
                "hendelseId" to hendelseId,
                "behovId" to behovId
        )) {
            sikkerLogg.info("mottok melding: ${packet.toJson()}")
            val ident = packet["HentPersoninfoV2.ident"].takeUnless { it.isMissingOrNull() } ?: packet["fødselsnummer"]

            val identer = if (ident.isArray) ident.map { it.asText() } else listOf(ident.asText())
            val løsninger = identer
                .map {
                    personinfoService.løsningForPersoninfo(behovId, it)
                }
                .flatten()
                .map { liste ->
                    liste.mapIndexed { index, personRespons ->
                        val identForLøsning = identer[index]
                        personRespons.løsningJson(identForLøsning)
                    }.ok()
                }
            try {
                when (løsninger) {
                    is Result.Error -> {
                        sikkerLogg.warn("Feil under løsing av personinfo-behov: ${løsninger.error}", løsninger.cause)
                        log.warn("Feil under løsing av personinfo-behov: ${løsninger.error}", løsninger.cause)
                    }
                    is Result.Ok -> {
                        val løsningJson = if (ident.isArray) {
                            sikkerLogg.warn("Løser PersoninfoV2 med flere identer")
                            ObjectMapper().createArrayNode().apply {
                                løsninger.value.forEach { individuellLøsning ->
                                    this.add(individuellLøsning)
                                }
                            }
                        } else {
                            løsninger.value.single()
                        }

                        packet["@løsning"] = mapOf("HentPersoninfoV2" to løsningJson)
                        context.publish(packet.toJson())
                    }
                }
            } catch (e: Exception) {
                sikkerLogg.warn("Feil under løsing av personinfo-behov: ${e.message}", e)
                log.warn("Feil under løsing av personinfo-behov: ${e.message}", e)
            }
        }
    }

    private fun PersonResponse.løsningJson(ident: String): JsonNode {
        return objectMapper.createObjectNode().apply {
            put("ident", ident)
            put("fornavn", fornavn)
            if (mellomnavn != null) put("mellomnavn", mellomnavn) else putNull("mellomnavn")
            put("etternavn", etternavn)
            put("fødselsdato", fødselsdato.toString())
            put("kjønn", when (kjønn) {
                PersonResponse.Kjønn.MANN -> "Mann"
                PersonResponse.Kjønn.KVINNE -> "Kvinne"
                PersonResponse.Kjønn.UKJENT -> "Ukjent"
            })
            put("adressebeskyttelse", when (adressebeskyttelse) {
                FORTROLIG -> "Fortrolig"
                STRENGT_FORTROLIG -> "StrengtFortrolig"
                STRENGT_FORTROLIG_UTLAND -> "StrengtFortroligUtland"
                UGRADERT -> "Ugradert"
            })
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerLogg.error("Forstod ikke HentPersoninfoV2-behov:\n${problems.toExtendedReport()}")
    }
}
