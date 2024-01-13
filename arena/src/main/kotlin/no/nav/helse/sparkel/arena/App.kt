package no.nav.helse.sparkel.arena

import com.github.navikt.tbd_libs.soap.InMemoryStsClient
import com.github.navikt.tbd_libs.soap.MinimalSoapClient
import com.github.navikt.tbd_libs.soap.MinimalStsClient
import com.github.navikt.tbd_libs.soap.samlStrategy
import java.net.URI
import java.net.http.HttpClient
import no.nav.helse.rapids_rivers.RapidApplication
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val env = System.getenv()
    RapidApplication.create(env).apply {
        val username = "/var/run/secrets/nais.io/service_user/username".readFile()
        val password = "/var/run/secrets/nais.io/service_user/password".readFile()
        val stsClientWs = stsClient(
            stsUrl = env.getValue("STS_URL"),
            username = username,
            password = password
        )
        val ytelseskontraktV3 = YtelseskontraktFactory.create(env.getValue("YTELSESKONTRAKT_BASE_URL"), stsClientWs)
        val meldekortUtbetalingsgrunnlagV1 = MeldekortUtbetalingsgrunnlagV1Factory.create(env.getValue("MELDEKORT_UTBETALINGSGRUNNLAG_ENDPOINTURL"), stsClientWs)

        val httpClient = HttpClient.newHttpClient()
        val samlTokenClient = InMemoryStsClient(MinimalStsClient(URI(env.getValue("GANDALF_BASE_URL")), httpClient))
        val meldekortSoapClient = MinimalSoapClient(URI(env.getValue("MELDEKORT_UTBETALINGSGRUNNLAG_ENDPOINTURL")), samlTokenClient, httpClient)
        val ytelsekontraktSoapClient = MinimalSoapClient(URI(env.getValue("YTELSESKONTRAKT_BASE_URL")), samlTokenClient, httpClient)
        val assertionStrategoy = samlStrategy(username, password)
        val ytelsekontraktClient = YtelsekontraktClient(ytelsekontraktSoapClient, assertionStrategoy)
        val meldekortUtbetalingsgrunnlagClient = MeldekortUtbetalingsgrunnlagClient(meldekortSoapClient, assertionStrategoy)

        Arena(this, ytelsekontraktClient, meldekortUtbetalingsgrunnlagClient, ytelseskontraktV3, meldekortUtbetalingsgrunnlagV1, "Dagpenger", "DAG", "Dagpenger")
        Arena(this, ytelsekontraktClient, meldekortUtbetalingsgrunnlagClient, ytelseskontraktV3, meldekortUtbetalingsgrunnlagV1, "Arbeidsavklaringspenger", "AAP", "Arbeidsavklaringspenger")
    }.start()
}

private fun String.readFile() = Files.readString(Paths.get(this))

