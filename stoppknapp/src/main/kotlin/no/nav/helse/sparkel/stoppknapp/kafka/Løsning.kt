package no.nav.helse.sparkel.stoppknapp.kafka

internal data class Løsning(
    val automatiseringStoppet: Boolean,
    val årsaker: Set<String>,
)
