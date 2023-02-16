package no.nav.helse.sparkel.sputnik

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    clusterAvhengigStart()
}

internal fun startRapidsApplication(foreldrepengerløser: Foreldrepengerløser) =
    RapidApplication.create(System.getenv()).apply {
        Foreldrepenger(this, foreldrepengerløser)
    }.start()