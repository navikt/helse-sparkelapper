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
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.Instant.now
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            OppgaveEndretConsumer(
                rapidApplication,
                kafkaConsumer,
                gosysOppgaveEndretProducer,
                objectMapper,
                fixedClock(time = 6, minutt = 15),
            )
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
            OppgaveEndretConsumer(
                rapidApplication,
                kafkaConsumer,
                gosysOppgaveEndretProducer,
                objectMapper,
                fixedClock(time = 6, minutt = 15),
            )
        every { kafkaConsumer.poll(any<Duration>()) } throws IOException()
        oppgaveEndretConsumer.run()
        verify(exactly = 1) { rapidApplication.stop() }
    }

    @Test
    fun `poller bare i gitt tidsrom`() {
        val gosysOppgaveEndretProducer = GosysOppgaveEndretProducer(rapidApplication)
        val manipulerbarKlokke = MutableClock(fixedClock(time = 6, minutt = 14).instant())
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(
                rapidApplication,
                kafkaConsumer,
                gosysOppgaveEndretProducer,
                objectMapper,
                manipulerbarKlokke,
            )

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            oppgaveEndretConsumer.run()
        }
        verify(exactly = 0) { kafkaConsumer.poll(any<Duration>()) }

        manipulerbarKlokke.instant = fixedClock(time = 6, minutt = 16).instant()

        // Må starte run på nytt pga den forrige er i dvale
        scope.launch {
            oppgaveEndretConsumer.run()
        }
        verify(atLeast = 1) { kafkaConsumer.poll(any<Duration>()) }
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

    private fun String.loadFromResources() = ClassLoader.getSystemResource(this).readText()

    private fun fixedClock(time: Int, minutt: Int) = Clock.fixed(
        LocalDateTime.now()
            .with(ChronoField.HOUR_OF_DAY, time.toLong())
            .with(ChronoField.MINUTE_OF_HOUR, minutt.toLong())
            .toInstant(ZoneId.systemDefault().rules.getOffset(now())),
        ZoneId.systemDefault()
    )
}

class MutableClock(var instant: Instant) : Clock() {
    override fun instant() = instant
    override fun getZone(): ZoneId = throw UnsupportedOperationException()
    override fun withZone(zoneId: ZoneId): Clock = throw UnsupportedOperationException() }
