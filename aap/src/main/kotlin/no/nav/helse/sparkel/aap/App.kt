package no.nav.helse.sparkel.aap

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
    }.start()
}
