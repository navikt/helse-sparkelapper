package no.nav.helse.sparkel.personinfo.leesah

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.every
import io.mockk.mockk
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.personinfo.PdlClient
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.adressebeskyttelseV1
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.serialize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.util.*
import no.nav.helse.sparkel.personinfo.FantIkkeIdenter
import no.nav.helse.sparkel.personinfo.Identer
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.dødsfallV1
import no.nav.helse.sparkel.personinfo.leesah.PersonhendelseFactory.folkeregisteridentifikatorV1

class PersonhendelseRiverTest {
    private val FNR = "20046913337"
    private val AKTØRID = "1234567890123"
    private val pdlClient = mockk<PdlClient>()
    private val testRapid = TestRapid()
    private val personhendelseRiver = PersonhendelseRiver(
        rapidsConnection = testRapid,
        pdlClient = pdlClient,
        cacheTimeout = Duration.ofMillis(500)
    )
    private val logCollector = ListAppender<ILoggingEvent>()

    init {
        (LoggerFactory.getLogger("tjenestekall") as Logger).addAppender(logCollector)
        logCollector.start()
    }

    @BeforeEach
    fun setUp() {
        logCollector.list.clear()
        testRapid.reset()
    }

    @Test
    fun `håndterer dødsfall`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        val dødsdato = LocalDate.of(2018, 1, 1)
        personhendelseRiver.onPackage(dødsfallV1(FNR, dødsdato))
        assertEquals(1, testRapid.inspektør.size)
        val melding = testRapid.inspektør.message(0)
        assertEquals(FNR, melding["fødselsnummer"].asText())
        assertEquals(AKTØRID, melding["aktørId"].asText())
        assertEquals("dødsmelding", melding["@event_name"].asText())
        assertEquals("$dødsdato", melding["dødsdato"].asText())
        assertDoesNotThrow(melding["@opprettet"]::asLocalDateTime)
        assertDoesNotThrow { UUID.fromString(melding["@id"].asText()) }
    }

    @Test
    fun `håndterer endring av ident`() {
        val nyttFnr = "11111111111"
        val nyAktørId = "2222222222222"
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = nyttFnr,
            aktørId = nyAktørId
        )
        personhendelseRiver.onPackage(folkeregisteridentifikatorV1(FNR))
        assertEquals(1, testRapid.inspektør.size)
        val melding = testRapid.inspektør.message(0)
        assertEquals(FNR, melding["fødselsnummer"].asText())
        assertEquals(nyAktørId, melding["aktørId"].asText())
        assertEquals(nyttFnr, melding["nye_identer"].path("fødselsnummer").asText())
        assertEquals(nyAktørId, melding["nye_identer"].path("aktørId").asText())
        assertEquals("ident_opphørt", melding["@event_name"].asText())
        assertDoesNotThrow(melding["@opprettet"]::asLocalDateTime)
        assertDoesNotThrow { UUID.fromString(melding["@id"].asText()) }
    }

    @Test
    fun `logger kun informasjon om endring av adressebeskyttelse`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        personhendelseRiver.onPackage(
            adressebeskyttelseV1(
                FNR,
                gradering = PersonhendelseOversetter.Gradering.FORTROLIG
            )
        )
        assertEquals(listOf("mottok endring på adressebeskyttelse"), logCollector.list.map(ILoggingEvent::getMessage))
        logCollector.list.forEach { assertNull(it.argumentArray) }
    }

    @Test
    fun `slår opp i pdl og legger adressebeskyttelse_endret på rapid`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        personhendelseRiver.onPackage(
            adressebeskyttelseV1(
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

    @Test
    fun `klarer å lese en avro melding som er deserialisert`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )

        val dokument = adressebeskyttelseV1(FNR, PersonhendelseOversetter.Gradering.FORTROLIG)
        val deserialisertDokument = PersonhendelseAvroDeserializer().deserialize("leesah", serialize(dokument))

        personhendelseRiver.onPackage(deserialisertDokument)
        assertEquals(1, testRapid.inspektør.size)
    }

    @Test
    fun `throttler meldinger fra PDL på samme ident som kommer inn tilnærmet samtidig`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        repeat(5) {
            personhendelseRiver.onPackage(
                adressebeskyttelseV1(
                    FNR,
                    gradering = PersonhendelseOversetter.Gradering.FORTROLIG
                )
            )
        }
        assertEquals(1, testRapid.inspektør.size)
    }

    @Test
    fun `throttler ikke når cacheTimeout er utløpt`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns Identer(
            fødselsnummer = FNR,
            aktørId = AKTØRID
        )
        repeat(5) {
            personhendelseRiver.onPackage(
                adressebeskyttelseV1(
                    FNR,
                    gradering = PersonhendelseOversetter.Gradering.FORTROLIG
                )
            )
        }
        Thread.sleep(600)
        personhendelseRiver.onPackage(
            adressebeskyttelseV1(
                FNR,
                gradering = PersonhendelseOversetter.Gradering.FORTROLIG
            )
        )
        assertEquals(2, testRapid.inspektør.size)
    }

    /**
     * Vi regner med at vi ikke trenger å vite om adressebeskyttelse til personer uten fnr (eller dnr),
     * da disse uansett ikke kan opprette sak i Spleis.
     *
     * I fremtiden vil vi måtte støtte disse både i Spleis og i resten av stacken.
     */
    @Test
    fun `Foreløpig støtter vi ikke personer som ikke har fnr`() {
        every { pdlClient.hentIdenter(FNR, any()) } returns FantIkkeIdenter()
        personhendelseRiver.onPackage(
            adressebeskyttelseV1(
                FNR,
                gradering = PersonhendelseOversetter.Gradering.FORTROLIG
            )
        )
        assertEquals(listOf("mottok endring på adressebeskyttelse", "Kan ikke registrere addressebeskyttelse-endring på $FNR pga manglende fnr"), logCollector.list.map(ILoggingEvent::getMessage))
        assertEquals(0, testRapid.inspektør.size)
    }

}
