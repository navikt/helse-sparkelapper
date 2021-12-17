package no.nav.helse.sparkel.personinfo

import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseConsumer
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseRiver
import no.nav.helse.sparkel.personinfo.leesah.createConsumer
import java.io.File
import java.time.Duration

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal fun createApp(env: Map<String, String>): RapidsConnection {
    val stsClient = StsRestClient(
        baseUrl = env.getValue("STS_BASE_URL"),
        serviceUser = "/var/run/secrets/nais.io/service_user".let {
            ServiceUser(
                "$it/username".readFile(),
                "$it/password".readFile()
            )
        }
    )
    val pdlClient = PdlClient(
        baseUrl = env.getValue("PDL_URL"),
        stsClient = stsClient
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
        Vergemålløser(this, personinfoService)
    }
}

private fun String.readFile() = File(this).readText(Charsets.UTF_8)
