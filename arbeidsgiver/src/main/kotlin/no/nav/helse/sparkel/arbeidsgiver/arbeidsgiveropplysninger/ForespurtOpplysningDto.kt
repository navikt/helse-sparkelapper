package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import org.slf4j.LoggerFactory
import tools.jackson.databind.JsonNode

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal sealed class ForespurtOpplysning {

    companion object {
        fun List<ForespurtOpplysning>.toJsonMap() = map { forespurtOpplysning ->
            when (forespurtOpplysning) {
                is Arbeidsgiverperiode -> mapOf(
                    "opplysningstype" to "Arbeidsgiverperiode"
                )

                is Inntekt -> mapOf(
                    "opplysningstype" to "Inntekt"
                )

                is Refusjon -> mapOf(
                    "opplysningstype" to "Refusjon"
                )
            }
        }
    }
}

internal data object Refusjon: ForespurtOpplysning()
internal data object Arbeidsgiverperiode : ForespurtOpplysning()
internal data object Inntekt: ForespurtOpplysning()

internal fun JsonNode.asBestemmendeFraværsdager() =
    associate { it["organisasjonsnummer"].asString() to it["førsteFraværsdag"].asLocalDate() }

internal fun JsonNode.asForespurteOpplysninger(): List<ForespurtOpplysning> =
    mapNotNull(JsonNode::asForespurtOpplysning)

internal fun JsonNode.asForespurtOpplysning() = when (val opplysningstype = this["opplysningstype"].asString()) {
    "Inntekt" -> Inntekt
    "Refusjon" -> Refusjon
    "Arbeidsgiverperiode" -> Arbeidsgiverperiode
    else -> {
        sikkerlogg.error("Mottok et trenger_opplysninger_fra_arbeidsgiver-event med ukjent opplysningtype: $opplysningstype")
        null
    }
}
