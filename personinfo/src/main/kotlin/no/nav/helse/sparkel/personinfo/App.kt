package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import java.io.File
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseConsumer
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseRiver
import no.nav.helse.sparkel.personinfo.leesah.createConsumer
import no.nav.helse.sparkel.personinfo.v3.HentPersoninfoV3Løser

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureAdClient = HttpClient(Apache) {
        install(ContentNegotiation) {
            register(
                ContentType.Application.Json, JacksonConverter(
                    jacksonObjectMapper()
                        .registerModule(JavaTimeModule())
                )
            )
        }
    }

    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        accessTokenClient = AccessTokenClient(
            aadAccessTokenUrl = env.getValue("AZURE_OPENID_CONFIG_TOKEN_ENDPOINT"),
            clientId = env.getValue("AZURE_APP_CLIENT_ID"),
            clientSecret = env.getValue("AZURE_APP_CLIENT_SECRET"),
            httpClient = azureAdClient),
        accessTokenScope = System.getenv("ACCESS_TOKEN_SCOPE"),
    )
    val personinfoService = PersoninfoService(pdlClient)
    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf("aapen-person-pdl-leesah-v1"))

    return RapidApplication.create(env).apply {
        val personhendelseRiver = PersonhendelseRiver(
            rapidsConnection = this,
            pdlClient = pdlClient,
            cacheTimeout = Duration.ofSeconds(5)
        )
        val personhendelseConsumer = PersonhendelseConsumer(this, kafkaConsumer, personhendelseRiver)
        Thread(personhendelseConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                personhendelseConsumer.close()
            }
        })
        Dødsinfoløser(this, personinfoService)
        HentPersoninfoV2Løser(this, personinfoService)
        HentPersoninfoV3Løser(this, pdlClient)
        HentIdenterLøser(this, pdlClient)
        Vergemålløser(this, personinfoService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
