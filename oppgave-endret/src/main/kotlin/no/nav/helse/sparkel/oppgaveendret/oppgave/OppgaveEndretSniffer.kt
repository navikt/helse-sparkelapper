package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Duration
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

internal class OppgaveEndretSniffer(
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val objectMapper: ObjectMapper,
) : AutoCloseable, Runnable {
    private var konsumerer = true
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run() {
        logger.info("OppgaveEndretSniffer starter opp")
        var totalt = 0
        var irrelevante = 0
        var relevante = 0
        try {
            while (konsumerer) {
                kafkaConsumer.poll(Duration.ofMillis(100)).forEach { consumerRecord ->
                    totalt++
                    val record = consumerRecord.value()
                    val oppgave: Oppgave = objectMapper.readValue(record)
                    if (oppgave.tema == "SYK") relevante++ else irrelevante++
                    if (totalt % 10_000 == 0) logger.info("$totalt oppgaver behandlet til nå")
                }
            }
        } catch (exception: Exception) {
            logger.error("Feilet under sniffing på oppgave_endret", exception)
        } finally {
            logger.info("""
                Alle meldinger lest inn, totalt $totalt. Av disse er $relevante relevante og vil trigge behandling i spesialist og $irrelevante er irrelevante."
            """.trimIndent())
            close()
        }
    }

    override fun close() {
        konsumerer = false
    }

}
