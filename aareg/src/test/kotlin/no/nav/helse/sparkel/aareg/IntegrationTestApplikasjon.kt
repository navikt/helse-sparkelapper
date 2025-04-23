package no.nav.helse.sparkel.aareg

import com.github.navikt.tbd_libs.rapids_and_rivers.test_support.TestRapid
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlin.random.Random
import no.nav.helse.rapids_rivers.NaisEndpoints
import no.nav.helse.rapids_rivers.ktorApplication
import no.nav.security.mock.oauth2.MockOAuth2Server

object IntegrationTestApplikasjon {
    val mockOAuth2Server = MockOAuth2Server().also { it.start() }
    val eregWireMock = newWireMock()

    private val app = App().also { app ->
        app.start(
            configuration = Configuration(
                organisasjonBaseUrl = eregWireMock.baseUrl(),
                kodeverkBaseUrl = newWireMock().baseUrl(),
                kodeverkOauthScope = "scope",
                aaregBaseUrlRest = newWireMock().baseUrl(),
                aaregOauthScope = "scope",
                tokenEndpointURL = mockOAuth2Server.tokenEndpointUrl(ISSUER_ID).toUri(),
                clientId = CLIENT_ID,
                clientSecret = "hush",
                issuerUrl = mockOAuth2Server.issuerUrl(ISSUER_ID).toString(),
                jwkProviderUri = mockOAuth2Server.jwksUrl(ISSUER_ID).toString(),
                appName = "sparkel-aareg"
            ),
            rapidsConnection = TestRapid(),
        )
    }

    private fun newWireMock(): WireMockServer =
        WireMockServer(WireMockConfiguration.options().dynamicPort())
            .also(WireMockServer::start)

    val port = Random.nextInt(10000, 20000)

    init {
        ktorApplication(
            meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT),
            naisEndpoints = NaisEndpoints.Default,
            port = port,
            aliveCheck = { true },
            readyCheck = { true },
            preStopHook = { },
            cioConfiguration = { },
            modules = listOf {
                app.ktorSetupCallback(this)
            }
        ).also { it.start() }
    }

    const val CLIENT_ID = "en-client-id"
    const val ISSUER_ID = "LocalTestIssuer"
}
