package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.result_object.Result
import com.github.navikt.tbd_libs.speed.VergemålEllerFremtidsfullmaktResponse.Vergemåltype
import io.micrometer.core.instrument.MeterRegistry
import net.logstash.logback.argument.StructuredArguments.keyValue
import org.slf4j.LoggerFactory

internal class Vergemålløser(
    rapidsConnection: RapidsConnection,
    private val personinfoService: PersoninfoService
) : River.PacketListener {

    companion object {
        private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
        private val logg = LoggerFactory.getLogger(this::class.java)
        const val behov = "Vergemål"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.rejectKey("@løsning") }
            validate { it.requireKey("@id") }
            validate { it.requireKey("fødselsnummer") }
        }.register(this)
    }

    override fun onError(problems: MessageProblems, context: MessageContext, metadata: MessageMetadata) {
        sikkerlogg.error("forstod ikke behov $behov med melding\n${problems.toExtendedReport()}")
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext, metadata: MessageMetadata, meterRegistry: MeterRegistry) {
        sikkerlogg.info("mottok melding: ${packet.toJson()}")
        val behovId = packet["@id"].asText()
        val fødselsnummer = packet["fødselsnummer"].asText()
        withMDC("callId" to behovId) {
            try {
                sikkerlogg.info("løser behov Vergemål {}", keyValue("id", behovId))
                logg.info("løser behov Vergemål {}", keyValue("id", behovId))
                when (val svar = personinfoService.løsningForVergemål(behovId, fødselsnummer)) {
                    is Result.Error -> sikkerlogg.error("Feil under løsing av vergemål-behov for {}: ${svar.error}", keyValue("fnr", fødselsnummer), svar.cause)
                    is Result.Ok -> {
                        val (fremtidsfullmakter, vergemål) = svar.value
                            .vergemålEllerFremtidsfullmakter
                            .map {
                                Vergemål(
                                    type = when (it.type) {
                                        Vergemåltype.ENSLIG_MINDREÅRIG_ASYLSØKER -> VergemålType.ensligMindreaarigAsylsoeker
                                        Vergemåltype.ENSLIG_MINDREÅRIG_FLYKTNING -> VergemålType.ensligMindreaarigFlyktning
                                        Vergemåltype.VOKSEN -> VergemålType.voksen
                                        Vergemåltype.MIDLERTIDIG_FOR_VOKSEN -> VergemålType.midlertidigForVoksen
                                        Vergemåltype.MINDREÅRIG -> VergemålType.mindreaarig
                                        Vergemåltype.MIDLERTIDIG_FOR_MINDREÅRIG -> VergemålType.midlertidigForMindreaarig
                                        Vergemåltype.FORVALTNING_UTENFOR_VERGEMÅL -> VergemålType.forvaltningUtenforVergemaal
                                        Vergemåltype.STADFESTET_FREMTIDSFULLMAKT -> VergemålType.stadfestetFremtidsfullmakt
                                    }
                                )
                            }
                            .partition {
                                it.type == VergemålType.stadfestetFremtidsfullmakt
                            }
                        val løsning = Resultat(
                            vergemål = vergemål,
                            fremtidsfullmakter = fremtidsfullmakter
                        )
                        packet["@løsning"] = mapOf(behov to løsning)
                        packet.toJson().let { løsningJson ->
                            context.publish(løsningJson)
                            sikkerlogg.info("sender Vergemål-svar {}:\n\tløsning=$løsningJson", keyValue("id", behovId))
                        }
                    }
                }
            } catch (e: Exception) {
                sikkerlogg.error("Feil under løsing av vergemål-behov for {}: ${e.message}", keyValue("fnr", fødselsnummer))
            }
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

    internal data class Resultat(
        val vergemål: List<Vergemål>,
        val fremtidsfullmakter: List<Vergemål>
    )
}
