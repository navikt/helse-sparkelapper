package no.nav.helse.sparkel.personinfo.leesah

import no.nav.helse.rapids_rivers.RapidsConnection
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.LoggerFactory
import java.time.Duration
import org.apache.kafka.clients.consumer.ConsumerRecord

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
                    try {
                        if (meldingSkalIgnoreres(it)) {
                            log.info("Ignorerer melding i dev som mangler personidenter")
                            return@forEach
                        }
                    } catch (e: Exception) {
                        throw RuntimeException("Noe gikk galt ifm. spesialhåndtering av dårlige data i dev", e)
                    }

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

    private fun meldingSkalIgnoreres(record: ConsumerRecord<ByteArray, GenericRecord>) =
        ("dev-fss" == System.getenv("NAIS_CLUSTER_NAME")
                && (record.value().get("personidenter") as List<Any?>).isEmpty()
                && record.timestamp() >= 1668188207619
                && record.timestamp() <= 1668456606000)

    override fun close() {
        konsumerer = false
    }
}
