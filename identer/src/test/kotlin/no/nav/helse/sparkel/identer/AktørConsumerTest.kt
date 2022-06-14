package no.nav.helse.sparkel.identer

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AktørConsumerTest {

    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<ByteArray, GenericRecord>>(relaxed = true)
    private val identhendelseHandler = mockk<IdenthendelseHandler>(relaxed = true)

    @Test
    fun `sender melding til videre håndtering`() {
        val aktørConsumer = AktørConsumer(rapidApplication, kafkaConsumer, identhendelseHandler)
        queueMessages(
            aktørConsumer,
            listOf(null, genericRecord())
        )
        aktørConsumer.run()

        val slot = slot<AktørV2>()
        verify(exactly = 1) {
            identhendelseHandler.håndterIdenthendelse(capture(slot))
        }
        assertTrue(slot.captured.identifikatorer.map { it.idnummer }.containsAll(listOf("123", "456")))
    }

    @Test
    fun `ignorerer melding uten folkeregisterident`() {
        val aktørConsumer = AktørConsumer(rapidApplication, kafkaConsumer, identhendelseHandler)
        queueMessages(
            aktørConsumer,
            listOf(genericRecord(type = Type.AKTORID))
        )
        aktørConsumer.run()

        verify(exactly = 0) {
            identhendelseHandler.håndterIdenthendelse(any())
        }
    }

    @Test
    fun `parser record`() {
        val aktørRecord = parseAktørMessage(genericRecord())
        assertEquals(2, aktørRecord.identifikatorer.size)

        val gjeldende = aktørRecord.identifikatorer.single { it.gjeldende }
        val historisk = aktørRecord.identifikatorer.single { !it.gjeldende }

        assertEquals("123", gjeldende.idnummer)
        assertEquals(Type.FOLKEREGISTERIDENT, gjeldende.type)
        assertEquals("456", historisk.idnummer)
    }

    private fun genericRecord(type: Type = Type.FOLKEREGISTERIDENT): GenericRecord =
        GenericData.Record(AktørAvroDeserializer.schema).apply {
            val foo = AktørAvroDeserializer.schema.getField("identifikatorer").schema().elementType
            val identer = listOf(
                GenericData.Record(foo).apply {
                    put("idnummer", "123")
                    put("type", GenericData.EnumSymbol(foo, type))
                    put("gjeldende", true)
                },
                GenericData.Record(foo).apply {
                    put("idnummer", "456")
                    put("type", GenericData.EnumSymbol(foo, type))
                    put("gjeldende", false)
                })
            put("identifikatorer", identer)
        }

    private fun queueMessages(consumer: AktørConsumer, records: List<GenericRecord?>) {
        val mutableRecords = records.toMutableList()
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                consumer.close()
                return@answers ConsumerRecords.empty()
            }
            mutableRecords
                .removeAt(0)
                .let {
                    val record = ConsumerRecord(PDL_AKTØR_TOPIC, 0, 0, byteArrayOf(), it)
                    ConsumerRecords(mapOf(TopicPartition(PDL_AKTØR_TOPIC, 0) to mutableListOf(record)))
                }
        }
    }
}