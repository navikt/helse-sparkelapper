package no.nav.helse.sparkel.oppgaveendret.oppgave

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
import no.nav.helse.sparkel.oppgaveendret.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class OppgaveEndretConsumerTest {
    private val rapidApplication = mockk<RapidApplication>(relaxed = true)
    private val kafkaConsumer = mockk<KafkaConsumer<String, String>>()
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun `happy case`() {
        val gosysOppgaveEndretProducer = mockk<GosysOppgaveEndretProducer>(relaxed = true)
        val manipulerbarKlokke = MutableClock(fixedClock(time = 6, minutt = 15).instant())
        val oppgaveEndretConsumer =
            OppgaveEndretConsumer(
                rapidApplication,
                kafkaConsumer,
                gosysOppgaveEndretProducer,
                objectMapper,
                manipulerbarKlokke,
            )

        assertTrue(oppgaveEndretConsumer.åpentVindu())

        logger.info("Køer opp noen testmeldinger")
        queueMessages(
            oppgaveEndretConsumer,
            listOf("""{"tema": "SYK"}""", """{"tema": "SYK"}"""),
        )

        logger.info("Starter consumeren")
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            oppgaveEndretConsumer.run()
        }

        // Vent til alle records er behandlet
        logger.info("Venter til minst to meldinger er behandlet")
        verify(atLeast = 2, timeout = Duration.ofSeconds(2).toMillis()) { kafkaConsumer.poll(any<Duration>()) }

        while (oppgaveEndretConsumer.åpentVindu()) logger.info("venter på at vinduet skal lukkes")

        verify(atLeast = 2) { gosysOppgaveEndretProducer.onPacket(any()) }
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

        queueMessages(
            oppgaveEndretConsumer,
            listOf("""{"tema": "SYK"}"""),
        )

        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            oppgaveEndretConsumer.run()
        }
        while (manipulerbarKlokke.count == 0) { Thread.sleep(100)}
        verify(exactly = 0) { kafkaConsumer.poll(any<Duration>()) }

        manipulerbarKlokke.instant = fixedClock(time = 6, minutt = 16).instant()

        // Må starte run på nytt pga den forrige har tenkt å sove i fem minutter
        scope.launch {
            oppgaveEndretConsumer.run()
        }
        while (manipulerbarKlokke.count == 1) { Thread.sleep(100)}
        verify(atLeast = 1) { kafkaConsumer.poll(any<Duration>()) }
    }

    private fun queueMessages(consumer: OppgaveEndretConsumer, records: List<String?>) {
        val mutableRecords = records.toMutableList()
        logger.info("setter opp mock for kafka-consumer")
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                logger.info("ingen (flere) records å sende, lukker kafka-consumeren")
                consumer.close()
                return@answers ConsumerRecords.empty()
            }
            mutableRecords
                .removeAt(0)
                ?.let {
                    val record = ConsumerRecord("Leesah", 0, 0, "", it)
                    logger.info("sender en record til kafka-consumeren")
                    ConsumerRecords(
                        mapOf(TopicPartition("Leesah", 0) to mutableListOf(record))
                    )
                } ?: ConsumerRecords.empty()
        }
    }

    private fun fixedClock(time: Int, minutt: Int) = Clock.fixed(
        LocalDateTime.now()
            .with(ChronoField.HOUR_OF_DAY, time.toLong())
            .with(ChronoField.MINUTE_OF_HOUR, minutt.toLong())
            .toInstant(ZoneId.systemDefault().rules.getOffset(now())),
        ZoneId.systemDefault()
    )
}

class MutableClock(var instant: Instant) : Clock() {
    var count: Int = 0
    override fun instant() = instant.also {
        count++
    }

    override fun getZone(): ZoneId = throw UnsupportedOperationException()
    override fun withZone(zoneId: ZoneId): Clock = throw UnsupportedOperationException()
}
