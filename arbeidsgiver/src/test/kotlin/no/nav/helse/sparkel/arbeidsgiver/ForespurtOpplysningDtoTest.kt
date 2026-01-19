package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Arbeidsgiverperiode
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Inntekt
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Inntektsforslag
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Refusjon
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.asForespurteOpplysninger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ForespurtOpplysningDtoTest {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    @Test
    fun `tolker forespurt opplysninger korrekt - med inntekt`() {
        val expectedForespurteOpplysninger = listOf(
            Inntekt(Inntektsforslag()),
            Refusjon(emptyList()),
            Arbeidsgiverperiode
        )
        val actualForespurteOpplysninger = forespurteOpplysningerMedInntektJson().asForespurteOpplysninger()

        assertEquals(expectedForespurteOpplysninger, actualForespurteOpplysninger)
    }

    private fun forespurteOpplysningerMedInntektJson() = objectMapper.readTree(
        """[
                {
                    "opplysningstype": "Inntekt",
                    "forslag": {}
                },
                {
                    "opplysningstype": "Refusjon", 
                    "forslag": []
                },
                {
                    "opplysningstype": "Arbeidsgiverperiode"
                }
            ]
        """
    )
}
