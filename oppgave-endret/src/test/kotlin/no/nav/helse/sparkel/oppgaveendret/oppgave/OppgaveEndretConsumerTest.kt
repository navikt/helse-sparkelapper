package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.oppgaveendret.GosysOppgaveEndretProducer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class OppgaveEndretConsumerTest {
    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<String, String>>(relaxed = true)

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun `happy case`() {
        val gosysOppgaveEndretProducer = GosysOppgaveEndretProducer(rapidApplication)
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(rapidApplication, kafkaConsumer, gosysOppgaveEndretProducer, objectMapper)
        queueMessages(
            oppgaveEndretConsumer,
            listOf(null, null)
        )
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }
    }

    @Test
    fun `kaller close på rapidapplication når vi får en exception`() {
        val gosysOppgaveEndretProducer = GosysOppgaveEndretProducer(rapidApplication)
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(rapidApplication, kafkaConsumer, gosysOppgaveEndretProducer, objectMapper)
        every { kafkaConsumer.poll(any<Duration>()) } throws IOException()
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }

    }

    @Test
    fun `Verifiser at objectMapper mapper correct fra consumer record`() {
        val GOSYS = "FS22"
        val TEMASYK = "SYK"

        val record = "oppgave-endret-record.json".loadFromResources()
        val oppgave: Oppgave = objectMapper.readValue(record)

        assertEquals(34333333, oppgave.id)
        assertEquals(IdentType.AKTOERID, oppgave.ident!!.identType)
        assertEquals(GOSYS, oppgave.behandlesAvApplikasjon)
        assertEquals(TEMASYK, oppgave.tema)
        assertEquals("100001231333", oppgave.ident!!.verdi)
        assertEquals("21312434234", oppgave.ident!!.folkeregisterident)
    }

    private fun queueMessages(consumer: OppgaveEndretConsumer, records: List<String?>) {
        val mutableRecords = records.toMutableList()
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                consumer.close()
                return@answers ConsumerRecords.empty()
            }
            mutableRecords
                .removeAt(0)
                ?.let {
                    val record = ConsumerRecord("Leesah", 0, 0, "", it)
                    ConsumerRecords(
                        mapOf(TopicPartition("Leesah", 0) to mutableListOf(record))
                    )
                } ?: ConsumerRecords.empty()
        }
    }
    fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()
}