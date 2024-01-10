package no.nav.helse.sparkel.norg

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.jackson.jackson
import no.nav.helse.rapids_rivers.RapidApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal const val NAV_OPPFOLGING_UTLAND_KONTOR_NR = "0393"

private val sikkerLogg: Logger = LoggerFactory.getLogger("tjenestekall")

fun main() {
    val environment = readEnvironment()
    launchApplication(environment)
}

fun launchApplication(environment: Environment) {
    val azureClient = createAzureTokenClientFromEnvironment()
    val norgRestClient = Norg2Client(
        baseUrl = environment.norg2BaseUrl,
        scope = environment.norg2Scope,
        azureClient = azureClient,
        httpClient = simpleHttpClient()
    )

    val pdl = PDL(azureClient, environment.pdlUrl, environment.pdlScope)

    val behandlendeEnhetService = PersoninfoService(norgRestClient, pdl)

    RapidApplication.create(System.getenv()).apply {
        BehandlendeEnhetRiver(this, behandlendeEnhetService)
    }.start()
}

private fun simpleHttpClient() = HttpClient {
    install(Logging) {
        level = LogLevel.BODY
        logger = object : io.ktor.client.plugins.logging.Logger {
            override fun log(message: String) {
                sikkerLogg.debug(message)
            }
        }
    }
    install(ContentNegotiation) {
        jackson()
    }
    expectSuccess = false
}
