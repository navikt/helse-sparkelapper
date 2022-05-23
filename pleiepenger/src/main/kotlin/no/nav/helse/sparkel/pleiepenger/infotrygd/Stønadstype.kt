package no.nav.helse.sparkel.pleiepenger.infotrygd

internal enum class Stønadstype(val url: String) {
    PLEIEPENGER("/vedtak/pleiepenger"),
    OMSORGSPENGER("/vedtak/omsorgspenger"),
    OPPLAERINGSPENGER("/vedtak/opplaeringspenger")
}