package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class ArbeidsgiveropplysningerRiverTest {
    private val objectMapper = jacksonObjectMapper()
    private val testRapid = TestRapid().also(::ArbeidsgiveropplysningerRiver)
    private val logCollector = ListAppender<ILoggingEvent>()
    private val sikkerlogCollector = ListAppender<ILoggingEvent>()

    init {
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
            )
        ).toString()

    @Test
    fun `logger ved gyldig event`() {
        testRapid.sendTestMessage(eventMelding("trenger_opplysninger_fra_arbeidsgiver"))
        assertEquals(1, logCollector.list.size)
        assertTrue(logCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis") })
        assertEquals(1, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Mottok trenger_opplysninger_fra_arbeidsgiver-event fra spleis med data") })
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(eventMelding("Tullebehov"))
        assertEquals(0, logCollector.list.size)
        assertEquals(0, sikkerlogCollector.list.size)
    }
}
