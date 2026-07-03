package no.nav.helse.sparkel.arbeidsgiver

import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import tools.jackson.databind.JsonNode

internal fun JsonNode.toPerioder() = toList().map {
    mapOf(
        "fom" to it["fom"].asLocalDate(),
        "tom" to it["tom"].asLocalDate()
    )
}

internal enum class Meldingstype {
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_BEGRENSET,
    TRENGER_IKKE_OPPLYSNINGER_FRA_ARBEIDSGIVER,
    INNTEKTSMELDING_HÅNDTERT,
    VEDTAKSPERIODE_FORKASTET
}
