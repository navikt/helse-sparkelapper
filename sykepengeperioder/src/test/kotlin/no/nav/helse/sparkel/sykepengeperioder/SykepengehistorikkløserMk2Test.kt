package no.nav.helse.sparkel.sykepengeperioder

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.helse.rapids_rivers.isMissingOrNull
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

@TestInstance(Lifecycle.PER_CLASS)
internal class SykepengehistorikkløserMk2Test : H2Database() {

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
            SykepengehistorikkløserMK2(this, infotrygdService)
        }
    }

    @BeforeEach
    fun beforeEach() {
        rapid.reset()
        clear()
    }

    @Test
    fun `løser behov`() {
        opprettPeriode(seq = 1)
        opprettPeriode(seq = 2)
        rapid.sendTestMessage(behov())
        val sykepengehistorikk = sisteSendtMelding.løsning()

        assertEquals(0, sykepengehistorikk.utbetalinger.size)
        assertTrue(sykepengehistorikk.arbeidskategorikoder.isEmpty)
        assertEquals(0, sykepengehistorikk.feriepengehistorikk.size)
        assertEquals(0, sykepengehistorikk.inntektshistorikk.size)
        assertFalse(sykepengehistorikk.harStatslønn)
        assertFalse(sykepengehistorikk.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `ignorerer behov som er mer enn 30 minutter gamle`() {
        rapid.sendTestMessage(behov(opprettet = LocalDateTime.now().minusMinutes(35)))
        assertEquals(0, rapid.inspektør.size)
    }

    @Test
    fun `mapper også ut inntekt og dagsats`() {
        opprettPeriode(
            seq = 1,
            utbetalinger = listOf(
                Utbetaling(5.september(2020), 25.september(2020), dagsats = 2176.0),
                Utbetaling(4.september(2020), 4.september(2020))
            ),
            inntekter = listOf(Inntekt(4.september(2020), lønn = 565700.0)),
            statslønn = null
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
            inntekter = listOf(Inntekt(4.februar(2019), lønn = 507680.0)),
            statslønn = null
        )
        opprettFeriepenger()

        rapid.sendTestMessage(behov())

        val løsning = sisteSendtMelding.løsning()

        assertEquals(8, løsning.utbetalinger.size)
        assertEquals(2, løsning.inntektshistorikk.size)
        assertEquals(1, løsning.arbeidskategorikoder.count())
        assertFalse(løsning.harStatslønn)
        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
        assertEquals(1, løsning.feriepengehistorikk.size)

        assertSykeperiode(
            sykeperiode = løsning.utbetalinger[0],
            fom = 5.september(2020),
            tom = 25.september(2020),
            grad = "100",
            orgnummer = orgnummer,
            dagsats = 2176.0
        )

        assertInntektsopplysninger(
            inntektsopplysning = løsning.inntektshistorikk[0],
            dato = 4.september(2020),
            inntektPerMåned = 565700 / 12,
            orgnummer = orgnummer
        )
        assertInntektsopplysninger(
            inntektsopplysning = løsning.inntektshistorikk[1],
            dato = 4.februar(2019),
            inntektPerMåned = 507680 / 12,
            orgnummer = orgnummer
        )
    }

    @Test
    fun `har statslønn i nyeste periode, men ikke eldste`() {
        opprettPeriode(seq = 1, statslønn = 1000.0)
        opprettPeriode(seq = 2, statslønn = null)

        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()
        assertTrue(løsning.harStatslønn)
    }

    @Test
    fun `har statslønn i gammel periode, men ikke nyeste`() {
        opprettPeriode(seq = 1, statslønn = null)
        opprettPeriode(seq = 2, statslønn = 1000.0)

        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()
        assertFalse(løsning.harStatslønn)
    }

    @Test
    fun `arbeidskategori`() {
        opprettPeriode(
            seq = 1,
            utbetalinger = listOf(
                Utbetaling(5.september(2020), 25.september(2020), dagsats = 2176.0),
                Utbetaling(4.september(2020), 4.september(2020))
            ),
            inntekter = listOf(Inntekt(4.september(2020), lønn = 565700.0)),
            arbeidskategori = "01"
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
            inntekter = listOf(Inntekt(4.februar(2019), lønn = 507680.0)),
            arbeidskategori = "00"
        )

        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()

        assertEquals(2, løsning.arbeidskategorikoder.count())
        assertEquals("2019-06-20", løsning.arbeidskategorikoder["00"].textValue())
        assertEquals("2020-09-25", løsning.arbeidskategorikoder["01"].textValue())
    }

    @Test
    fun `arbeidskategori ved tidligere periode med samme kode`() {
        opprettPeriode(
            seq = 1,
            utbetalinger = listOf(
                Utbetaling(5.september(2020), 25.september(2020), dagsats = 2176.0),
                Utbetaling(4.september(2020), 4.september(2020))
            ),
            inntekter = listOf(Inntekt(4.september(2020), lønn = 565700.0)),
            arbeidskategori = "01"
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
            inntekter = listOf(Inntekt(4.februar(2019), lønn = 507680.0)),
            arbeidskategori = "01"
        )

        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()

        assertEquals(1, løsning.arbeidskategorikoder.count())
        assertEquals("2020-09-25", løsning.arbeidskategorikoder["01"].textValue())
    }

    @Test
    fun `nullverdier for fom og tom`() {
        opprettPeriode(
            seq = 1,
            utbetalinger = listOf(Utbetaling()),
            inntekter = listOf(Inntekt(4.september(2020), lønn = 565700.0)),
            arbeidskategori = "01"
        )
        opprettPeriode(
            seq = 2,
            utbetalinger = listOf(Utbetaling()),
            inntekter = listOf(Inntekt(4.februar(2019), lønn = 507680.0)),
            arbeidskategori = "00"
        )

        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()
        assertEquals(0, løsning.arbeidskategorikoder.count())
        assertEquals(2, løsning.utbetalinger.size)
        assertEquals(2, løsning.inntektshistorikk.size)
    }

    @Test
    fun `henter feriepengehistorikk`() {
        opprettPeriode()
        opprettFeriepenger()
        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()

        assertEquals(1, løsning.feriepengehistorikk.size)
        assertEquals(orgnummer, løsning.feriepengehistorikk.first().orgnummer)
        assertEquals(1.mai(2020), løsning.feriepengehistorikk.first().fom)
        assertEquals(31.mai(2020), løsning.feriepengehistorikk.first().tom)
        assertEquals(1000.0, løsning.feriepengehistorikk.first().beløp)
        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `henter feriepengehistorikk med flere arbeidsgivere`() {
        opprettPeriode()
        opprettFeriepenger(
            beløp = listOf(1000.0, 2000.0),
            orgnumre = listOf("987654321", "654321987"))
        rapid.sendTestMessage(behov())
        val løsning = sisteSendtMelding.løsning()

        assertEquals(4, løsning.feriepengehistorikk.size)
        assertEquals("987654321", løsning.feriepengehistorikk[0].orgnummer)
        assertEquals(1.mai(2020), løsning.feriepengehistorikk[0].fom)
        assertEquals(31.mai(2020), løsning.feriepengehistorikk[0].tom)
        assertEquals(1000.0, løsning.feriepengehistorikk[0].beløp)
        assertEquals("987654321", løsning.feriepengehistorikk[1].orgnummer)
        assertEquals(1.mai(2020), løsning.feriepengehistorikk[1].fom)
        assertEquals(31.mai(2020), løsning.feriepengehistorikk[1].tom)
        assertEquals(2000.0, løsning.feriepengehistorikk[1].beløp)
        assertEquals("654321987", løsning.feriepengehistorikk[2].orgnummer)
        assertEquals(1.mai(2020), løsning.feriepengehistorikk[2].fom)
        assertEquals(31.mai(2020), løsning.feriepengehistorikk[2].tom)
        assertEquals(1000.0, løsning.feriepengehistorikk[2].beløp)
        assertEquals("654321987", løsning.feriepengehistorikk[3].orgnummer)
        assertEquals(1.mai(2020), løsning.feriepengehistorikk[3].fom)
        assertEquals(31.mai(2020), løsning.feriepengehistorikk[3].tom)
        assertEquals(2000.0, løsning.feriepengehistorikk[3].beløp)
        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal behandles manuelt`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad()
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertEquals(1, løsning.feriepengehistorikk.size)
        assertEquals(orgnummer, løsning.feriepengehistorikk.first().orgnummer)
        assertEquals(1.mai(2021), løsning.feriepengehistorikk.first().fom)
        assertEquals(31.mai(2021), løsning.feriepengehistorikk.first().tom)
        assertEquals(1000.0, løsning.feriepengehistorikk.first().beløp)
        assertTrue(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal ikke behandles manuelt ved merknadsdato innenfor tidligere år`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 31.desember(2020))
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal behandles manuelt ved merknadsdato innenfor tidligere år (første januar)`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 1.januar(2021))
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertTrue(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal behandles manuelt, merknader for flere år med riktig merknad`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 31.desember(2020), id = 1)
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 31.desember(2021), id = 2)
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertTrue(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal ikke behandles manuelt, merknader for flere år med feil merknad for gjeldende år`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 31.desember(2020), kode = "242", id = 1)
        opprettManuellFeriepengeberegningMerknad(merknadsdato = 31.desember(2021), kode = "512", id = 2)
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `feriepenger skal ikke behandles manuelt ved annen merknadskode`() {
        opprettPeriode()
        opprettFeriepenger(fom = 1.mai(2021), tom = 31.mai(2021))
        opprettManuellFeriepengeberegningMerknad(kode = "512")
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()

        assertFalse(løsning.feriepengerSkalBeregnesManuelt)
    }

    @Test
    fun `Svarer på behov selv om det ikke finnes historikk`() {
        rapid.sendTestMessage(behov())

        assertTrue(sisteSendtMelding.has("@løsning"))
        assertTrue(sisteSendtMelding.path("@løsning").has(SykepengehistorikkløserMK2.behov))
    }

    @Test
    fun `inkluderer periode med sykmeldtfom og sykmeldtTom innenfor fom og tom`() {
        opprettPeriode(
            sykmeldtFom = 1.mai(2020),
            sykmeldtTom = 31.mai(2020),
            utbetalinger = listOf(Utbetaling(1.mai(2020), 31.mai(2020)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer periode med sykmeldtFom før fom`() {
        opprettPeriode(
            sykmeldtFom = 1.desember(2019),
            sykmeldtTom = 31.januar(2020),
            utbetalinger = listOf(Utbetaling(1.desember(2019), 31.januar(2020)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        val løsning = sisteSendtMelding.løsning()
        assertEquals(1, løsning.utbetalinger.size)
    }

    @Test
    fun `inkluderer periode med sykmeldtTom etter Tom`() {
        opprettPeriode(
            sykmeldtFom = 31.desember(2020),
            sykmeldtTom = 31.januar(2021),
            utbetalinger = listOf(Utbetaling(31.desember(2020), 31.januar(2021)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer periode som strekker seg fra før fom til etter tom`() {
        opprettPeriode(
            sykmeldtFom = 31.desember(2019),
            sykmeldtTom = 31.januar(2021),
            utbetalinger = listOf(Utbetaling(31.desember(2019), 31.januar(2021)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer ikke periode som er før vår til etter tom`() {
        opprettPeriode(
            sykmeldtFom = 1.november(2019),
            sykmeldtTom = 1.desember(2019),
            utbetalinger = listOf(Utbetaling(1.november(2019), 1.desember(2019)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(0, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer ikke periode som er akkurat før vår til etter tom`() {
        opprettPeriode(
            sykmeldtFom = 1.desember(2019),
            sykmeldtTom = 31.desember(2019),
            utbetalinger = listOf(Utbetaling(1.desember(2019), 31.desember(2019)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(0, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer ikke periode som er etter vår til etter tom`() {
        opprettPeriode(
            sykmeldtFom = 1.februar(2021),
            sykmeldtTom = 28.februar(2021),
            utbetalinger = listOf(Utbetaling(1.februar(2021), 28.februar(2021)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(0, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer ikke periode som er akkurat etter vår til etter tom`() {
        opprettPeriode(
            sykmeldtFom = 1.januar(2021),
            sykmeldtTom = 31.januar(2021),
            utbetalinger = listOf(Utbetaling(1.januar(2021), 31.januar(2021)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(0, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer periode som er akkurat lik vår fom og tom`() {
        opprettPeriode(
            sykmeldtFom = 1.januar(2020),
            sykmeldtTom = 31.desember(2020),
            utbetalinger = listOf(Utbetaling(1.januar(2020), 31.desember(2020)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer periode som slutter på vår fom`() {
        opprettPeriode(
            sykmeldtFom = 1.desember(2019),
            sykmeldtTom = 1.januar(2020),
            utbetalinger = listOf(Utbetaling(1.desember(2019), 1.januar(2020)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    @Test
    fun `inkluderer periode som starer på vår tom`() {
        opprettPeriode(
            sykmeldtFom = 31.desember(2020),
            sykmeldtTom = 31.januar(2021),
            utbetalinger = listOf(Utbetaling(31.desember(2020), 31.januar(2021)))
        )
        rapid.sendTestMessage(behov(fom = 1.januar(2020), tom = 31.desember(2020)))
        assertEquals(1, sisteSendtMelding.løsning().utbetalinger.size)
    }

    private fun JsonNode.løsning() =
        this.path("@løsning").path(SykepengehistorikkløserMK2.behov).let {
            Sykepengehistorikk(it)
        }

    private class Sykepengehistorikk(json: JsonNode) {
        val utbetalinger = json["utbetalinger"].map { UtbetalteSykeperiode(it) }
        val inntektshistorikk = json["inntektshistorikk"].map { Inntektsopplysning(it) }
        val feriepengehistorikk = json["feriepengehistorikk"].map { Feriepenger(it) }
        val harStatslønn = json["harStatslønn"].asBoolean()
        val feriepengerSkalBeregnesManuelt = json["feriepengerSkalBeregnesManuelt"].asBoolean()
        val arbeidskategorikoder = json["arbeidskategorikoder"] as ObjectNode

        class UtbetalteSykeperiode(json: JsonNode) {
            val fom = json["fom"].asOptionalLocalDate()
            val tom = json["tom"].asOptionalLocalDate()
            val utbetalingsGrad = json["utbetalingsGrad"].asText()
            val orgnummer = json["orgnummer"].asText()
            val dagsats = json["dagsats"].asDouble()
        }

        class Inntektsopplysning(json: JsonNode) {
            val sykepengerFom = json["sykepengerFom"].asLocalDate()
            val inntekt = json["inntekt"].asInt()
            val orgnummer = json["orgnummer"].asText()
        }

        class Feriepenger(json: JsonNode) {
            val orgnummer = json["orgnummer"].asText()
            val beløp = json["beløp"].asDouble()
            val fom = json["fom"].asLocalDate()
            val tom = json["tom"].asLocalDate()
        }

        private companion object {
            fun JsonNode.asLocalDate() = LocalDate.parse(this.asText())
            fun JsonNode.asOptionalLocalDate() = takeUnless { this.isMissingOrNull() }?.asLocalDate()
        }
    }

    private fun assertSykeperiode(
        sykeperiode: Sykepengehistorikk.UtbetalteSykeperiode,
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
        inntektsopplysning: Sykepengehistorikk.Inntektsopplysning,
        dato: LocalDate,
        inntektPerMåned: Int,
        orgnummer: String
    ) {
        assertEquals(dato, inntektsopplysning.sykepengerFom)
        assertEquals(inntektPerMåned, inntektsopplysning.inntekt)
        assertEquals(orgnummer, inntektsopplysning.orgnummer)
    }

    @Language("JSON")
    private fun behov(
        opprettet: LocalDateTime = LocalDateTime.now().minusMinutes(5),
        fom: LocalDate = 1.januar(2018),
        tom: LocalDate = 1.januar(2022)
    ) =
        """
            {
            "@id": "behovsid", 
            "@opprettet":"$opprettet",
            "@behov":[
                "${SykepengehistorikkløserMK2.behov}"], 
                "${SykepengehistorikkløserMK2.behov}": { 
                    "historikkFom": "$fom", 
                    "historikkTom": "$tom"
                }, 
                "fødselsnummer": "$fnr", 
                "vedtaksperiodeId": "id"
            }
        """
}
