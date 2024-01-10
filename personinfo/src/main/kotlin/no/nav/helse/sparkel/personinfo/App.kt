package no.nav.helse.sparkel.personinfo

import com.github.navikt.tbd_libs.azure.createAzureTokenClientFromEnvironment
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
    val azureClient = createAzureTokenClientFromEnvironment(env)
    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        accessTokenClient = azureClient,
        accessTokenScope = System.getenv("ACCESS_TOKEN_SCOPE"),
    )
    val personinfoService = PersoninfoService(pdlClient)
    val kafkaConsumer = createConsumer()
    kafkaConsumer.subscribe(listOf("pdl.leesah-v1"))

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
