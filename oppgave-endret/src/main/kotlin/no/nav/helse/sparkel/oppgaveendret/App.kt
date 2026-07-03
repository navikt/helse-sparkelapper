package no.nav.helse.sparkel.oppgaveendret

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import java.time.Clock
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.oppgaveendret.kafka.createConsumer
import no.nav.helse.sparkel.oppgaveendret.oppgave.OppgaveEndretConsumer
import tools.jackson.databind.ObjectMapper
import tools.jackson.module.kotlin.jacksonObjectMapper

fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal val objectMapper: ObjectMapper = jacksonObjectMapper()

internal fun createApp(env: Map<String, String>): RapidsConnection {

    val consumeTopic = System.getenv("OPPGAVE_ENDRET_TOPIC")
    val kafkaConsumerOppgaveEndret = createConsumer()
    kafkaConsumerOppgaveEndret.subscribe(listOf(consumeTopic))

    return RapidApplication.create(env).apply {
        val gosysOppgaveEndretProducer = GosysOppgaveEndretProducer(this)
        val oppgaveEndretConsumer = OppgaveEndretConsumer(
            this,
            kafkaConsumerOppgaveEndret,
            gosysOppgaveEndretProducer,
            objectMapper,
            Clock.systemDefaultZone(),
        )
        Thread(oppgaveEndretConsumer).start()
        this.register(object : RapidsConnection.StatusListener {
            override fun onShutdown(rapidsConnection: RapidsConnection) {
                oppgaveEndretConsumer.close()
            }
        })
    }
}
