package no.nav.helse.sparkel.sykepengeperioderapi

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.net.URL
import java.time.LocalDate
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Infotrygdutbetalinger
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.slf4j.LoggerFactory

private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variable $this" }
private val objectMapper = jacksonObjectMapper()
private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")

fun main() {
    embeddedServer(CIO, port = 8080, module = Application::sykepengeperioderApi).start(wait = true)
}

private val List<Infotrygdperiode>.response get() = objectMapper.createObjectNode().let { json ->
    val perioder = map { objectMapper.createObjectNode()
        .put("organisasjonsnummer", it.organisasjonsnummer)
        .put("fom", "${it.fom}")
        .put("tom", "${it.tom}")
        .put("grad", it.grad)
    }
    json.putArray("perioder").addAll(perioder)
    json.toString()
}

private fun Application.sykepengeperioderApi() {

    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = File("/var/run/secrets/nais.io/oracle/config/jdbc_url").readText()
        username = File("/var/run/secrets/nais.io/oracle/creds/username").readText()
        password = File("/var/run/secrets/nais.io/oracle/creds/password").readText()
        schema = "DATABASE_SCHEMA".env
    })

    val infotrygdutbetalinger = Infotrygdutbetalinger(dataSource)

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
                val request = objectMapper.readTree(call.receiveText())
                val personidentifikator = request.path("personidentifikator").asText()
                val fom = LocalDate.parse(request.path("fom").asText())
                val tom = LocalDate.parse(request.path("tom").asText())
                val perioder = infotrygdutbetalinger.utbetalinger(Personidentifikator(personidentifikator), fom, tom)
                val response = perioder.response
                sikkerlogg.info("Sender perioder:\n\t$response")
                call.respondText(response, Json)
            }
        }
    }
}