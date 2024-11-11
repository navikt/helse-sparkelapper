package no.nav.helse.sparkel.sykepengeperiodermock

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.LoggerFactory

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

fun main() {
    val env = System.getenv()
    val app = RapidApplication.create(
        env = env,
        objectMapper = objectMapper,
        builder = {
            withKtorModule {
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
    )

    SparkelSykepengeperioderMockRiver(app, svarSykepengehistorikk)
    SparkelUtbetalingsperioderMockRiver(app, svarUtbetalingsperioder)

    app.start()
}

private val log = LoggerFactory.getLogger("SparkelSykepengerMock")
private val svarSykepengehistorikk = mutableMapOf<String, List<Sykepengehistorikk>>()
private val svarUtbetalingsperioder = mutableMapOf<String, List<Utbetalingsperiode>>()
