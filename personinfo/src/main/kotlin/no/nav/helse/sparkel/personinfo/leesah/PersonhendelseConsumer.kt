package no.nav.helse.sparkel.personinfo.leesah

import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration

internal class PersonhendelseConsumer(
    private val rapidConnection: RapidsConnection,
    private val kafkaConsumer: KafkaConsumer<ByteArray, GenericRecord>,
    private val personhendelseRiver: PersonhendelseRiver
): AutoCloseable, Runnable {
    private val log = LoggerFactory.getLogger("personhendelse-konsumer")
    private var konsumerer = true

    override fun run() {
        log.info("PersonhendelseConsumer starter opp")
        try {
            while (konsumerer) {
                val records = kafkaConsumer.poll(Duration.ofMillis(100))
                records.forEach {
                    val record = it.value()
                    personhendelseRiver.onPackage(record)
                }
            }
        } catch (e: Exception) {
            log.error("Feilet under konsumering av personhendelse", e)
        } finally {
            close()
            rapidConnection.stop()
        }
    }

    override fun close() {
        konsumerer = false
    }
}
