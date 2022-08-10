package no.nav.helse.sparkel.skjermetendret

import io.mockk.every
import io.mockk.mockk
import java.time.Duration
import java.time.LocalDateTime
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SkjermetConsumerTest {
    private val rapidsConnection = mockk<RapidsConnection>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<String, String>>(relaxed = true)
    private val testRapid = TestRapid()
    private val FNR = "20046913337"

    @Test
    fun `mottar melding på skjermet-rapid og legger ut melding på tbd-rapid`() {
        val consumer = SkjermetConsumer(rapidsConnection, kafkaConsumer, SkjermetEndretPubliserer(testRapid))

        val mutableRecords = mutableListOf<String?>("true", "false")
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                consumer.close()
                return@answers ConsumerRecords.empty()
            }

            mutableRecords.removeAt(0)
                ?.let {
                    val record = ConsumerRecord(SKJERMET_TOPIC, 0, 0, FNR, it)
                    ConsumerRecords(
                        mapOf(TopicPartition(SKJERMET_TOPIC, 0) to mutableListOf(record))
                    )
                } ?: ConsumerRecords.empty()
        }

        val localDateTimeFørKonsumering = LocalDateTime.now()
        consumer.run()

        assertEquals(2, testRapid.inspektør.size)

        val melding1 = testRapid.inspektør.message(0)
        assertEquals(FNR, melding1["fødselsnummer"].asText())
        assertTrue(melding1["skjermet"].asBoolean())
        assertEquals("innhent_skjermetinfo", melding1["@event_name"].asText())
        val opprettet = melding1["@opprettet"]::asLocalDateTime.invoke()
        assertTrue(opprettet.isAfter(localDateTimeFørKonsumering))
        assertTrue(opprettet.isBefore(LocalDateTime.now()))
        assertNotNull(melding1["@id"].asText())

        val melding2 = testRapid.inspektør.message(1)
        assertFalse(melding2["skjermet"].asBoolean())
    }
}
