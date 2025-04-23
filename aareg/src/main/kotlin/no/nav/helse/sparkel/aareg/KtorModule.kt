package no.nav.helse.sparkel.aareg

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.routing.routing
import java.net.URI
import java.util.UUID
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.EregClient
import org.slf4j.event.Level

fun ktorModule(
    application: Application,
    clientId: String,
    issuerUrl: String,
    jwkProviderUri: String,
    eregClient: EregClient
) {
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
                OrganisasjonController(eregClient).addToRoute(this)
            }
        }
    }
}



