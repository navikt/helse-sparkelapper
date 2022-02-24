package no.nav.helse.sparkel.vilkarsproving

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.vilkarsproving.egenansatt.*
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

    val aad: AzureAD? = try {
        AzureAD(AzureADProps(env.tokenEndpointURL, env.clientId, env.clientSecret, env.skjermendeOauthScope))
            .also { logger.info("Initielt token mot skjermende hentet fra AD.") }
    } catch (ex: Exception) {
        logger.error("Klarte ikke å opprette skjermende-klient: $ex",  ex)
        null
    }
    val skjermedePersoner = SkjermedePersoner(aad, env.skjermedeBaseURL)

    EgenAnsattLøser(rapidsConnection, skjermedePersoner)

    return rapidsConnection
}
