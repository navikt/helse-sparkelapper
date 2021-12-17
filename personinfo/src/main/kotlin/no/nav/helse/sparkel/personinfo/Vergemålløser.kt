package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.*
import org.slf4j.LoggerFactory
import java.time.LocalDate

internal class Vergemålløser(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        const val behov = "Vergemål"
        val objectMapper: ObjectMapper = jacksonObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .registerModule(JavaTimeModule())
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        sikkerlogg.error("forstod ikke behov $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val behovId = packet["@id"].asText()
        val fødselsnummer = packet["fødselsnummer"].asText()
        try {
            personinfoService.løsningForVergemål(behovId, fødselsnummer).let { vergemålløsning ->
                packet["@løsning"] = mapOf(behov to vergemålløsning)
            }
            packet.toJson().let { løsningJson ->
                context.publish(løsningJson)
                sikkerlogg.info(
                    "sender svar {}:\n\tløsning=$løsningJson",
                    keyValue("id", behovId),
                )
            }
        } catch (e: Exception) {
            sikkerlogg.error("Feil under løsing av vergemål-behov for {}: ${e.message}", keyValue("fnr", fødselsnummer))
        }
    }

    enum class VergemålType {
        ensligMindreaarigAsylsoeker,
        ensligMindreaarigFlyktning,
        voksen,
        midlertidigForVoksen,
        mindreaarig,
        midlertidigForMindreaarig,
        forvaltningUtenforVergemaal,
        stadfestetFremtidsfullmakt;

        companion object {
            fun gyldig(type: String): Boolean {
                return enumValues<VergemålType>().any { it.name == type }
            }
        }
    }

    internal data class Vergemål(
        val type: VergemålType
    )

    internal data class Fullmakt(
        val områder: List<Område>,
        val gyldigFraOgMed: LocalDate,
        val gyldigTilOgMed: LocalDate
    )

    internal data class Resultat(
        val vergemål: List<Vergemål>,
        val fremtidsfullmakter: List<Vergemål>,
        val fullmakter: List<Fullmakt>
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
