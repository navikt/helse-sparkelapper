package no.nav.helse.sparkel.sykepengeperioderapi

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.net.URL

private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variable $this" }

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::sykepengeperioderApi).start(wait = true)
}

private fun Application.sykepengeperioderApi() {
    authentication {
        jwt {
            val jwkProvider = JwkProviderBuilder(URL("AZURE_OPENID_CONFIG_JWKS_URI".env)).build()
            verifier(jwkProvider, "AZURE_OPENID_CONFIG_ISSUER".env) {
                withAudience("AZURE_APP_CLIENT_ID".env)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }

    routing {
        get("/isalive") { call.respondText("ISALIVE") }
        get("/isready") { call.respondText("READY") }
        authenticate {
            post {
                call.respondText("""{"sykepengeperioder":[]}""", Json)
            }
        }
    }
}