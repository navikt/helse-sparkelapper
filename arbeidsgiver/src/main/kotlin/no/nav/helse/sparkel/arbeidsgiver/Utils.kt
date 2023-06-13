package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.asLocalDate

internal fun JsonNode.toPerioder() = map {
    mapOf(
        "fom" to it["fom"].asLocalDate(),
        "tom" to it["tom"].asLocalDate()
    )
}