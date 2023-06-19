package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.asLocalDate

internal fun JsonNode.toPerioder() = map {
    mapOf(
        "fom" to it["fom"].asLocalDate(),
        "tom" to it["tom"].asLocalDate()
    )
}

internal enum class Meldingstype {
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_BEGRENSET,
    INNTEKTSMELDING_HÅNDTERT
}