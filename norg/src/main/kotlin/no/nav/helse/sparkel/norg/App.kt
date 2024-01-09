package no.nav.helse.sparkel.norg

import com.github.navikt.tbd_libs.azure.AzureAuthMethod
import com.github.navikt.tbd_libs.azure.AzureTokenClient
import com.github.navikt.tbd_libs.azure.InMemoryAzureTokenCache
import io.ktor.client.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson
import java.net.URI
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
    val azureClient = environment.tokenEndpoint?.let {
        InMemoryAzureTokenCache(AzureTokenClient(
            tokenEndpoint = URI(environment.tokenEndpoint),
            clientId = environment.clientId!!,
            authMethod = AzureAuthMethod.Secret(environment.clientSecret!!)
        ))
    }
    val sts = environment.securityTokenServiceUrl?.let {
        val serviceUser = readServiceUserCredentials()
        STS(it, serviceUser)
    }
    val norgRestClient = Norg2Client(
        baseUrl = environment.norg2BaseUrl,
        scope = environment.norg2Scope,
        azureClient = azureClient,
        httpClient = simpleHttpClient()
    )

    val pdl = PDL(azureClient, sts, environment.pdlUrl, environment.pdlScope)

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
