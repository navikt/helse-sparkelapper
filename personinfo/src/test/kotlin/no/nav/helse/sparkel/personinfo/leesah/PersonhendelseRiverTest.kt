package no.nav.helse.sparkel.personinfo.leesah

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.mockk
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.personinfo.PdlClient
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class PersonhendelseRiverTest {
    private val pdlClient = mockk<PdlClient>()
    private val testRapid = TestRapid()
    private val personhendelseRiver = PersonhendelseRiver(testRapid, pdlClient)
    private val logCollector = ListAppender<ILoggingEvent>()

    init {
        (LoggerFactory.getLogger("tjenestekall") as Logger).addAppender(logCollector)
        logCollector.start()
    }

    @BeforeEach
    fun setUp() {
        logCollector.list.clear()
    }

    @Test
    fun `logger kun informasjon om endring av adressebeskyttelse`() {
        personhendelseRiver.onPackage(nyttDokument(
            "20046913337",
            gradering = PersonhendelseOversetter.Gradering.FORTROLIG
        ))
        assertEquals(listOf("mottok endring på adressebeskyttelse"), logCollector.list.map(ILoggingEvent::getMessage) )
        logCollector.list.forEach { assertNull(it.argumentArray) }
    }

}
