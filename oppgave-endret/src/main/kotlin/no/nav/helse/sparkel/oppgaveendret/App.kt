package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.Clock
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.kafka.createConsumer
import no.nav.helse.sparkel.oppgaveendret.oppgave.OppgaveEndretConsumer


fun main() {
    val app = createApp(System.getenv())
    app.start()
}

internal val objectMapper: ObjectMapper = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerKotlinModule()
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

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