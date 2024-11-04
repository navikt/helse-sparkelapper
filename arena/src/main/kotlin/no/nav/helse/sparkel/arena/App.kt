package no.nav.helse.sparkel.arena

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.result_object.map
import com.github.navikt.tbd_libs.result_object.ok
import com.github.navikt.tbd_libs.soap.InMemoryStsClient
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.MinimalStsClient
import com.github.navikt.tbd_libs.soap.samlStrategy
import java.net.URI
import java.net.http.HttpClient
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val username = env.getValue("SERVICEUSER_NAME")
        val password = env.getValue("SERVICEUSER_PASSWORD")

        val azureClient = createAzureTokenClientFromEnvironment(env)
        val proxyAuthorization = {
            azureClient.bearerToken(env.getValue("WS_PROXY_SCOPE")).map { azureToken ->
                "Bearer ${azureToken.token}".ok()
            }
        }
        val httpClient = HttpClient.newHttpClient()
        val samlTokenClient = InMemoryStsClient(MinimalStsClient(
            baseUrl = URI(env.getValue("GANDALF_BASE_URL")),
            httpClient = httpClient,
            proxyAuthorization = proxyAuthorization
        ))
        val meldekortSoapClient = MinimalSoapClient(
            serviceUrl = URI(env.getValue("MELDEKORT_UTBETALINGSGRUNNLAG_ENDPOINTURL")),
            tokenProvider = samlTokenClient,
            httpClient = httpClient,
            proxyAuthorization = proxyAuthorization
        )
        val ytelsekontraktSoapClient = MinimalSoapClient(
            serviceUrl = URI(env.getValue("YTELSESKONTRAKT_BASE_URL")),
            tokenProvider = samlTokenClient,
            httpClient = httpClient,
            proxyAuthorization = proxyAuthorization
        )
        val assertionStrategoy = samlStrategy(username, password)
        val ytelsekontraktClient = YtelsekontraktClient(ytelsekontraktSoapClient, assertionStrategoy)
        val meldekortUtbetalingsgrunnlagClient = MeldekortUtbetalingsgrunnlagClient(meldekortSoapClient, assertionStrategoy)

        Arena(this, ytelsekontraktClient, meldekortUtbetalingsgrunnlagClient, "Dagpenger", "DAG", "Dagpenger")
        Arena(this, ytelsekontraktClient, meldekortUtbetalingsgrunnlagClient, "Arbeidsavklaringspenger", "AAP", "Arbeidsavklaringspenger")
    }.start()
}

