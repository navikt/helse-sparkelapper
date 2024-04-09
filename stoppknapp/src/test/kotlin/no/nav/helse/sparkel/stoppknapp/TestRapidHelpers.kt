package no.nav.helse.sparkel.stoppknapp

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid

internal object TestRapidHelpers {
    internal fun TestRapid.RapidInspector.meldinger() = (0 until size).map { index -> message(index) }

    internal fun TestRapid.RapidInspector.hendelser(type: String) = meldinger().filter { it.path("@event_name").asText() == type }

    internal fun TestRapid.RapidInspector.løsninger() =
        hendelser("behov")
            .filter { it.hasNonNull("@løsning") }

    internal fun TestRapid.RapidInspector.løsning(behov: String): JsonNode? =
        løsninger()
            .findLast { it.path("@behov").map(JsonNode::asText).contains(behov) }
            ?.path("@løsning")
            ?.path(behov)
}
