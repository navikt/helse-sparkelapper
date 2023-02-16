package no.nav.helse.sparkel.sputnik

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.response.respondTextWriter
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter
import io.prometheus.client.exporter.common.TextFormat
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("MidlertidigClusterDiff")

internal val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

private val cluster get() = System.getenv("NAIS_CLUSTER_NAME")?.lowercase() ?: "prod-fss"

internal fun clusterAvhengigStart() = when (cluster) {
    "prod-gcp" -> startSimpleApp()
    "dev-gcp" -> startSimpleApp()
    else -> startOnPrem()
}

private fun startOnPrem() {
    log.info("Starter on prem rapids app for $cluster")
    val serviceUser = readServiceUserCredentials()
    val environment = setUpEnvironment()
    val stsRestClient = StsRestClient(environment.stsBaseUrl, serviceUser)
    val fpsakRestClient = FpsakRestClient(
        baseUrl = environment.fpsakBaseUrl,
        httpClient = simpleHttpClient(),
        stsRestClient = stsRestClient
    )
    startRapidsApplication(fpsakRestClient)
}

private fun startSimpleApp() {
    log.info("Starter simple app for $cluster")
    val requests = Counter.build("requests", "requests").labelNames("endpoint").register()
    embeddedServer(CIO, port = 8080) {
        routing {
            get("/isalive") {
                requests.labels("isalive").inc()
                call.respondText("ALIVE!") }
            get("/isready") {
                requests.labels("isready").inc()
                call.respondText("READY!") }
            get("/metrics") {
                requests.labels("metrics").inc()
                call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
                    TextFormat.write004(this, CollectorRegistry.defaultRegistry.metricFamilySamples())
                }
            }
        }
    }.start(wait = true)
}

private fun simpleHttpClient() = HttpClient {
    install(ContentNegotiation) {
        jackson {
            objectMapper
        }
    }
    install(HttpTimeout) {
        connectTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        socketTimeoutMillis = 10000
    }
}
