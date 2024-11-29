package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.naisful.naisApp
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = System.getenv()
    val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT, PrometheusRegistry.defaultRegistry, Clock.SYSTEM)
    val app = RapidApplication.create(
        env = env,
        meterRegistry = meterRegistry,
        builder = {
            withKtor { preStopHook, rapid ->
                naisApp(
                    meterRegistry = meterRegistry,
                    objectMapper = objectMapper,
                    applicationLogger = LoggerFactory.getLogger("no.nav.helse.sparkel.sykepengeperiodermock.App"),
                    callLogger = LoggerFactory.getLogger("no.nav.helse.sparkel.sykepengeperiodermock.CallLogging"),
                    naisEndpoints = com.github.navikt.tbd_libs.naisful.NaisEndpoints.Default,
                    timersConfig = { call, _ ->
                        this
                            // https://github.com/linkerd/polixy/blob/main/DESIGN.md#l5d-client-id-client-id
                            // eksempel: <APP>.<NAMESPACE>.serviceaccount.identity.linkerd.cluster.local
                            .tag("konsument", call.request.header("L5d-Client-Id") ?: "n/a")
                    },
                    mdcEntries = mapOf(
                        "konsument" to { call: ApplicationCall -> call.request.header("L5d-Client-Id") }
                    ),
                    aliveCheck = rapid::isReady,
                    readyCheck = rapid::isReady,
                    preStopHook = preStopHook::handlePreStopRequest
                ) {
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
                            } catch (_: ContentTransformationException) {
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
                            } catch (_: ContentTransformationException) {
                                return@post call.respond(HttpStatusCode.BadRequest, "Kunne ikke parse payload")
                            }
                            svarUtbetalingsperioder[fødselsnummer] = utbetalteSykeperiode
                            log.info("Oppdatererte mocket utbetalingshistorikk for fnr: ${fødselsnummer.substring(0, 4)}*******")
                            call.respond(HttpStatusCode.OK)
                        }
                    }

                }
            }
        }
    )

    SparkelSykepengeperioderMockRiver(app, svarSykepengehistorikk)
    SparkelUtbetalingsperioderMockRiver(app, svarUtbetalingsperioder)

    app.start()
}

private val log = LoggerFactory.getLogger("SparkelSykepengerMock")
private val svarSykepengehistorikk = mutableMapOf<String, List<Sykepengehistorikk>>()
private val svarUtbetalingsperioder = mutableMapOf<String, List<Utbetalingsperiode>>()
