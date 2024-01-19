package no.nav.helse.sparkel.arena

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.soap.InMemoryStsClient
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.MinimalStsClient
import com.github.navikt.tbd_libs.soap.samlStrategy
import java.net.URI
import java.net.http.HttpClient
import java.nio.file.Files
import java.nio.file.Paths
import no.nav.helse.rapids_rivers.RapidApplication

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val username = env["SERVICEUSER_NAME"] ?: "/var/run/secrets/nais.io/service_user/username".readFile()
        val password = env["SERVICEUSER_PASSWORD"] ?: "/var/run/secrets/nais.io/service_user/password".readFile()

        val azureClient = createAzureTokenClientFromEnvironment(env)
        val proxyAuthorization = env["WS_PROXY_SCOPE"]?.let { scope ->
            { "Bearer ${azureClient.bearerToken(scope).token}" }
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

private fun String.readFile() = Files.readString(Paths.get(this))

