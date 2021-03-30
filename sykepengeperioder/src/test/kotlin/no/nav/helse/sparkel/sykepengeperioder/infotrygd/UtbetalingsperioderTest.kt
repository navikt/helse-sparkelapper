package no.nav.helse.sparkel.sykepengeperioder.infotrygd

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.sparkel.sykepengeperioder.Sykepenger
import no.nav.helse.sparkel.sykepengeperioder.Utbetalingsperiode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class UtbetalingsperioderTest {

    @Test
    fun `utbetalingsperioder med manglende fom og tom i utbetalingslisten`() {
        val json = readJson("infotrygdResponseMissingFomAndTom.json")
        val utbetalingsperioder = Utbetalingsperiode.tilPerioder(json)

        assertEquals(69, utbetalingsperioder.size)
        assertNull(utbetalingsperioder[1].fom)
        assertNull(utbetalingsperioder[2].tom)
    }

    @Test
    fun `parser alle perioders utbetalingslister`() {
        val json = readJson("infotrygdResponse.json")
        val utbetalingsperioder = json.let { Utbetalingsperiode.tilPerioder(it) }
        assertEquals(69, utbetalingsperioder.size)
    }

    @Test
    fun `parser alle felter for utbetalingsperiode`() {
        val json = readJson("infotrygdResponse.json")
        val utbetalingsperioderList = json.let { Utbetalingsperiode.tilPerioder(it) }

        val enIkkeTomUtbetalingsperiode = utbetalingsperioderList.first()
        assertEquals(LocalDate.of(2019, 3, 18), enIkkeTomUtbetalingsperiode.fom)
        assertEquals(LocalDate.of(2019, 4, 11), enIkkeTomUtbetalingsperiode.tom)
        assertEquals("050", enIkkeTomUtbetalingsperiode.grad)
        assertEquals(765.0, enIkkeTomUtbetalingsperiode.dagsats)
        assertEquals("ArbRef", enIkkeTomUtbetalingsperiode.typetekst)
        assertEquals("88888888", enIkkeTomUtbetalingsperiode.organisasjonsnummer)
        assertEquals("01", enIkkeTomUtbetalingsperiode.arbeidsKategoriKode)
    }

    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    private fun readJson(fileName: String) =
        objectMapper.readValue<Sykepenger>(this::class.java.getResource("/$fileName").readText())
}