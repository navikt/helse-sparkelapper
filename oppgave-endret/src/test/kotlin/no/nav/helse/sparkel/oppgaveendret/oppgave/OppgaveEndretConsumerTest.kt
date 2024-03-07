package no.nav.helse.sparkel.oppgaveendret.oppgave

import com.fasterxml.jackson.databind.node.ObjectNode
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
import no.nav.helse.sparkel.oppgaveendret.Hendelsetype
import no.nav.helse.sparkel.oppgaveendret.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
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
                manipulerbarKlokke
            )

        assertTrue(oppgaveEndretConsumer.åpentVindu())

        logger.info("Køer opp noen testmeldinger")
        queueMessages(
            consumer = oppgaveEndretConsumer,
            records = List(7) { testJson.toString() },
            pollSize = 3
        )

        logger.info("Starter consumeren")
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            oppgaveEndretConsumer.run()
        }

        logger.info("Venter til meldingene er behandlet")
        verify(atLeast = 4, timeout = Duration.ofSeconds(2).toMillis()) { kafkaConsumer.poll(any<Duration>()) }
        verify(atLeast = 4, timeout = Duration.ofSeconds(5).toMillis()) { gosysOppgaveEndretProducer.onPacket(any()) }
        verify(exactly = 1) { gosysOppgaveEndretProducer.shipIt() }
    }

    @Test
    fun `ignorerer hendelser av typen OPPGAVE_ENDRET`() {
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

        val json = testJson.apply {
            this as ObjectNode
            val hendelseNode = this.path("hendelse") as ObjectNode
            hendelseNode.put("hendelsestype", Hendelsetype.OPPGAVE_ENDRET.name)
        }

        logger.info("Køer opp noen testmeldinger")
        queueMessages(
            consumer = oppgaveEndretConsumer,
            records = listOf(json.toString()),
            pollSize = 3
        )

        logger.info("Starter consumeren")
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            oppgaveEndretConsumer.run()
        }

        logger.info("Venter til meldingene er behandlet")
        verify(exactly = 0) { gosysOppgaveEndretProducer.onPacket(any()) }
        verify(exactly = 1) { gosysOppgaveEndretProducer.shipIt() }
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
            consumer = oppgaveEndretConsumer,
            records = listOf(testJson.toString()),
            pollSize = 5,
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

    private fun queueMessages(consumer: OppgaveEndretConsumer, records: List<String?>, pollSize: Int) {
        val mutableRecords = records.reversed().toMutableList()
        logger.info("setter opp mock for kafka-consumer")
        every { kafkaConsumer.poll(any<Duration>()) } answers {
            if (mutableRecords.isEmpty()) {
                logger.info("ingen (flere) records å sende, lukker kafka-consumeren")
                consumer.close()
                return@answers ConsumerRecords.empty()
            }
            val antallRecords = if (mutableRecords.size > pollSize) pollSize else mutableRecords.size
            val pollRecords = nesteRecords(mutableRecords, antallRecords)
            if (pollRecords.isNotEmpty()) {
                logger.info("sender $antallRecords record{} til kafka-consumeren", if (antallRecords > 1) "(s)" else "")
                ConsumerRecords(mapOf(TopicPartition("oppgave-endret", 0) to pollRecords))
            } else ConsumerRecords.empty()
        }
    }

    @Language("JSON")
    private val testJson = """
        {
            "hendelse": {
                "hendelsestype": "OPPGAVE_OPPRETTET"
            },
            "oppgave": {
                "oppgaveId": 123,
                "kategorisering": {
                    "tema": "SYK"
                },
                "bruker": {
                    "ident": "12345678911",
                    "identType": "FOLKEREGISTERIDENT"
                }
            }
        } 
        """.let { objectMapper.readTree(it) }

    private fun nesteRecords(
        mutableRecords: MutableList<String?>,
        pollRecordsSize: Int
    ): List<ConsumerRecord<String, String?>> {
        val pollResult = mutableRecords.subList(0, pollRecordsSize)
        return pollResult.map {
            ConsumerRecord("oppgave-endret", 0, 0, "", it)
        }.also { pollResult.clear() }
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

    override fun getZone(): ZoneId = ZoneId.systemDefault()
    override fun withZone(zoneId: ZoneId): Clock = throw UnsupportedOperationException()
}
