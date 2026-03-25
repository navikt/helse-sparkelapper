package no.nav.helse.sparkel.sykepengeperioderapi

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.http.ContentType
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
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import java.io.File
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URI
import java.time.Duration
import java.time.LocalDate
import java.util.UUID
import javax.sql.DataSource
import no.nav.helse.sparkel.infotrygd.api.Infotrygdperiode
import no.nav.helse.sparkel.infotrygd.api.Infotrygdutbetalinger
import no.nav.helse.sparkel.infotrygd.api.Periodetype
import no.nav.helse.sparkel.infotrygd.api.Personidentifikator
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

private val logg = LoggerFactory.getLogger(::main::class.java)
private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
private val String.env get() = checkNotNull(System.getenv(this)) { "Fant ikke environment variablen $this" }
private val objectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
}

fun main() {
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
        sikkerlogg.error("Feil ved oppkobling til Oracle: ${err.message}", err)
        logg.error("Feil ved oppkobling til Oracle. Se sikker logg")
        throw err
    }

    try {
        embeddedServer(ConfiguredCIO, port = 8080) {
            sykepengeperioderApi(
                dataSource = dataSource,
                jwksUri = "AZURE_OPENID_CONFIG_JWKS_URI".env,
                issuer = "AZURE_OPENID_CONFIG_ISSUER".env,
                audience = "AZURE_APP_CLIENT_ID".env,
                httpProxy = System.getenv("HTTP_PROXY")
            )
        }.start(wait = true)
    } catch (err: Exception) {
        sikkerlogg.error("Feil ved oppstart av applikasjonen! ${err.message}", err)
        logg.error("Feil ved oppstart av applikasjonen! Se sikker logg")
    }
}

private fun List<Infotrygdperiode>.toResponse() = ResponseDto(
    utbetaltePerioder = map { periode ->
        UtbetaltPeriodeDto(
            personidentifikator = periode.personidentifikator.toString(),
            organisasjonsnummer = periode.organisasjonsnummer?.toString(),
            fom = periode.fom,
            tom = periode.tom,
            grad = periode.grad,
            dagsats = periode.dagsats,
            type = periode.type,
            tags = periode.tags
        )
    }
)

private data class RequestDto(
    val personidentifikatorer: List<String>,
    val fom: LocalDate,
    val tom: LocalDate,
    val inkluderAllePeriodetyper: Boolean = false
)

private data class ResponseDto(val utbetaltePerioder: List<UtbetaltPeriodeDto>)

private data class UtbetaltPeriodeDto(
    val personidentifikator: String,
    val organisasjonsnummer: String?,
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int,
    val dagsats: BigDecimal,
    val type: Periodetype,
    val tags: Set<String>
)

internal fun Application.sykepengeperioderApi(
    dataSource: DataSource,
    jwksUri: String,
    issuer: String,
    audience: String,
    httpProxy: String? = null
) {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
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

    val infotrygdutbetalinger = Infotrygdutbetalinger(dataSource)

    authentication {
        jwt {
            val jwkProviderBuilder = JwkProviderBuilder(URI(jwksUri).toURL())
            httpProxy?.let { proxy ->
                val proxyUri = URI(proxy)
                jwkProviderBuilder.proxied(Proxy(Proxy.Type.HTTP, InetSocketAddress(proxyUri.host, proxyUri.port)))
            }
            val jwkProvider = jwkProviderBuilder.build()

            verifier(jwkProvider, issuer) {
                withAudience(audience)
            }
            validate { credentials -> JWTPrincipal(credentials.payload) }
        }
    }

    routing {
        get("/isalive") { call.respondText("ISALIVE") }
        get("/isready") { call.respondText("READY") }
        authenticate {
            post {
                val request = call.receive<RequestDto>()
                val personidentifikatorer = request.personidentifikatorer
                    .map { Personidentifikator(it) }
                    .toSet()
                    .takeUnless { it.isEmpty() } ?: throw IllegalArgumentException("Det må sendes med minst én personidentifikator")
                val response = infotrygdutbetalinger.utbetalinger(personidentifikatorer, request.fom, request.tom, request.inkluderAllePeriodetyper).markerUsikkerGrad().toResponse()
                sikkerlogg.info("Sender perioder:\n\t${objectMapper.writeValueAsString(response)}")
                call.respond(response)
            }
        }
    }
}
