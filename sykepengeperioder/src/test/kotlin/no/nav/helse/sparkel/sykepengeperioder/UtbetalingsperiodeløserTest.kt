package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate
import java.util.UUID
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.infotrygd.PeriodeDAO
import no.nav.helse.sparkel.infotrygd.UtbetalingDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UtbetalingsperiodeløserTest : H2Database() {

    private lateinit var service: InfotrygdService
    private val rapid = TestRapid()
    private val sendtMelding get() = rapid.inspektør.let {
        it.message(it.size - 1)
    }

    @BeforeAll
    fun setup() {
        service = InfotrygdService(
            PeriodeDAO { dataSource },
            UtbetalingDAO { dataSource },
            InntektDAO { dataSource },
            StatslønnDAO { dataSource },
            FeriepengeDAO { dataSource }
        )
    }

    @BeforeEach
    internal fun beforeEach() {
        clear()
        rapid.reset()
    }

    @Test
    fun `løser enkelt behov`() {
        opprettPeriode(utbetalinger = listOf(Utbetaling(1.januar(2020), 31.januar(2020))))
        testBehov(enkeltBehov())

        assertFalse(sendtMelding.isEmpty)
        val perioder = sendtMelding.løsning()

        assertEquals(1, perioder.size)
    }

    @Test
    fun `løser behov med flere behov-nøkler`() {
        opprettPeriode(utbetalinger = listOf(Utbetaling(1.januar(2020), 31.januar(2020))))
        testBehov(behovMedFlereBehovsnøkler())

        assertFalse(sendtMelding.isEmpty)
        val perioder = sendtMelding.løsning()

        assertEquals(1, perioder.size)
    }

    @Test
    fun `mapper også ut perioder`() {
        opprettPeriode(utbetalinger = listOf(Utbetaling(1.januar(2020), 31.januar(2020))))
        testBehov(enkeltBehov())

        val løsninger = sendtMelding.løsning()
        assertEquals(1, løsninger.size)

        val periode = løsninger.first()

        assertInfotrygdperiode(
            periode = periode,
            fom = 1.januar(2020),
            tom = 31.januar(2020),
            grad = "100",
            dagsats = 1000.0,
            typetekst = "ArbRef",
            organisasjonsnummer = orgnummer,
            arbeidsKategoriKode = "01"
        )
    }

    @Test
    fun `Svarer på behov selv om det ikke finnes historikk`() {
        testBehov(enkeltBehov())

        assertTrue(sendtMelding.has("@løsning"))
        assertTrue(sendtMelding.path("@løsning").has(Utbetalingsperiodeløser.behov))
        val løsninger = sendtMelding.løsning()
        assertTrue(løsninger.isEmpty())
    }

    private fun JsonNode.løsning() = this.path("@løsning").path(Utbetalingsperiodeløser.behov).map {
        Infotrygdperiode(it)
    }

    class Infotrygdperiode(json: JsonNode) {
        val fom = json["fom"].asLocalDate()
        val tom = json["tom"].asLocalDate()
        val grad = json["grad"].asText()
        val dagsats = json["dagsats"].asDouble()
        val typetekst = json["typetekst"].asText()
        val organisasjonsnummer = json["organisasjonsnummer"].asText()
        val arbeidsKategoriKode = json["arbeidsKategoriKode"].asText()

        private companion object {
            fun JsonNode.asLocalDate() = LocalDate.parse(this.asText())
        }
    }

    private fun testBehov(behov: String) {
        Utbetalingsperiodeløser(rapid, service)
        rapid.sendTestMessage(behov)
    }

    private fun assertInfotrygdperiode(
        periode: Infotrygdperiode,
        fom: LocalDate,
        tom: LocalDate, grad: String,
        dagsats: Double,
        typetekst: String,
        organisasjonsnummer: String,
        arbeidsKategoriKode: String,
    ) {
        assertEquals(fom, periode.fom)
        assertEquals(tom, periode.tom)
        assertEquals(grad, periode.grad)
        assertEquals(dagsats, periode.dagsats)
        assertEquals(typetekst, periode.typetekst)
        assertEquals(organisasjonsnummer, periode.organisasjonsnummer)
        assertEquals(arbeidsKategoriKode, periode.arbeidsKategoriKode)
    }

    private fun behovMedFlereBehovsnøkler() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentEnhet", "HentPersoninfo", "HentInfotrygdutbetalinger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18T18:56:47.339159",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "$fnr",
            "orgnummer" : "$orgnummer",
            "HentInfotrygdutbetalinger": {
                "historikkFom" : "2017-05-18",
                "historikkTom" : "2020-05-18"
            }
        }
        """.trimIndent()

    private fun enkeltBehov() =
        """
        {
            "@event_name" : "behov",
            "@behov" : [ "HentInfotrygdutbetalinger" ],
            "@id" : "${UUID.randomUUID()}",
            "@opprettet" : "2020-05-18T18:56:47.339159",
            "hendelseId" : "hendelseId",
            "vedtaksperiodeId" : "vedtaksperiodeId",
            "fødselsnummer" : "$fnr",
            "orgnummer" : "$orgnummer",
            "HentInfotrygdutbetalinger": {
                "historikkFom" : "2017-05-18",
                "historikkTom" : "2020-05-18"
            }
        }
        """.trimIndent()
}
