package no.nav.helse.sparkel.personinfo.leesah

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class PersonhendelseRiverTest {
    private val personhendelseRiver = PersonhendelseRiver()
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
    fun `logger ident for events som ikke er endring av adressebeskyttelse`() {
        personhendelseRiver.onPackage(nyttDokument(
            "20046913337",
            gradering = PersonhendelseOversetter.Gradering.STRENGT_FORTROLIG,
            opplysningstype = "Noe annet"
        ))
        assertEquals(listOf("Mottok event på ident [20046913337]"), logCollector.list.map(ILoggingEvent::getMessage))
    }

    @Test
    fun `logger hendelse om gradering er ugradert`() {
        personhendelseRiver.onPackage(nyttDokument(
            "20046913337",
            gradering = PersonhendelseOversetter.Gradering.UGRADERT
        ))
        assertEquals(listOf("mottok endring på adressebeskyttelse"), logCollector.list.map(ILoggingEvent::getMessage) )
    }

    @Test
    fun `logger ikke hendelse om gradering er noe annet enn ugradert`() {
        personhendelseRiver.onPackage(nyttDokument(
            "20046913337",
            gradering = PersonhendelseOversetter.Gradering.STRENGT_FORTROLIG
        ))
        assertEquals(0, logCollector.list.size)
    }

}
