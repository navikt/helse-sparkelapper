package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.UUID

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

    private fun behovMelding(behovType: String, løsning: String? = null): String =
        objectMapper.valueToTree<JsonNode>(
            mapOf(
                "@id" to UUID.randomUUID(),
                "@behov" to listOf(behovType),
                "@løsning" to løsning
            )
        ).toString()

    @Test
    fun `logger ved gyldig behov`() {
        testRapid.sendTestMessage(behovMelding("Arbeidsgiveropplysninger"))
        assertEquals(1, logCollector.list.size)
        assertTrue(logCollector.list.any { it.message.contains("Mottok Arbeidsgiveropplysninger-behov fra spleis") })
        assertEquals(1, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Mottok Arbeidsgiveropplysninger-behov fra spleis med data") })
    }

    @Test
    fun `ignorerer løste behov`() {
        testRapid.sendTestMessage(behovMelding("Arbeidsgiveropplysninger", "Test Løsning"))
        assertEquals(0, logCollector.list.size)
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Test
    fun `logger ikke ved ugyldig behov`() {
        testRapid.sendTestMessage(behovMelding("Tullebehov"))
        assertEquals(0, logCollector.list.size)
        assertEquals(0, sikkerlogCollector.list.size)
    }
}
