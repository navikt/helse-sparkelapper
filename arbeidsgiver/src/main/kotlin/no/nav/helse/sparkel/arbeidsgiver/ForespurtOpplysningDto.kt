package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import no.nav.helse.rapids_rivers.asLocalDate
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal sealed class ForespurtOpplysning() {
    fun List<ForespurtOpplysning>.toJsonMap() = map { forespurteOpplysning ->
        when (forespurteOpplysning) {
            is Arbeidsgiverperiode -> mapOf(
                "opplysningstype" to "Arbeidsgiverperiode",
                "forslag" to forespurteOpplysning.forslag.map { forslag ->
                    mapOf(
                        "fom" to forslag["fom"],
                        "tom" to forslag["tom"]
                    )
                }
            )
            Inntekt -> mapOf("opplysningstype" to "Inntekt")
            Refusjon -> mapOf("opplysningstype" to "Refusjon")
        }
    }
}

internal object Inntekt : ForespurtOpplysning()
internal object Refusjon : ForespurtOpplysning()
internal data class Arbeidsgiverperiode(val forslag: List<Map<String, LocalDate>>) : ForespurtOpplysning()

internal fun JsonNode.asForespurteOpplysninger(): List<ForespurtOpplysning> =
    mapNotNull { forespurtOpplysning ->
        when (val opplysningstype = forespurtOpplysning["opplysningstype"].asText()) {
            "Inntekt" -> Inntekt
            "Refusjon" -> Refusjon
            "Arbeidsgiverperiode" -> Arbeidsgiverperiode(forslag = forespurtOpplysning["forslag"].asForslag())
            else -> {
                sikkerlogg.error("Mottok et trenger_opplysninger_fra_arbeidsgiver-event med ukjent opplysningtype: $opplysningstype")
                null
            }
        }

    }

private fun JsonNode.asForslag(): List<Map<String, LocalDate>> = map { periode ->
    mapOf(
        "fom" to periode["fom"].asLocalDate(),
        "tom" to periode["tom"].asLocalDate()
    )
}
