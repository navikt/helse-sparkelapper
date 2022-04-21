package no.nav.helse.sparkel.norg

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Routing.registerBehandlendeEnhetApi(personinfoService: PersoninfoService){
    get("/behandlendeEnhet") {
        val fødselsnummer = requireNotNull(call.request.headers["fødselsnummer"])
        call.respond(personinfoService.finnBehandlendeEnhet(fødselsnummer, "web"))
    }
}
