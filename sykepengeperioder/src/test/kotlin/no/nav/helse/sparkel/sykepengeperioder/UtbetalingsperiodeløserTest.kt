package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.sparkel.sykepengeperioder.dbting.InntektDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.PeriodeDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.StatslønnDAO
import no.nav.helse.sparkel.sykepengeperioder.dbting.UtbetalingDAO
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class UtbetalingsperiodeløserTest : H2Database() {

    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private var sendtMelding: JsonNode = objectMapper.createObjectNode()
    private lateinit var service: InfotrygdService

    private val rapid = object : RapidsConnection() {

        fun sendTestMessage(message: String) {
            listeners.forEach { it.onMessage(message, this) }
        }

        override fun publish(message: String) {
            sendtMelding = objectMapper.readTree(message)
        }

        override fun publish(key: String, message: String) {}

        override fun start() {}

        override fun stop() {}
    }

    @BeforeAll
    fun setup() {
        service = InfotrygdService(
            PeriodeDAO(dataSource),
            UtbetalingDAO(dataSource),
            InntektDAO(dataSource),
            StatslønnDAO(dataSource)
        )
    }

    @BeforeEach
    internal fun beforeEach() {
        clear()
        sendtMelding = objectMapper.createObjectNode()
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
            "@id" : "id",
            "@opprettet" : "2020-05-18T18:56:47.339159",
            "spleisBehovId" : "spleisBehovId",
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
            "@id" : "id",
            "@opprettet" : "2020-05-18T18:56:47.339159",
            "spleisBehovId" : "spleisBehovId",
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
