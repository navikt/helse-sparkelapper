package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.kafka.AivenConfig
import com.github.navikt.tbd_libs.kafka.ConsumerProducerFactory
import com.github.navikt.tbd_libs.rapids_and_rivers.createDefaultKafkaRapidFromEnv
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import java.net.InetAddress
import java.util.UUID
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = System.getenv()
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, PrometheusRegistry.defaultRegistry, Clock.SYSTEM)
    val kafkaRapid = createDefaultKafkaRapidFromEnv(
        factory = ConsumerProducerFactory(AivenConfig.default),
        meterRegistry = meterRegistry,
        env = env
    )
    val app = RapidApplication.Builder(
        appName = env["RAPID_APP_NAME"] ?: generateAppName(env),
        instanceId = generateInstanceId(env),
        rapid = kafkaRapid,
        meterRegistry = meterRegistry
    )
        .withKtorModule {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
            routing {
                post("/reset") {
                    log.info("Fjerner ${svarSykepengehistorikk.size} konfigurerte sykepengeperioder og ${svarUtbetalingsperioder.size} konfigurerte utbetalingsperioder")
                    svarSykepengehistorikk.clear()
                    svarUtbetalingsperioder.clear()
                    call.respond(HttpStatusCode.OK)
                }
                post("/sykepengehistorikk/{fødselsnummer}") {
                    val fødselsnummer = call.parameters["fødselsnummer"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Requesten mangler fødselsnummer"
                    )

                    val utbetalteSykeperiode = try {
                        call.receive<List<Sykepengehistorikk>>()
                    } catch (e: ContentTransformationException) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Kunne ikke parse payload")
                    }
                    svarSykepengehistorikk[fødselsnummer] = utbetalteSykeperiode
                    log.info("Oppdatererte mocket sykepengehistorikk for fnr: ${fødselsnummer.substring(0, 4)}*******")
                    call.respond(HttpStatusCode.OK)
                }
                post("/utbetalingshistorikk/{fødselsnummer}") {
                    val fødselsnummer = call.parameters["fødselsnummer"] ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Requesten mangler fødselsnummer"
                    )

                    val utbetalteSykeperiode = try {
                        call.receive<List<Utbetalingsperiode>>()
                    } catch (e: ContentTransformationException) {
                        return@post call.respond(HttpStatusCode.BadRequest, "Kunne ikke parse payload")
                    }
                    svarUtbetalingsperioder[fødselsnummer] = utbetalteSykeperiode
                    log.info("Oppdatererte mocket utbetalingshistorikk for fnr: ${fødselsnummer.substring(0, 4)}*******")
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
        .build()

    SparkelSykepengeperioderMockRiver(app, svarSykepengehistorikk)
    SparkelUtbetalingsperioderMockRiver(app, svarUtbetalingsperioder)

    app.start()
}

private fun generateInstanceId(env: Map<String, String>): String {
    if (env.containsKey("NAIS_APP_NAME")) return InetAddress.getLocalHost().hostName
    return UUID.randomUUID().toString()
}

private fun generateAppName(env: Map<String, String>): String? {
    val appName = env["NAIS_APP_NAME"] ?: return log.info("not generating app name because NAIS_APP_NAME not set").let { null }
    val namespace = env["NAIS_NAMESPACE"] ?: return log.info("not generating app name because NAIS_NAMESPACE not set").let { null }
    val cluster = env["NAIS_CLUSTER_NAME"] ?: return log.info("not generating app name because NAIS_CLUSTER_NAME not set").let { null }
    return "$appName-$cluster-$namespace"
}

private val log = LoggerFactory.getLogger("SparkelSykepengerMock")
private val svarSykepengehistorikk = mutableMapOf<String, List<Sykepengehistorikk>>()
private val svarUtbetalingsperioder = mutableMapOf<String, List<Utbetalingsperiode>>()
