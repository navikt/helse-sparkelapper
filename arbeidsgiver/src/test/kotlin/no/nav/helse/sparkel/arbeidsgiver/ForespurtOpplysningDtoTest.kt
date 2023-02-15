package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.YearMonth
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ForespurtOpplysningDtoTest {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())

    @Test
    fun `tolker forespurt opplysninger korrekt - med inntekt`() {
        val expectedForespurteOpplysninger = listOf(
            Inntekt(Inntektsforslag(listOf(
                YearMonth.of(2022, 8),
                YearMonth.of(2022, 9),
                YearMonth.of(2022, 10)
            ))),
            Refusjon(emptyList()),
            Arbeidsgiverperiode(listOf(
                mapOf(
                    "fom" to LocalDate.of(2022, 11, 1),
                    "tom" to LocalDate.of(2022, 11, 16)
                ))
            )
        )
        val actualForespurteOpplysninger = forespurteOpplysningerMedInntektJson().asForespurteOpplysninger()

        assertEquals(expectedForespurteOpplysninger, actualForespurteOpplysninger)
    }

    @Test
    fun `tolker forespurt opplysninger korrekt - med fastsatt inntekt`() {
        val expectedForespurteOpplysninger = listOf(
            FastsattInntekt(10000.0),
            Refusjon(forslag = listOf(
                Refusjonsforslag(
                    fom = LocalDate.of(2022, 11, 1),
                    tom = null,
                    beløp = 10000.0
            ))),
            Arbeidsgiverperiode(listOf(
                mapOf(
                    "fom" to LocalDate.of(2022, 11, 1),
                    "tom" to LocalDate.of(2022, 11, 16)
                ))
            )
        )
        val actualForespurteOpplysninger = forespurteOpplysningerMedFastsattInntektJson().asForespurteOpplysninger()

        assertEquals(expectedForespurteOpplysninger, actualForespurteOpplysninger)
    }

    private fun forespurteOpplysningerMedFastsattInntektJson() = objectMapper.readTree(
        """[
                {
                    "opplysningstype": "FastsattInntekt",
                    "fastsattInntekt": 10000.0
                },
                {
                    "opplysningstype": "Refusjon", 
                    "forslag": [
                        { "fom": "2022-11-01", "tom": null, "beløp": 10000.0 }
                    ]
                },
                {
                    "opplysningstype": "Arbeidsgiverperiode",
                    "forslag": [
                        {
                            "fom": "2022-11-01",
                            "tom": "2022-11-16"
                        }
                    ]
                }
            ]
        """
    )

    private fun forespurteOpplysningerMedInntektJson() = objectMapper.readTree(
        """[
                {
                    "opplysningstype": "Inntekt",
                    "forslag": {
                        "beregningsmåneder": [
                            "2022-08",
                            "2022-09",
                            "2022-10"
                        ]
                    }
                },
                {
                    "opplysningstype": "Refusjon", 
                    "forslag": []
                },
                {
                    "opplysningstype": "Arbeidsgiverperiode",
                    "forslag": [
                        {
                            "fom": "2022-11-01",
                            "tom": "2022-11-16"
                        }
                    ]
                }
            ]
        """
    )
}