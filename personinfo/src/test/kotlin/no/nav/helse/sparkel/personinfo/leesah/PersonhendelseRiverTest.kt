package no.nav.helse.sparkel.personinfo.leesah

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.personinfo.PdlClient
import no.nav.helse.sparkel.personinfo.PdlOversetter
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.nyttDokument
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.LoggerFactory
import java.util.*

class PersonhendelseRiverTest {
    private val FNR = "20046913337"
    private val AKTØRID = "1234567890123"
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
        every { pdlClient.hentIdenter(FNR, any()) } returns PdlOversetter.Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        personhendelseRiver.onPackage(
            nyttDokument(
                FNR,
                gradering = PersonhendelseOversetter.Gradering.FORTROLIG
            )
        )
        assertEquals(listOf("mottok endring på adressebeskyttelse"), logCollector.list.map(ILoggingEvent::getMessage))
        logCollector.list.forEach { assertNull(it.argumentArray) }
    }

    @Test
    fun `slår opp i pdl og legger event på rapid`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns PdlOversetter.Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        personhendelseRiver.onPackage(
            nyttDokument(
                FNR,
                gradering = PersonhendelseOversetter.Gradering.FORTROLIG
            )
        )
        assertEquals(1, testRapid.inspektør.size)
        val melding = testRapid.inspektør.message(0)
        assertEquals(FNR, melding["fødselsnummer"].asText())
        assertEquals(AKTØRID, melding["aktørId"].asText())
        assertEquals("adressebeskyttelse_endret", melding["@event_name"].asText())
        assertDoesNotThrow(melding["@opprettet"]::asLocalDateTime)
        assertDoesNotThrow { UUID.fromString(melding["@id"].asText()) }
    }

}
