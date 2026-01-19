package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asOptionalLocalDate
import java.time.LocalDate
import org.slf4j.LoggerFactory

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

internal sealed class ForespurtOpplysning {

    companion object {
        fun List<ForespurtOpplysning>.toJsonMap() = map { forespurtOpplysning ->
            when (forespurtOpplysning) {
                is Arbeidsgiverperiode -> mapOf(
                    "opplysningstype" to "Arbeidsgiverperiode"
                )

                is Inntekt -> mapOf(
                    "opplysningstype" to "Inntekt",
                    "forslag" to forespurtOpplysning.forslag.toJsonMap()
                )

                is Refusjon -> mapOf(
                    "opplysningstype" to "Refusjon",
                    "forslag" to forespurtOpplysning.forslag.map {
                        mapOf(
                            "fom" to it.fom,
                            "tom" to it.tom,
                            "beløp" to it.beløp
                        )
                    }
                )
            }
        }

        fun Inntektsforslag.toJsonMap(): Map<String, Any?> =
            mapOf(
                "forrigeInntekt" to this.forrigeInntekt?.let { forrigeInntekt ->
                    mapOf(
                        "skjæringstidspunkt" to forrigeInntekt.skjæringstidspunkt,
                        "kilde" to forrigeInntekt.kilde,
                        "beløp" to forrigeInntekt.beløp
                    )
                }
            )
    }
}

internal data class Refusjonsforslag(val fom: LocalDate, val tom: LocalDate?, val beløp: Double)
internal data class Refusjon(val forslag: List<Refusjonsforslag>) : ForespurtOpplysning()
internal object Arbeidsgiverperiode : ForespurtOpplysning()
internal data class Inntektsforslag(val forrigeInntekt: ForrigeInntekt? = null)
internal data class ForrigeInntekt(val skjæringstidspunkt: LocalDate, val kilde: String, val beløp: Double)
internal data class Inntekt(val forslag: Inntektsforslag) : ForespurtOpplysning()

internal fun JsonNode.asBestemmendeFraværsdager() =
    associate { it["organisasjonsnummer"].asText() to it["førsteFraværsdag"].asLocalDate() }

internal fun JsonNode.asForespurteOpplysninger(): List<ForespurtOpplysning> =
    mapNotNull(JsonNode::asForespurtOpplysning)

internal fun JsonNode.asForespurtOpplysning() = when (val opplysningstype = this["opplysningstype"].asText()) {
    "Inntekt" -> Inntekt(forslag = this["forslag"].asInntektsforslag())
    "Refusjon" -> Refusjon(forslag = this["forslag"].asRefusjonsforslag())
    "Arbeidsgiverperiode" -> Arbeidsgiverperiode
    else -> {
        sikkerlogg.error("Mottok et trenger_opplysninger_fra_arbeidsgiver-event med ukjent opplysningtype: $opplysningstype")
        null
    }
}

private fun JsonNode.asInntektsforslag(): Inntektsforslag =
    Inntektsforslag(forrigeInntekt = this["forrigeInntekt"]?.asForrigeInntekt())

private fun JsonNode.asForrigeInntekt(): ForrigeInntekt? =
    if (this.isNull) null
    else {
        ForrigeInntekt(
            skjæringstidspunkt = this["skjæringstidspunkt"].asLocalDate(),
            kilde = this["kilde"].asText(),
            beløp = this["beløp"].asDouble()
        )
    }

private fun JsonNode.asRefusjonsforslag() = map {
    Refusjonsforslag(
        it["fom"].asLocalDate(),
        it["tom"].asOptionalLocalDate(),
        it["beløp"].asDouble(),
    )
}
