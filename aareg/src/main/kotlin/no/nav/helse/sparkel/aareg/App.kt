package no.nav.helse.sparkel.aareg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
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
    val environment = setUpEnvironment()
    val app = createApp(environment)
    app.start()
}

internal fun createApp(environment: Environment): RapidsConnection {
    val httpClient = HttpClient {
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(objectMapper))
        }
        expectSuccess = false
    }
    val azureAD = createAzureTokenClientFromEnvironment(environment.raw)

    val kodeverkClient = KodeverkClient(
        kodeverkBaseUrl = environment.kodeverkBaseUrl,
        environment.kodeverkOauthScope,
        azureAD,
    )

    val eregClient = EregClient(environment.organisasjonBaseUrl, environment.appName, httpClient)
    val aaregClient = AaregClient(environment.aaregBaseUrlRest, environment.aaregOauthScope, azureAD, httpClient)

    val rapidsConnection = RapidApplication.create(environment.raw)
    Arbeidsgiverinformasjonsbehovløser(rapidsConnection, kodeverkClient, eregClient)
    Arbeidsforholdbehovløser(rapidsConnection, aaregClient)
    ArbeidsforholdLøserV2(rapidsConnection, aaregClient)

    return rapidsConnection
}
