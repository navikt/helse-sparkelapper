package no.nav.helse.sparkel.arbeidsgiver

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
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
        testRapid.sendTestMessage(forkastetVedtaksperiode(LocalDate.MIN, LocalDate.MIN.plusDays(15), eventName = "tull"))
        assertEquals(0, sikkerlogCollector.list.size)
    }

    @Language("JSON")
    private fun forkastetVedtaksperiode(fom: LocalDate, tom: LocalDate, eventName: String = "vedtaksperiode_forkastet") = """
    {
        "@event_name": "$eventName",
        "vedtaksperiodeId": "${UUID.randomUUID()}",
        "tilstand": "START",
        "fom": "$fom",
        "tom": "$tom",
        "forlengerSpleisEllerInfotrygd": true,
        "fødselsnummer": "fnr"
    }
    """
}
