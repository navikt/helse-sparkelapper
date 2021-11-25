package no.nav.helse.sparkel.personinfo.leesah

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.RapidApplication
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test
import java.io.IOException
import java.time.Duration

class PersonhendelseConsumerTest {
    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<ByteArray, GenericRecord>>(relaxed = true)
    private val personhendelseRiver = mockk<PersonhendelseRiver>(relaxed = true)

    @Test
    fun `happy case`() {
        val personhendelseConsumer = PersonhendelseConsumer(rapidApplication, kafkaConsumer, personhendelseRiver)
       queueMessages(
            personhendelseConsumer,
            listOf(null, null, mockk<GenericRecord>(relaxed = true))
        )
        personhendelseConsumer.run()
        verify(exactly = 1) { personhendelseRiver.onPackage(any()) }

    }

    @Test
    fun `kaller close på rapidapplication når vi får en exception`() {
        val personhendelseConsumer = PersonhendelseConsumer(rapidApplication, kafkaConsumer, personhendelseRiver)
        every { kafkaConsumer.poll(any<Duration>()) } throws IOException()
        personhendelseConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }

    }

    private fun queueMessages(consumer: PersonhendelseConsumer, records: List<GenericRecord?>) {
        val mutableRecords = records.toMutableList()
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                consumer.close()
                return@answers ConsumerRecords.empty()
            }
            mutableRecords
                .removeAt(0)
                ?.let {
                    val record = ConsumerRecord("Leesah", 0, 0, byteArrayOf(), it)
                    ConsumerRecords(
                        mapOf(TopicPartition("Leesah", 0) to mutableListOf(record))
                    )
                } ?: ConsumerRecords.empty()
        }
    }
}