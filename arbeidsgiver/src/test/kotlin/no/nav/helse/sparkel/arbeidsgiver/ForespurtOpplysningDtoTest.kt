package no.nav.helse.sparkel.arbeidsgiver

import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Arbeidsgiverperiode
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Inntekt
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Refusjon
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.asForespurteOpplysninger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import tools.jackson.module.kotlin.jacksonObjectMapper

internal class ForespurtOpplysningDtoTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `tolker forespurt opplysninger korrekt - med inntekt`() {
        val expectedForespurteOpplysninger = listOf(
            Inntekt,
            Refusjon,
            Arbeidsgiverperiode
        )
        val actualForespurteOpplysninger = forespurteOpplysningerMedInntektJson().asForespurteOpplysninger()

        assertEquals(expectedForespurteOpplysninger, actualForespurteOpplysninger)
    }

    private fun forespurteOpplysningerMedInntektJson() = objectMapper.readTree(
        """[
                {
                    "opplysningstype": "Inntekt"
                },
                {
                    "opplysningstype": "Refusjon"
                },
                {
                    "opplysningstype": "Arbeidsgiverperiode"
                }
            ]
        """
    )
}
