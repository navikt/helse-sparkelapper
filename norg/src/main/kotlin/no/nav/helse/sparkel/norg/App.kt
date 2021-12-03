package no.nav.helse.sparkel.norg

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.logging.*
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal const val NAV_OPPFOLGING_UTLAND_KONTOR_NR = "0393"

private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val serviceUser = readServiceUserCredentials()
    val environment = readEnvironment()
    launchApplication(environment, serviceUser)
}

fun launchApplication(
    environment: Environment,
    serviceUser: ServiceUser
) {
    val norgRestClient = Norg2Client(
        baseUrl = environment.norg2BaseUrl,
        httpClient = simpleHttpClient()
    )

    val sts = STS(environment.securityTokenServiceUrl, serviceUser)
    val pdl = PDL(sts, environment.pdlUrl)

    val behandlendeEnhetService = PersoninfoService(norgRestClient, pdl)

    RapidApplication.create(System.getenv()).apply {
        BehandlendeEnhetRiver(this, behandlendeEnhetService)
    }.start()
}

private fun simpleHttpClient(serializer: JacksonSerializer? = JacksonSerializer()) = HttpClient {
    install(Logging) {
        level = LogLevel.BODY
        logger = object : io.ktor.client.features.logging.Logger {
            override fun log(message: String) {
                sikkerLogg.debug(message)
            }
        }
    }
    install(JsonFeature) {
        this.serializer = serializer
    }
    expectSuccess = false
}
