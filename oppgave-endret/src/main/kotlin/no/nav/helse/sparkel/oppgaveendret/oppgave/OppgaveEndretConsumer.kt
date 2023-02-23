package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.oppgaveendret.GosysOppgaveEndretProducer
import no.nav.helse.sparkel.oppgaveendret.Hendelse
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

    private fun erDev() = "dev-gcp" == System.getenv("NAIS_CLUSTER_NAME")

    override fun run() {
        logger.info("OppgaveEndretConsumer starter opp")
        try {
            while (konsumerer) {
                // sorry my dudes
                if (!erDev() && !åpentVindu()) {
                    logger.info("Vinduet er lukket, sover i fem minutter.")
                    Thread.sleep(Duration.of(5, ChronoUnit.MINUTES).toMillis())
                    continue
                }
                logger.info("Poller topic")
                kafkaConsumer.poll(Duration.ofSeconds(5))
                    .apply {
                        val count = this.count()
                        if (count == 0) {
                            logger.info("Ingen flere oppgavemeldinger å lese, sender meldinger")
                            gosysOppgaveEndretProducer.shipIt()
                        } else logger.info("Oppgave-endret record count: {}", count)
                    }
                    .filter {
                        val hendelseJson = objectMapper.readTree(it.value())
                        Hendelse.fromJson(hendelseJson).erRelevant()
                    }
                    .mapNotNull {
                        val jsonNode = objectMapper.readTree(it.value())
                        Oppgave.fromJson(jsonNode)
                    }
                    .filter { it.erRelevant() }
                    .onEach { gosysOppgaveEndretProducer.onPacket(it) }
            }
        } catch (exception: Exception) {
            logger.error("Feilet under konsumering av oppgave_endret", exception)
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    private val vindusåpning = LocalTime.of(6, 15)
    private val vinduslukking = LocalTime.of(6, 21)

    /*
    Pga risiko for dobbeltutbetaling med Infotrygd sjekker vi bare endringer i Gosys-oppgaver etter at spleis har stått
    hele natten og oppfrisket historikk fra Infotrygd.

    Gosys-oppgaver kan lukkes som et resultat av utbetaling i Infotrygd, og per i dag klarer ikke spleis/spesialist å
    fange opp disse utbetalingene raskt nok. Når det er på plass kan dette vinduet fjernes.
     */
    internal fun åpentVindu(): Boolean {
        val nå = now()
        val åpent = vindusåpning < nå && nå < vinduslukking

        logger.info("Vinduet er åpent: $åpent (sjekket om $nå er etter $vindusåpning og før $vinduslukking)")
        return åpent
    }

    private fun now() = LocalTime.now(clock)

    override fun close() {
        logger.info("close er kalt, avslutter konsumering", RuntimeException("Stack trace for debugging-formål"))
        konsumerer = false
    }

}
