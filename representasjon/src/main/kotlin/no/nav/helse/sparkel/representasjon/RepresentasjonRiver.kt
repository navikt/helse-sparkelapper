package no.nav.helse.sparkel.representasjon

import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.sparkel.representasjon.RepresentasjonRiver.Område.*
import org.slf4j.LoggerFactory

internal class RepresentasjonRiver(
    rapidsConnection: RapidsConnection,
    private val representasjonClient: RepresentasjonClient,
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val log = LoggerFactory.getLogger(RepresentasjonRiver::class.java)
    }

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAll("@behov", listOf("Fullmakt"))
                it.rejectKey("@løsning")
                it.requireKey("@id", "fødselsnummer")
            }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke behov 'fullmakt':\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val id = packet["@id"].asText()
        log.info("Leser melding {}", id)
        val fnr = packet["fødselsnummer"].asText()

        val svar = representasjonClient.hentFullmakt(fnr)
            ?.map {
                Fullmakt(
                    områder = it["omraade"].map { område -> Område.fra(område["tema"].asText()) },
                    gyldigFraOgMed = it["gyldigFraOgMed"].asLocalDate(),
                    gyldigTilOgMed = it["gyldigTilOgMed"].asLocalDate()
                )
            }
            ?.filter { it.områder.any { område -> område in listOf(Syk, Sym, Alle) } }
        packet["@løsning"] = mapOf("fullmakt" to (svar ?: mapOf("oppslagFeilet" to true)))
        context.publish(packet.toJson())
        sikkerlogg.info(
            "Besvarte behov {}:\n{}",
            kv("id", id),
            packet.toJson()
        )

    }

    internal data class Fullmakt(
        val områder: List<Område>,
        val gyldigFraOgMed: LocalDate,
        val gyldigTilOgMed: LocalDate
    )
    enum class Område {
        Alle, Syk, Sym, Annet;

        companion object {
            fun fra(verdi: String): Område {
                return when (verdi) {
                    "*" -> Alle
                    "SYK" -> Syk
                    "SYM" -> Sym
                    else -> Annet
                }
            }
        }
    }
}
