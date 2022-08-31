package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import net.logstash.logback.argument.StructuredArguments.keyValue
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.GosysOppgaveEndretProducer
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

internal class OppgaveEndretConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val gosysOppgaveEndretProducer: GosysOppgaveEndretProducer,
    private val objectMapper: ObjectMapper,
    private val clock: Clock,
) : AutoCloseable, Runnable {
    private var konsumerer = true
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun run() {
        logger.info("OppgaveEndretConsumer starter opp")
        try {
            while (konsumerer) {
                // sorry my dudes
                if (!åpentVindu()) {
                    logger.info("Vinduet er lukket, sover i fem minutter.")
                    Thread.sleep(Duration.of(5, ChronoUnit.MINUTES).toMillis())
                    continue
                }
                logger.info("Poller topic")
                kafkaConsumer.poll(Duration.ofMillis(100)).onEach { consumerRecord ->
                    val record = consumerRecord.value()
                    val oppgave: Oppgave = objectMapper.readValue(record)
                    if (oppgave.tema != "SYK") return
                    logger.info("Mottatt oppgave_endret med {}", keyValue("oppaveId", oppgave.id))
                    gosysOppgaveEndretProducer.onPacket(oppgave)
                }.also {
                    if (it.isEmpty) {
                        logger.info("Ingen flere records, lukker vinduet.")
                        vinduslukking = now()
                    }
                }
            }
        } catch (exception: Exception) {
            logger.error("Feilet under konsumering av oppgave_endret", exception)
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    private val vindusåpning = LocalTime.of(6, 15)
    private var vinduslukking = LocalTime.of(7, 15)

    /*
    Pga risiko for dobbeltutbetaling med Infotrygd sjekker vi bare endringer i Gosys-oppgaver etter at spleis har stått
    hele natten og oppfrisket historikk fra Infotrygd.

    Gosys-oppgaver kan lukkes som et resultat av utbetaling i Infotrygd, og per i dag klarer ikke spleis/spesialist å
    fange opp disse utbetalingene raskt nok. Når det er på plass kan dette vinduet fjernes.
     */
    internal fun åpentVindu() = now().let {
        vindusåpning < it && it < vinduslukking
    }

    private fun now() = clock.instant().atZone(ZoneId.systemDefault()).toLocalTime()

    override fun close() {
        logger.info("close er kalt, avslutter konsumering")
        konsumerer = false
    }

}
