package no.nav.helse.sparkel.sykepengeperioderapi

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.Url
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Infotrygdutbetalinger
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val logg = LoggerFactory.getLogger(::main::class.java)
private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variablen $this" }
private val objectMapper = jacksonObjectMapper()

fun main() {
    try {
        embeddedServer(ConfiguredCIO, port = 8080, module = Application::sykepengeperioderApi).start(wait = true)
    } catch (err: Exception) {
        sikkerlogg.error("Feil ved oppstart av applikasjonen! ${err.message}", err)
        logg.error("Feil ved oppstart av applikasjonen! Se sikker logg")
    }
}

private val List<Infotrygdperiode>.response get() = objectMapper.createObjectNode().let { json ->
    val utbetaltePerioder = map { objectMapper.createObjectNode()
        .put("personidentifikator", it.personidentifikator.toString())
        .put("organisasjonsnummer", it.organisasjonsnummer?.toString())
        .put("fom", "${it.fom}")
        .put("tom", "${it.tom}")
        .put("grad", it.grad)
        .apply {
            putArray("tags").let { tags -> it.tags.forEach(tags::add) }
        }
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

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            sikkerlogg.error("Feil ved håndtering av ${call.request.httpMethod.value} - ${call.request.path()}", cause)
            call.respond(InternalServerError)
        }
    }

    val dataSource = try {
        HikariDataSource(HikariConfig().apply {
            jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText()
            username = File("/var/run/secrets/nais.io/oracle/creds/username").readText()
            password = File("/var/run/secrets/nais.io/oracle/creds/password").readText()
            schema = "DATABASE_SCHEMA".env
            connectionTimeout = Duration.ofSeconds(10).toMillis()
            maxLifetime = Duration.ofMinutes(30).toMillis()
            initializationFailTimeout = Duration.ofMinutes(1).toMillis()
        })
    } catch (err: Exception) {
        throw RuntimeException("Feil ved oppkobling til Oracle", err)
    }

    val infotrygdutbetalinger = Infotrygdutbetalinger(dataSource)

    authentication {
        jwt {
            val jwkProvider = JwkProviderBuilder(URI("AZURE_OPENID_CONFIG_JWKS_URI".env).toURL())
                .proxied(ProxyBuilder.http(Url("HTTP_PROXY".env)))
                .build()

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
                val request = objectMapper.readTree(call.receiveText())
                val personidentifikatorer = request.path("personidentifikatorer")
                    .map { Personidentifikator(it.asText()) }
                    .toSet()
                    .takeUnless { it.isEmpty() } ?: throw IllegalArgumentException("Det må sendes med minst én personidentifikator")
                val fom = LocalDate.parse(request.path("fom").asText())
                val tom = LocalDate.parse(request.path("tom").asText())
                val perioder = infotrygdutbetalinger.utbetalinger(personidentifikatorer, fom, tom).markerUsikkerGrad()
                val response = perioder.response
                sikkerlogg.info("Sender perioder:\n\t$response")
                call.respondText(response, Json)
            }
        }
    }
}