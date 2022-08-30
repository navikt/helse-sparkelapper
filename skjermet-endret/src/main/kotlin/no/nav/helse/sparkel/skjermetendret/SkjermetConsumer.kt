package no.nav.helse.sparkel.skjermetendret

import java.time.Duration
import no.nav.helse.rapids_rivers.RapidsConnection
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory

internal class SkjermetConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<String, String>,
    private val skjermetEndretPubliserer: SkjermetEndretPubliserer,
    ) : AutoCloseable, Runnable {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val sikkerlogg = LoggerFactory.getLogger("tjenestekall")
    private var konsumerer = true

    override fun run() {
        log.info("SkjermetConsumer starter opp")
        try {
            while (konsumerer) {
                val records = kafkaConsumer.poll(Duration.ofSeconds(10))
                log.info("Pollet og mottok ${records.count()} meldinger.")
                records.forEach {
                    val fnr = it.key()
                    val maskedFnr = fnr.first() + "#".repeat(9) + fnr.last()
                    val skjermet = it.value().toBoolean()

                    sikkerlogg.info("Leste inn melding for skjermet-status for ${maskedFnr}: Skjermet=$skjermet")
                    skjermetEndretPubliserer.publiserSkjermetEndring(fnr, skjermet)
                }
            }
        } catch (exception: Exception) {
            sikkerlogg.error("Feilet under håndtering av skjermethendelse", exception)
            throw exception
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    override fun close() {
        konsumerer = false
    }
}
