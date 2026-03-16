package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid

fun TestRapid.RapidInspector.meldinger() =
    (0 until size).map { index -> message(index) }

fun TestRapid.RapidInspector.hendelser(type: String) =
    meldinger().filter { it.path("@event_name").asText() == type }

fun TestRapid.RapidInspector.løsning(behov: String): JsonNode =
    hendelser("behov")
        .filter { it.hasNonNull("@løsning") }
        .last { it.path("@behov").map(JsonNode::asText).contains(behov) }
        .path("@løsning").path(behov)
