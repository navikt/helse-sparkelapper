package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.speed.SpeedClient
import java.net.http.HttpClient
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseConsumer
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseRiver
import no.nav.helse.sparkel.personinfo.leesah.createConsumer

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = createAzureTokenClientFromEnvironment(env)
    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        accessTokenClient = azureClient,
        accessTokenScope = System.getenv("ACCESS_TOKEN_SCOPE"),
    )
    val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    val speedClient = SpeedClient(
        httpClient = HttpClient.newHttpClient(),
        objectMapper = objectMapper,
        tokenProvider = azureClient
    )
    val personinfoService = PersoninfoService(pdlClient, speedClient)
    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf("pdl.leesah-v1"))

    return RapidApplication.create(env).apply {
        val personhendelseRiver = PersonhendelseRiver(
            rapidsConnection = this,
            speedClient = speedClient,
            cacheTimeout = Duration.ofSeconds(5)
        )
        val personhendelseConsumer = PersonhendelseConsumer(this, kafkaConsumer, personhendelseRiver)
        Thread(personhendelseConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                personhendelseConsumer.close()
            }
        })
        HentPersoninfoV2Løser(this, personinfoService, objectMapper)
        HentIdenterLøser(this, speedClient)
        Vergemålløser(this, personinfoService)
    }
}
