package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.time.YearMonth
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asYearMonth
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal sealed class ForespurtOpplysning() {
    fun List<ForespurtOpplysning>.toJsonMap() = map { forespurtOpplysning ->
        when (forespurtOpplysning) {
            is Arbeidsgiverperiode -> mapOf(
                "opplysningstype" to "Arbeidsgiverperiode",
                "forslag" to forespurtOpplysning.forslag.map { forslag ->
                    mapOf(
                        "fom" to forslag["fom"],
                        "tom" to forslag["tom"]
                    )
                }
            )
            is FastsattInntekt -> mapOf(
                "opplysningstype" to "FastsattInntekt",
                "fastsattInntekt" to forespurtOpplysning.fastsattInntekt
            )
            is Inntekt -> mapOf(
                "opplysningstype" to "Inntekt",
                "forslag" to mapOf("beregningsmåneder" to forespurtOpplysning.forslag.beregningsmåneder)
            )
            Refusjon -> mapOf("opplysningstype" to "Refusjon")
        }
    }
}

internal object Refusjon : ForespurtOpplysning()
internal data class Arbeidsgiverperiode(val forslag: List<Map<String, LocalDate>>) : ForespurtOpplysning()
internal data class FastsattInntekt(val fastsattInntekt: Double) : ForespurtOpplysning()

internal data class Inntektsforslag(val beregningsmåneder: List<YearMonth>)
internal data class Inntekt(val forslag: Inntektsforslag) : ForespurtOpplysning()

internal fun JsonNode.asForespurteOpplysninger(): List<ForespurtOpplysning> =
    mapNotNull { forespurtOpplysning ->
        when (val opplysningstype = forespurtOpplysning["opplysningstype"].asText()) {
            "Inntekt" -> Inntekt(forslag = forespurtOpplysning["forslag"].asInntektsforslag())
            "Refusjon" -> Refusjon
            "Arbeidsgiverperiode" -> Arbeidsgiverperiode(forslag = forespurtOpplysning["forslag"].asArbeidsgiverperiodeforslag())
            "FastsattInntekt" -> FastsattInntekt(fastsattInntekt = forespurtOpplysning["fastsattInntekt"].asDouble())
            else -> {
                sikkerlogg.error("Mottok et trenger_opplysninger_fra_arbeidsgiver-event med ukjent opplysningtype: $opplysningstype")
                null
            }
        }
    }

private fun JsonNode.asArbeidsgiverperiodeforslag(): List<Map<String, LocalDate>> = map { periode ->
    mapOf(
        "fom" to periode["fom"].asLocalDate(),
        "tom" to periode["tom"].asLocalDate()
    )
}
private fun JsonNode.asInntektsforslag() = Inntektsforslag( beregningsmåneder = this["beregningsmåneder"].map(JsonNode::asYearMonth))