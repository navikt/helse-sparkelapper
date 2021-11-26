package no.nav.helse.sparkel.norg

import kotlinx.coroutines.runBlocking
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HentPersoninfoRiver(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)
    private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("HentPersoninfo"))
                it.rejectKey("@løsning")
                it.requireKey("fødselsnummer", "spleisBehovId", "@id")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) = runBlocking {
        val fnr = packet["fødselsnummer"].asText()
        val behov = packet["spleisBehovId"].asText()
        log.info(
            "Henter personinfo for {}, {}",
            keyValue("spleisBehovId", behov),
            keyValue("@id", packet["@id"].asText())
        )
        try {
            val person = personinfoService.finnPerson(fnr, behov) ?: return@runBlocking
            packet["@løsning"] = mapOf(
                "HentPersoninfo" to mapOf(
                    "fornavn" to person.fornavn,
                    "mellomnavn" to person.mellomnavn,
                    "etternavn" to person.etternavn,
                    "fødselsdato" to person.fødselsdato,
                    "kjønn" to person.kjønn
                )
            )
            context.publish(packet.toJson())
        } catch (err: Exception) {
            log.error("feil ved håntering av behov {} for {}: ${err.message}",
                keyValue("spleisBehovId", packet["spleisBehovId"].asText()),
                keyValue("@id", packet["@id"].asText()),
                err)
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerLogg.error("Forstod ikke HentPersoninfo-behov:\n${problems.toExtendedReport()}")
    }
}

enum class Kjønn {
    Mann, Kvinne, Ukjent
}
