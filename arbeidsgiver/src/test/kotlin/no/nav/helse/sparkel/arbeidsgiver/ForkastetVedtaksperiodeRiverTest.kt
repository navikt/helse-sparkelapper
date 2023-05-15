package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

internal class ForkastetVedtaksperiodeRiverTest {

    private val testRapid = TestRapid()
    private val sikkerlogCollector = ListAppender<ILoggingEvent>()

    init {
        VedtaksperiodeForkastetRiver(testRapid)
        (LoggerFactory.getLogger("tjenestekall") as Logger).addAppender(sikkerlogCollector)
        sikkerlogCollector.start()
    }

    @BeforeEach
    fun beforeEach() {
        sikkerlogCollector.list.clear()
    }

    @Test
    fun `ignorerer andre eventer`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                LocalDate.MIN,
                LocalDate.MIN.plusDays(15),
                eventName = "tull",
                tilstand = "START"
            )
        )
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Test
    fun `logger periode med trengerArbeidsgiveropplysninger=true og som er forkastet i tilstand START`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                LocalDate.MIN,
                LocalDate.MIN.plusDays(30),
                tilstand = "START"
            )
        )
        assertEquals(1, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Fant en forkastet periode som trenger forespørsel.") })
    }

    @Test
    fun `logger periode med trengerArbeidsgiveropplysninger=true og som er forkastet i tilstand AVVENTER_INFOTRYGDHISTORIKK`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                LocalDate.MIN,
                LocalDate.MIN.plusDays(16),
                tilstand = "AVVENTER_INFOTRYGDHISTORIKK"
            )
        )
        assertEquals(1, sikkerlogCollector.list.size)
        assertTrue(sikkerlogCollector.list.any { it.message.contains("Fant en forkastet periode som trenger forespørsel.") })
    }

    @Test
    fun `logger ikke periode som er forkastet i annen tilstand enn AVVENTER_INFOTRYGDHISTORIKK eller START`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                LocalDate.MIN,
                LocalDate.MIN.plusDays(16),
                tilstand = "tulletilstand"
            )
        )
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Test
    fun `logger ikke periode med trengerArbeidsgiveropplysninger=false`() {
        testRapid.sendTestMessage(
            forkastetVedtaksperiode(
                fom = LocalDate.MIN,
                tom = LocalDate.MIN.plusDays(16),
                tilstand = "START",
                trengerArbeidsgiveropplysninger = false
            )
        )
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Language("JSON")
    private fun forkastetVedtaksperiode(
        fom: LocalDate,
        tom: LocalDate,
        eventName: String = "vedtaksperiode_forkastet",
        tilstand: String,
        trengerArbeidsgiveropplysninger: Boolean = true
    ) = """
    {
        "@event_name": "$eventName",
        "vedtaksperiodeId": "${UUID.randomUUID()}",
        "tilstand": "$tilstand",
        "fom": "$fom",
        "tom": "$tom",
        "trengerArbeidsgiveropplysninger": $trengerArbeidsgiveropplysninger,
        "fødselsnummer": "fnr"
    }
    """
}
