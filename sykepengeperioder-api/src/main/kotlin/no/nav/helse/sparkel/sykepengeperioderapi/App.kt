package no.nav.helse.sparkel.sykepengeperioderapi

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.header
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.net.URL
import java.time.LocalDate
import java.util.Base64
import java.util.UUID
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Infotrygdutbetalinger
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variable $this" }
private val String.envOgLogg get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variable $this" }.also { verdi ->
    sikkerlogg.info("Config $this=$verdi")
}
private val objectMapper = jacksonObjectMapper()
private suspend fun ApplicationCall.respondChallenge() {
    try {
        val jwt = request.header(HttpHeaders.Authorization)
            ?.substringAfter("Bearer ")
            ?.split(".")
            ?.takeIf { it.size == 3 }
            ?: return respond(HttpStatusCode.Unauthorized, "Bearer token må settes i Authorization header for å hente data!")
        sikkerlogg.error("Mottok request med access token som ikke har tilgang til endepunkt ${request.path()}!\n\tJWT Headers: ${String(Base64.getUrlDecoder().decode(jwt[0]))}\n\tJWT Payload: ${String(Base64.getUrlDecoder().decode(jwt[1]))}")
        respond(HttpStatusCode.Forbidden, "Bearer token som er brukt har ikke rett tilgang til å hente data! Ta kontakt med NAV.")
    } catch (throwable: Throwable) {
        respond(HttpStatusCode.Unauthorized, "Bearer token må settes i Authorization header for å hente data!")
    }
}

fun main() {
    embeddedServer(ConfiguredCIO, port = 8080, module = Application::sykepengeperioderApi).start(wait = true)
}

private val List<Infotrygdperiode>.response get() = objectMapper.createObjectNode().let { json ->
    val utbetaltePerioder = map { objectMapper.createObjectNode()
        .put("organisasjonsnummer", it.organisasjonsnummer)
        .put("fom", "${it.fom}")
        .put("tom", "${it.tom}")
        .put("grad", it.grad)
    }
    json.putArray("utbetaltePerioder").addAll(utbetaltePerioder)
    json.toString()
}

private fun Application.sykepengeperioderApi() {

    install(CallId) {
        header("x-callId")
        verify { it.isNotEmpty() }
        generate { UUID.randomUUID().toString() }
    }
    install(CallLogging) {
        logger = sikkerlogg
        level = Level.INFO
        disableDefaultColors()
        callIdMdc("callId")
        filter { call ->
            val path = call.request.path()
            listOf("isalive", "isready", "metrics").none { path.contains(it) }
        }
    }

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText()
        username = File("/var/run/secrets/nais.io/oracle/creds/username").readText()
        password = File("/var/run/secrets/nais.io/oracle/creds/password").readText()
        schema = "DATABASE_SCHEMA".env
    })

    val infotrygdutbetalinger = Infotrygdutbetalinger(dataSource)

    /*
        System.getenv("HTTP_PROXY")?.let {
        jwkProviderBuilder.proxied(ProxyBuilder.http(it))
    }
     */

    authentication {
        jwt {
            val jwkProvider = JwkProviderBuilder(URL("AZURE_OPENID_CONFIG_JWKS_URI".envOgLogg))
                .proxied(ProxyBuilder.http(Url("HTTP_PROXY".envOgLogg)))
                .build()

            verifier(jwkProvider, "AZURE_OPENID_CONFIG_ISSUER".envOgLogg) {
                withAudience("AZURE_APP_CLIENT_ID".envOgLogg)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
            challenge { _, _ -> call.respondChallenge() }
        }
    }

    routing {
        get("/isalive") { call.respondText("ISALIVE") }
        get("/isready") { call.respondText("READY") }
        authenticate {
            post {
                val request = objectMapper.readTree(call.receiveText())
                val personidentifikatorer = request.path("personidentifikatorer")
                    .map { Personidentifikator(it.asText()) }
                    .toSet()
                    .takeUnless { it.isEmpty() } ?: throw IllegalArgumentException("Det må sendes med minst én personidentifikator")
                val fom = LocalDate.parse(request.path("fom").asText())
                val tom = LocalDate.parse(request.path("tom").asText())
                val perioder = infotrygdutbetalinger.utbetalinger(personidentifikatorer, fom, tom)
                val response = perioder.response
                sikkerlogg.info("Sender perioder:\n\t$response")
                call.respondText(response, Json)
            }
        }
    }
}