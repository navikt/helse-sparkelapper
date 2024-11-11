package no.nav.helse.sparkel.representasjon

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asOptionalLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import java.time.LocalDate
import net.logstash.logback.argument.StructuredArguments.kv
import no.nav.helse.sparkel.representasjon.RepresentasjonRiver.Område.Alle
import no.nav.helse.sparkel.representasjon.RepresentasjonRiver.Område.Syk
import no.nav.helse.sparkel.representasjon.RepresentasjonRiver.Område.Sym
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

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke behov 'fullmakt':\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        val id = packet["@id"].asText()
        log.info("Leser melding {}", id)
        val fnr = packet["fødselsnummer"].asText()

        val svar = representasjonClient.hentFullmakt(fnr).mapCatching { fullmakt ->
            fullmakt.map {
                Fullmakt(
                    områder = it["omraade"].map { område -> Område.fra(område["tema"].asText()) },
                    gyldigFraOgMed = it["gyldigFraOgMed"].asLocalDate(),
                    gyldigTilOgMed = it["gyldigTilOgMed"].asOptionalLocalDate()
                )
            }.filter { it.områder.any { område -> område in listOf(Syk, Sym, Alle) } }
        }
        svar.fold(
            onSuccess = { fullmakt: List<Fullmakt> ->
                packet["@løsning"] = mapOf("Fullmakt" to fullmakt)
                context.publish(packet.toJson())
                sikkerlogg.info(
                    "Besvarte behov {}:\n{}",
                    kv("id", id),
                    packet.toJson()
                )
            },
            onFailure = { t: Throwable ->
                "Fikk feil ved oppslag mot representasjon".also { message ->
                    log.error(message)
                    sikkerlogg.error("$message {}", kv("Exception", t))
                }
                throw t
            }
        )
    }

    internal data class Fullmakt(
        val områder: List<Område>,
        val gyldigFraOgMed: LocalDate,
        val gyldigTilOgMed: LocalDate?
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
