package no.nav.helse.sparkel.sykepengeperioderapi

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.time.LocalDateTime

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::sykepengeperioderApi).start(wait = true)
}

private fun Application.sykepengeperioderApi() {
    routing {
        get("/isalive") { call.respondText("ÆØÅæøå ${LocalDateTime.now()}") }
        get("/isready") { call.respondText("READY!") }
    }
}