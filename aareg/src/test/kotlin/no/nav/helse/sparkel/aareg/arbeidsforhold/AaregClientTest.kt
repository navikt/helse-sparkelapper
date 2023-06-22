package no.nav.helse.sparkel.aareg.arbeidsforhold

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import no.nav.helse.rapids_rivers.isMissingOrNull
import no.nav.helse.sparkel.aareg.AzureAD
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregMockClient
import no.nav.helse.sparkel.aareg.arbeidsforhold.util.aaregmockGenerator
import no.nav.helse.sparkel.aareg.kodeverk.KodeverkClient
import no.nav.helse.sparkel.aareg.sikkerlogg
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class AaregClientTest {

    private val kodeverkClientMock = mockk<KodeverkClient>()

    @Test
    fun `mapping toArbeidsforhold fra aareg er ok`() {

        val azureAdMock = mockk<AzureAD>()

        every { azureAdMock.accessToken() } returns "superToken"

        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            tokenSupplier = { azureAdMock.accessToken() },
            httpClient = aaregMockClient(aaregmockGenerator)
        )

        val aaregResponse = runBlocking { aaregClient.hentFraAareg("12343555", UUID.randomUUID()) }

        val arbeidsforhold = aaregResponse.map { it.toArbeidsforhold() }

        assertEquals("organisasjonsnummer", arbeidsforhold[0].orgnummer)
        assertEquals(LocalDate.of(2014, 7, 1), arbeidsforhold[0].ansattSiden)
        assertEquals(LocalDate.of(2015, 12, 31), arbeidsforhold[0].ansattTil)
    }

    @Test
    fun `mapping toLøsningDto fra aareg er ok`() {

        val azureAdMock = mockk<AzureAD>()

        every { azureAdMock.accessToken() } returns "superToken"

        every { kodeverkClientMock.getYrke(any()) } returns "utvikler"

        val aaregClient = AaregClient(
            baseUrl = "http://baseUrl.local",
            tokenSupplier = { azureAdMock.accessToken() },
            httpClient = aaregMockClient(aaregmockGenerator)
        )

        val aaregResponse = runBlocking { aaregClient.hentFraAareg("12343555", UUID.randomUUID()) }

        val listArbeidsforholdbehovløserLøsingDto = aaregResponse.filter { arbeidsforhold ->
            arbeidsforhold["arbeidsgiver"].path("organisasjonsnummer").asText() == "organisasjonsnummer"
        }
            .filter { it.path("innrapportertEtterAOrdningen").asBoolean() }
            .also {
                if (it.any { arbeidsforhold ->
                        !arbeidsforhold.path("arbeidsavtaler").any { arbeidsavtale ->
                            arbeidsavtale.path("gyldighetsperiode").path("tom")
                                .isMissingOrNull()
                        }
                    }) {
                    sikkerlogg.info("RESTen av svaret {}", it)
                }
            }
            .toLøsningDto()


        assertEquals(49, listArbeidsforholdbehovløserLøsingDto[0].stillingsprosent)
        assertEquals(LocalDate.of(2015,12,31), listArbeidsforholdbehovløserLøsingDto[0].sluttdato)
        assertEquals(LocalDate.of(2014,7,1), listArbeidsforholdbehovløserLøsingDto[0].startdato)
        assertEquals("utvikler", listArbeidsforholdbehovløserLøsingDto[0].stillingstittel)
    }


    private fun JsonNode.toArbeidsforhold() = Arbeidsforhold(
        ansattSiden = this.path("ansettelsesperiode").path("periode").path("fom").asLocalDate(),
        ansattTil = this.path("ansettelsesperiode").path("periode").path("tom").asOptionalLocalDate(),
        orgnummer = this["arbeidsgiver"].path("organisasjonsnummer").asText()
    )

    private fun List<JsonNode>.toLøsningDto(): List<Arbeidsforholdbehovløser.LøsningDto> =
        this.flatMap { arbeidsforhold ->
            arbeidsforhold.path("arbeidsavtaler").map {
                Arbeidsforholdbehovløser.LøsningDto(
                    startdato = it.path("gyldighetsperiode").path("fom").asLocalDate(),
                    sluttdato = it.path("gyldighetsperiode").path("tom")?.asOptionalLocalDate(),
                    stillingsprosent = it.path("stillingsprosent")?.asInt() ?: 0,
                    stillingstittel = kodeverkClientMock.getYrke(it.path("yrke").asText())
                )
            }
        }
}
