package no.nav.helse.sparkel.dokumenter

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).start()
}