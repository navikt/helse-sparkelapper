import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import java.time.Duration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.sparkel.oppgaveendret.OppgaveEndretConsumer
import no.nav.helse.sparkel.oppgaveendret.GosysOppgaveSykEndretProducer
import no.nav.helse.sparkel.oppgaveendret.pdl.PdlClient
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Test

class OppgaveEndretConsumerTest {
    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<String, String>>(relaxed = true)
    private val pdlClient = mockk<PdlClient>()

    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerKotlinModule()
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun `happy case`() {
        val gosysOppgaveSykEndretProducer = GosysOppgaveSykEndretProducer(rapidApplication, pdlClient)
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(rapidApplication, kafkaConsumer, gosysOppgaveSykEndretProducer, objectMapper)
        queueMessages(
            oppgaveEndretConsumer,
            listOf(null, null)
        )
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }
    }

    @Test
    fun `kaller close på rapidapplication når vi får en exception`() {
        val gosysOppgaveSykEndretProducer = GosysOppgaveSykEndretProducer(rapidApplication, pdlClient)
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(rapidApplication, kafkaConsumer, gosysOppgaveSykEndretProducer, objectMapper)
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