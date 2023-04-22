package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import no.nav.helse.sparkel.sykepengeperioder.dbting.*
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@TestInstance(Lifecycle.PER_CLASS)
internal class SykepengehistorikkløserTest : H2Database() {

    private lateinit var infotrygdService: InfotrygdService
    private val rapid = TestRapid()

    private val sisteSendtMelding get() = rapid.inspektør.message(rapid.inspektør.size.minus(1))

    @BeforeAll
    fun setup() {
        infotrygdService = InfotrygdService(
            PeriodeDAO(dataSource),
            UtbetalingDAO(dataSource),
            InntektDAO(dataSource),
            StatslønnDAO(dataSource),
            FeriepengeDAO(dataSource)
        )
        rapid.apply {
            Sykepengehistorikkløser(this, infotrygdService)
        }
    }

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        clear()
    }

    @Test
    fun `sender ikke tomme perioder`() {
        opprettPeriode(seq = 1)
        opprettPeriode(seq = 2)
        rapid.sendTestMessage(behov())
        val perioder = sisteSendtMelding.løsning()
        assertEquals(0, perioder.size)
    }

    @Test
    fun `ignorerer behov som er mer enn 30 minutter gamle`() {
        rapid.sendTestMessage(behov(opprettet = LocalDateTime.now().minusMinutes(35)))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `løser behov uten vedtaksperiodeId`() {
        opprettPeriode(seq = 1)
        opprettPeriode(seq = 2)
        val behov =
            """{"@id": "${UUID.randomUUID()}", "@opprettet":"${
                LocalDateTime.now().minusMinutes(1)
            }", "@behov":["${Sykepengehistorikkløser.behov}"], "${Sykepengehistorikkløser.behov}": { "historikkFom": "2017-01-01", "historikkTom": "2021-01-01"}, "fødselsnummer": "$fnr" }"""

        rapid.sendTestMessage(behov)

        val perioder = sisteSendtMelding.løsning()
        assertEquals(0, perioder.size)
    }

    @Test
    fun `mapper også ut inntekt og dagsats`() {
        opprettPeriode(
            seq = 1,
            utbetalinger = listOf(
                Utbetaling(5.september(2020), 25.september(2020), dagsats = 2176.0),
                Utbetaling(4.september(2020), 4.september(2020))
            ),
            inntekter = listOf(Inntekt(4.september(2020), lønn = 565700.0))
        )
        opprettPeriode(
            seq = 2,
            utbetalinger = listOf(
                Utbetaling(2.juni(2019), 20.juni(2019)),
                Utbetaling(17.mai(2019), 1.juni(2019)),
                Utbetaling(1.mai(2019), 16.mai(2019)),
                Utbetaling(15.april(2019), 30.april(2019)),
                Utbetaling(30.mars(2019), 14.april(2019)),
                Utbetaling(4.februar(2019), 29.mars(2019))
            ),
            inntekter = listOf(Inntekt(4.februar(2019), lønn = 507680.0))
        )
        rapid.sendTestMessage(behov())

        val perioder = sisteSendtMelding.løsning()

        assertEquals(2, perioder.size)
        assertEquals(2, perioder[0].utbetalteSykeperioder.size)
        assertEquals(6, perioder[1].utbetalteSykeperioder.size)
        assertEquals(1, perioder[0].inntektsopplysninger.size)
        assertEquals(1, perioder[1].inntektsopplysninger.size)

        assertSykeperiode(
            sykeperiode = perioder[0].utbetalteSykeperioder[1],
            fom = 5.september(2020),
            tom = 25.september(2020),
            grad = "100",
            orgnummer = orgnummer,
            dagsats = 2176.0
        )

        assertInntektsopplysninger(
            inntektsopplysninger = perioder[0].inntektsopplysninger,
            dato = 4.september(2020),
            inntektPerMåned = 565700 / 12,
            orgnummer = orgnummer
        )
        assertInntektsopplysninger(
            inntektsopplysninger = perioder[1].inntektsopplysninger,
            dato = 4.februar(2019),
            inntektPerMåned = 507680 / 12,
            orgnummer = orgnummer
        )
    }

    @Test
    fun `setter ikke statslønn hvis tidligere periode har statslønn`() {
        opprettPeriode(seq = 1, sykmeldtFom = 1.januar(2020), statslønn = 1000.0)
        opprettPeriode(seq = 2, sykmeldtFom = 1.februar(2020), statslønn = null)
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { utenStatslønn ->
            utenStatslønn.forEach { periode ->
                assertFalse(periode.statslønn)
            }
        }
    }

    @Test
    fun `setter statslønn hvis nyeste periode har statslønn`() {
        opprettPeriode(seq = 1, utbetalinger = listOf(Utbetaling(1.januar(2020), 31.januar(2020))), statslønn = null)
        opprettPeriode(seq = 2, utbetalinger = listOf(Utbetaling(1.februar(2020), 28.februar(2020))), statslønn = 1000.0)
        rapid.sendTestMessage(behov())

        sisteSendtMelding.løsning().let { medStatlønn ->
            assertTrue(medStatlønn[0].statslønn)
            assertFalse(medStatlønn[1].statslønn)
        }
    }

    @Test
    fun `setter arbeidsKategoriKode`() {
        opprettPeriode(utbetalinger = listOf(Utbetaling(1.januar, 31.januar)), arbeidskategori = "01")
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { løsning ->
            assertEquals("01", løsning.first().arbeidsKategoriKode)
        }
    }

    @Test
    fun `forskjellig  arbeidsKategoriKode`() {
        opprettPeriode(seq = 1, utbetalinger = listOf(Utbetaling(1.mars, 31.mars)), arbeidskategori = "01")
        opprettPeriode(seq = 2, utbetalinger = listOf(Utbetaling(1.januar, 31.januar)), arbeidskategori = "02")
        rapid.sendTestMessage(behov())
        sisteSendtMelding.løsning().let { løsning ->
            assertEquals("01", løsning.first().arbeidsKategoriKode)
            assertEquals("02", løsning.last().arbeidsKategoriKode)
        }
    }

    @Test
    fun `Svarer på behov selv om det ikke finnes historikk`() {
        rapid.sendTestMessage(behov())

        assertTrue(sisteSendtMelding.has("@løsning"))
        assertTrue(sisteSendtMelding.path("@løsning").has(Sykepengehistorikkløser.behov))
        val løsninger = sisteSendtMelding.løsning()
        assertTrue(løsninger.isEmpty())
    }

    private fun JsonNode.løsning() =
        this.path("@løsning").path(Sykepengehistorikkløser.behov).map {
            Utbetalingshistorikk(it)
        }

    private class Utbetalingshistorikk(json: JsonNode) {

        val utbetalteSykeperioder = json["utbetalteSykeperioder"].map {
            UtbetalteSykeperiode(it)
        }
        val inntektsopplysninger = json["inntektsopplysninger"].map {
            Inntektsopplysning(it)
        }
        val statslønn = json["statslønn"].asBoolean()
        val arbeidsKategoriKode = json["arbeidsKategoriKode"].asText()

        class UtbetalteSykeperiode(json: JsonNode) {
            val fom = json["fom"].asLocalDate()
            val tom = json["tom"].asLocalDate()
            val utbetalingsGrad = json["utbetalingsGrad"].asText()
            val orgnummer = json["orgnummer"].asText()
            val dagsats = json["dagsats"].asDouble()
        }

        class Inntektsopplysning(json: JsonNode) {
            val sykepengerFom = json["sykepengerFom"].asLocalDate()
            val inntekt = json["inntekt"].asInt()
            val orgnummer = json["orgnummer"].asText()
        }

        private companion object {
            fun JsonNode.asLocalDate() = LocalDate.parse(this.asText())
        }
    }

    private fun assertSykeperiode(
        sykeperiode: Utbetalingshistorikk.UtbetalteSykeperiode,
        fom: LocalDate,
        tom: LocalDate,
        grad: String,
        orgnummer: String,
        dagsats: Double
    ) {
        assertEquals(fom, sykeperiode.fom)
        assertEquals(tom, sykeperiode.tom)
        assertEquals(grad, sykeperiode.utbetalingsGrad)
        assertEquals(orgnummer, sykeperiode.orgnummer)
        assertEquals(dagsats, sykeperiode.dagsats)
    }

    private fun assertInntektsopplysninger(
        inntektsopplysninger: List<Utbetalingshistorikk.Inntektsopplysning>,
        dato: LocalDate,
        inntektPerMåned: Int,
        orgnummer: String
    ) {
        assertEquals(dato, inntektsopplysninger[0].sykepengerFom)
        assertEquals(inntektPerMåned, inntektsopplysninger[0].inntekt)
        assertEquals(orgnummer, inntektsopplysninger[0].orgnummer)
    }

    @Language("Json")
    private fun behov(opprettet: LocalDateTime = LocalDateTime.now().minusMinutes(5)) =
        """
            {
            "@id": "${UUID.randomUUID()}", 
            "@opprettet":"$opprettet",
            "@behov":[
                "${Sykepengehistorikkløser.behov}"], 
                "${Sykepengehistorikkløser.behov}": { 
                    "historikkFom": "2017-01-01", 
                    "historikkTom": "2021-01-01"
                }, 
                "fødselsnummer": "$fnr", 
                "vedtaksperiodeId": "id"
            }
        """
}
