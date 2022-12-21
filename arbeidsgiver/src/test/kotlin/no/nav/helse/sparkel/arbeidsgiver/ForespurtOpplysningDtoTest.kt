package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class ForespurtOpplysningDtoTest {
    private val objectMapper = jacksonObjectMapper()
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(JavaTimeModule())
    @Test
    fun `tolker forespurt opplysninger korrekt`() {
        val expectedForespurteOpplysninger = listOf(
            FastsattInntekt(10000.0),
            Refusjon,
            Arbeidsgiverperiode(listOf(
                mapOf(
                    "fom" to LocalDate.of(2022, 11, 1),
                    "tom" to LocalDate.of(2022, 11, 16)
                ))
            )
        )
        val actualForespurteOpplysninger = forespurteOpplysningerJson().asForespurteOpplysninger()

        assertEquals(expectedForespurteOpplysninger, actualForespurteOpplysninger)
    }

    private fun forespurteOpplysningerJson() = objectMapper.readTree(
        """[
                {
                    "opplysningstype": "FastsattInntekt",
                    "fastsattInntekt": 10000.0
                },
                {
                    "opplysningstype": "Refusjon"
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