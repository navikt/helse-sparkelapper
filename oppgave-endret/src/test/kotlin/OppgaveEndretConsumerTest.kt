import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.oppgaveendret.OppgaveEndretConsumer
import no.nav.helse.sparkel.oppgaveendret.OppgaveEndretProducer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class OppgaveEndretConsumerTest {
    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<String, String>>(relaxed = true)

    @Test
    fun `happy case`() {
        val oppgaveEndretProducer = OppgaveEndretProducer(rapidApplication)
        val oppgaveEndretConsumer = OppgaveEndretConsumer(rapidApplication, kafkaConsumer, oppgaveEndretProducer)
        queueMessages(
            oppgaveEndretConsumer,
            listOf(null, null)
        )
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }
    }

    @Test
    fun `kaller close på rapidapplication når vi får en exception`() {
        val oppgaveEndretProducer = OppgaveEndretProducer(rapidApplication)
        val oppgaveEndretConsumer = OppgaveEndretConsumer(rapidApplication, kafkaConsumer, oppgaveEndretProducer)
        every { kafkaConsumer.poll(any<Duration>()) } throws IOException()
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }

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
}