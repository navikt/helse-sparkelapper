package no.nav.helse.sparkel.aareg.arbeidsforhold

import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.sparkel.aareg.arbeidsforhold.ArbeidsforholdLøserV2.Companion.toArbeidsforhold
import no.nav.helse.sparkel.aareg.arbeidsforhold.Arbeidsforholdbehovløser.Companion.toLøsningDto
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforhold
import no.nav.helse.sparkel.aareg.arbeidsforhold.model.AaregArbeidsforholdMedDetaljer
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.azureTokenStub
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AaregClientTest {

    @Test
    fun `mapping toArbeidsforhold fra aareg er ok`() {
        val azureAdMock = azureTokenStub()
        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            scope = "aareg-scope",
            tokenSupplier = azureAdMock,
            httpClient = aaregMockClient()
        )

        val aaregResponse = runBlocking { aaregClient.hentFraAareg<AaregArbeidsforhold>("12343555", UUID.randomUUID()) }
        val arbeidsforhold = aaregResponse.toArbeidsforhold()

        assertEquals("123456789", arbeidsforhold[0].orgnummer)
        assertEquals(LocalDate.of(2003, 8, 3), arbeidsforhold[0].ansattSiden)
        assertEquals(LocalDate.of(2010, 8, 3), arbeidsforhold[0].ansattTil)
        assertEquals(Arbeidsforholdtype.ORDINÆRT, arbeidsforhold[0].type)
    }

    @Test
    fun `mapping toLøsningDto fra aareg er ok`() {
        val azureAdMock = azureTokenStub()
        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            scope = "aareg-scope",
            tokenSupplier = azureAdMock,
            httpClient = aaregMockClient()
        )

        val aaregResponse = runBlocking { aaregClient.hentFraAareg<AaregArbeidsforholdMedDetaljer>("12343555", UUID.randomUUID())  }
        val løsningsDto = aaregResponse.toLøsningDto()


        assertEquals(100, løsningsDto[0].stillingsprosent)
        assertEquals("HELSEFAGARBEIDER", løsningsDto[0].stillingstittel)
        assertEquals(LocalDate.of(2003, 8, 3), løsningsDto[0].startdato)
        assertEquals(LocalDate.of(2010, 8, 3), løsningsDto[0].sluttdato)
    }

}

