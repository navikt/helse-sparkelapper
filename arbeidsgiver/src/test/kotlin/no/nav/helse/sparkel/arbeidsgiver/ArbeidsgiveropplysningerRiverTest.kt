package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.mockk
import io.mockk.verify
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class ArbeidsgiveropplysningerRiverTest {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModules(JavaTimeModule())
    private val testRapid = TestRapid()
    private val mockproducer: KafkaProducer<String, ArbeidsgiveropplysningerDTO> = mockk(relaxed = true)
    private val logCollector = ListAppender<ILoggingEvent>()
    private val sikkerlogCollector = ListAppender<ILoggingEvent>()

    init {
        ArbeidsgiveropplysningerRiver(testRapid, mockproducer)
        (LoggerFactory.getLogger(ArbeidsgiveropplysningerRiver::class.java) as Logger).addAppender(logCollector)
        logCollector.start()
        (LoggerFactory.getLogger("tjenestekall") as Logger).addAppender(sikkerlogCollector)
        sikkerlogCollector.start()
    }

    @BeforeEach
    fun beforeEach() {
        logCollector.list.clear()
        sikkerlogCollector.list.clear()
    }

    private fun eventMelding(eventName: String): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@event_name" to eventName,
                "@opprettet" to LocalDateTime.MAX,
                "fom" to LocalDate.MIN,
                "tom" to LocalDate.MAX,
                "organisasjonsnummer" to "4321",
                "fødselsnummer" to "123"
            )
        ).toString()

    @Test
    fun `logger ved gyldig event`() {
        testRapid.sendTestMessage(eventMelding("trenger_opplysninger_fra_arbeidsgiver"))
        assertEquals(2, logCollector.list.size)
        assertTrue(logCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis") })
        assertTrue(logCollector.list.any { it.message.contains("Publiserte forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger") })
        assertEquals(2, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis med data") })
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Publiserte forespørsel om arbeidsgiveropplyninger til helsearbeidsgiver-bro-sykepenger") })
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(eventMelding("Tullebehov"))
        assertEquals(0, logCollector.list.size)
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Test
    fun `publiserer forespørsel om arbeidsgiveropplysninger`() {
        testRapid.sendTestMessage(eventMelding("trenger_opplysninger_fra_arbeidsgiver"))

        val payload = ArbeidsgiveropplysningerDTO(
            type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
            organisasjonsnummer = "4321",
            fødselsnummer = "123",
            fom = LocalDate.MIN,
            tom = LocalDate.MAX,
            opprettet = LocalDateTime.MAX
        )
        verify(exactly = 1) {
            mockproducer.send(
                ProducerRecord(
                    "arbeidsgiveropplysninger",
                    null,
                    payload.fødselsnummer,
                    payload,
                    listOf(RecordHeader("type", payload.meldingstype))
                )
            )
        }
        assertEquals(1, testRapid.inspektør.size)
    }
}
