package no.nav.helse.sparkel.aareg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createDefaultAzureTokenClient
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.server.application.Application
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.aareg.arbeidsforhold.AaregClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.ArbeidsforholdLøserV2
import no.nav.helse.sparkel.aareg.arbeidsforhold.Arbeidsforholdbehovløser
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.Arbeidsgiverinformasjonsbehovløser
import no.nav.helse.sparkel.aareg.arbeidsgiverinformasjon.EregClient
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val sikkerlogg: Logger = LoggerFactory.getLogger("tjenestekall")

internal val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .registerModule(JavaTimeModule())

fun main() {
    val app = App()
    app.start(
        configuration = configurationFromEnvironment(),
        rapidsConnection = RapidApplication.create(
            env = System.getenv(),
            builder = {
                withKtorModule {
                    app.ktorSetupCallback(this)
                }
            },
        )
    )
}

class App() {
    lateinit var ktorSetupCallback: (Application) -> Unit

    fun start(
        configuration: Configuration,
        rapidsConnection: RapidsConnection
    ) {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                register(ContentType.Application.Json, JacksonConverter(objectMapper))
            }
            expectSuccess = false
        }

        val azureAD = createDefaultAzureTokenClient(
            tokenEndpoint = configuration.tokenEndpointURL,
            clientId = configuration.clientId,
            clientSecret = configuration.clientSecret
        )

        val kodeverkClient = KodeverkClient(
            kodeverkBaseUrl = configuration.kodeverkBaseUrl,
            kodeverkOauthScope = configuration.kodeverkOauthScope,
            azureTokenProvider = azureAD,
        )

        val eregClient = EregClient(
            baseUrl = configuration.organisasjonBaseUrl,
            appName = configuration.appName,
            httpClient = httpClient
        )

        val aaregClient = AaregClient(
            baseUrl = configuration.aaregBaseUrlRest,
            scope = configuration.aaregOauthScope,
            tokenSupplier = azureAD,
            httpClient = httpClient
        )

        ktorSetupCallback = { ktorApplication ->
            KtorModule(
                clientId = configuration.clientId,
                issuerUrl = configuration.issuerUrl,
                jwkProviderUri = configuration.jwkProviderUri,
                eregClient = eregClient,
            ).ktorModule(
                application = ktorApplication,
            )
        }

        Arbeidsgiverinformasjonsbehovløser(rapidsConnection, kodeverkClient, eregClient)
        Arbeidsforholdbehovløser(rapidsConnection, aaregClient)
        ArbeidsforholdLøserV2(rapidsConnection, aaregClient)

        rapidsConnection.start()
    }
}
