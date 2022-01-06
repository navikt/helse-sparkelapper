package no.nav.helse.sparkel.vilkarsproving

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.vilkarsproving.egenansatt.*
import no.nav.helse.sparkel.vilkarsproving.opptjening.AaregClient
import no.nav.helse.sparkel.vilkarsproving.opptjening.OpptjeningLøser
import no.nav.helse.sparkel.vilkarsproving.opptjening.StsRestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main() {
    val env = setUpEnvironment()
    val app = createApp(env)
    app.start()
}

internal val objectMapper: ObjectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(JavaTimeModule())

internal val logger: Logger = LoggerFactory.getLogger("sparkel-vilkarsproving")

fun createApp(env: Environment): RapidsConnection {
    val rapidsConnection = RapidApplication.create(env.raw)
    val serviceUser = readServiceUserCredentials()

    val stsClientWs = stsClient(
        stsUrl = env.stsSoapBaseUrl,
        serviceUser = serviceUser
    )

    val egenAnsattService = EgenAnsattFactory.create(env.egenAnsattBaseUrl, listOf())
    stsClientWs.configureFor(egenAnsattService)

    val aad: AzureAD? = try {
        AzureAD(AzureADProps(env.tokenEndpointURL, env.clientId, env.clientSecret, env.nomOauthScope))
            .also { logger.info("Initielt token mot NOM hentet fra AD.") }
    } catch (ex: Exception) {
        logger.error("Klarte ikke å opprette NOM-klient: $ex",  ex)
        null
    }
    val nom = NOM(aad, env.nomBaseURL)

    val aregClient = AaregClient(
        baseUrl = env.aaregBaseUrl,
        stsRestClient = StsRestClient(env.stsBaseUrl, serviceUser),
        httpClient = simpleHttpClient()
    )

    EgenAnsattLøser(rapidsConnection, egenAnsattService, nom)
    OpptjeningLøser(rapidsConnection, aregClient)

    return rapidsConnection
}

private fun simpleHttpClient(serializer: JacksonSerializer? = JacksonSerializer()) = HttpClient {
    install(JsonFeature) {
        this.serializer = serializer
    }
    install(HttpTimeout) {
        socketTimeoutMillis = 10000
        requestTimeoutMillis = 10000
        connectTimeoutMillis = 10000
    }
}
