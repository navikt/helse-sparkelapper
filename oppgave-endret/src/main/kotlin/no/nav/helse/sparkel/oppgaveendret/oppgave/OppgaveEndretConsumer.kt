package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.GosysOppgaveSykEndretProducer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

internal class OppgaveEndretConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val gosysOppgaveSykEndretProducer: GosysOppgaveSykEndretProducer,
    private val objectMapper: ObjectMapper
) : AutoCloseable, Runnable {
    private var konsumerer = true
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run() {
        logger.info("OppgaveEndretConsumer starter opp")
        try {
            while (konsumerer) {
                kafkaConsumer.poll(Duration.ofMillis(100)).forEach { consumerRecord ->
                    val record = consumerRecord.value()
                    val oppgave: Oppgave = objectMapper.readValue(record)
                    logger.info("Mottatt oppgave_endret {}", oppgave.id)

                    gosysOppgaveSykEndretProducer.onPacket(oppgave)
                }
            }
        } catch (exception: Exception) {
            logger.error("Feilet under konsumering av oppgave_endret", exception)
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    override fun close() {
        konsumerer = false
    }

}