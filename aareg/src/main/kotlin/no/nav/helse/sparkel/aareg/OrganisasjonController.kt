package no.nav.helse.sparkel.aareg

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.EregClient
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.FeilVedHenting

class OrganisasjonController(private val eregClient: EregClient) {

    fun addToRoute(route: Route) {
        route.get("/organisasjoner/{organisasjonsnummer}") {
            call.parameters["organisasjonsnummer"]
                ?.let { organisasjonsnummer -> getOrganisasjon(organisasjonsnummer = organisasjonsnummer, callId = call.callId) }
                ?.let { organisasjon -> call.respond(organisasjon) }
                ?: call.respond(HttpStatusCode.NotFound)
        }
    }

    private suspend fun getOrganisasjon(organisasjonsnummer: String, callId: String?): OrganisasjonDto? =
        try {
            eregClient.hentOrganisasjonUtenRetry(
                organisasjonsnummer = organisasjonsnummer,
                callId = callId.asUUIDIfUUID() ?: UUID.randomUUID()
            ).let { eregResponse ->
                OrganisasjonDto(
                    organisasjonsnummer = organisasjonsnummer,
                    navn = eregResponse.navn,
                )
            }
        } catch (e: FeilVedHenting) {
            if (e.statusCode == 404) null else throw e
        }

    private fun String?.asUUIDIfUUID(): UUID? =
        this?.let {
            try {
                UUID.fromString(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }

    data class OrganisasjonDto(
        val organisasjonsnummer: String,
        val navn: String,
    )
}
