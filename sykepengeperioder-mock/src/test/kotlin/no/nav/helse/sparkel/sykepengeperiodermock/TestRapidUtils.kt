package no.nav.helse.sparkel.sykepengeperiodermock

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import tools.jackson.databind.JsonNode

fun TestRapid.RapidInspector.meldinger() =
    (0 until size).map { index -> message(index) }

fun TestRapid.RapidInspector.hendelser(type: String) =
    meldinger().filter { it.path("@event_name").asString() == type }

fun TestRapid.RapidInspector.løsning(behov: String): JsonNode =
    hendelser("behov")
        .filter { it.hasNonNull("@løsning") }
        .last { it.path("@behov").toList().map(JsonNode::asString).contains(behov) }
        .path("@løsning").path(behov)
