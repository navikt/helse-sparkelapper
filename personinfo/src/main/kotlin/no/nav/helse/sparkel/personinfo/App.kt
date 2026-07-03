package no.nav.helse.sparkel.personinfo

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper as jackson2ObjectMapper
import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import com.github.navikt.tbd_libs.speed.SpeedClient
import java.net.http.HttpClient
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseConsumer
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseRiver
import no.nav.helse.sparkel.personinfo.leesah.createConsumer
import tools.jackson.module.kotlin.jacksonObjectMapper

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val azureClient = createAzureTokenClientFromEnvironment(env)
    val objectMapperJackson2 = jackson2ObjectMapper().registerModule(JavaTimeModule())
    val objectMapper = jacksonObjectMapper()
    val speedClient = SpeedClient(
        httpClient = HttpClient.newHttpClient(),
        objectMapper = objectMapperJackson2,
        tokenProvider = azureClient
    )
    val personinfoService = PersoninfoService(speedClient)
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
