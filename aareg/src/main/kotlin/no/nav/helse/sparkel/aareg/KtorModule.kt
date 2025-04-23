package no.nav.helse.sparkel.aareg

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.net.URI
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.EregClient
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.FeilVedHenting
import org.slf4j.event.Level

class KtorModule(
    private val clientId: String,
    private val issuerUrl: String,
    private val jwkProviderUri: String,
    private val eregClient: EregClient
) {
    fun ktorModule(application: Application) {
        with(application) {
            install(CallId) {
                retrieveFromHeader(HttpHeaders.XRequestId)
                generate {
                    UUID.randomUUID().toString()
                }
            }
            install(CallLogging) {
                disableDefaultColors()
                logger = sikkerlogg
                level = Level.INFO
                callIdMdc("callId")
                filter { call -> call.request.path() !in setOf("/metrics", "/isalive", "/isready") }
            }
            install(ContentNegotiation) { register(ContentType.Application.Json, JacksonConverter(objectMapper)) }
            authentication {
                jwt("oidc") {
                    verifier(
                        jwkProvider = JwkProviderBuilder(URI(jwkProviderUri).toURL()).build(),
                        issuer = issuerUrl
                    ) {
                        withAudience(clientId)
                    }
                    validate { credentials ->
                        JWTPrincipal(credentials.payload)
                    }
                }
            }
            routing {
                authenticate("oidc") {
                    routings()
                }
            }
        }
    }

    private fun Route.routings() {
        get("/organisasjoner/{organisasjonsnummer}") {
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

