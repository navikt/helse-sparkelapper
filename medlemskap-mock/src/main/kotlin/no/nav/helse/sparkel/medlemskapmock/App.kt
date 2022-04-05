package no.nav.helse.sparkel.medlemskapmock

import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        Medlemskap(this)
    }.start()
}
