package no.nav.helse.sparkel.arbeidsgiver

import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("sparkel-arbeidsgiver")

fun main() {
    val app = RapidApplication.create(System.getenv())
    logger.info("Hei, bro!")
    app.start()
}
