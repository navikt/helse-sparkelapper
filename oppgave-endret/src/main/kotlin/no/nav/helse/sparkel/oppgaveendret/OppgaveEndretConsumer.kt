package no.nav.helse.sparkel.oppgaveendret

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidsConnection
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

internal class OppgaveEndretConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<String, String>
) : AutoCloseable, Runnable {
    private var konsumerer = true
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    override fun run() {
        logger.info("OppgaveEndretConsumer starter opp")
        try {
            while (konsumerer) {
                kafkaConsumer.poll(Duration.ofMillis(100)).forEach { consumerRecord ->
                    val record = consumerRecord.value()
                    logger.info("Value on kafka topic: " + record)
                    val oppgave: Oppgave = objectMapper.readValue(record)
                    logger.info("Mottatt oppgave_endret {}", oppgave.id)

                    // TODO use rapidsConnection to produce need
                }
            }
        } catch (e: Exception) {
            logger.error("Feilet under konsumering av oppgave_endret", e)
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    override fun close() {
        konsumerer = false
    }

}