package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Refusjonsforslag
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto

internal const val FNR = "1111111111"
internal const val ORGNUMMER = "222222222"

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModules(JavaTimeModule())

private fun mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId: UUID,
    type: Meldingstype,
    forespurtData: List<Map<String, Any>>,
    skjæringstidspunkt: LocalDate? = LocalDate.MIN,
    sykmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
    egenmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN))
) = TrengerArbeidsgiveropplysningerDto(
    type = type,
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    skjæringstidspunkt = skjæringstidspunkt,
    sykmeldingsperioder = sykmeldingsperioder,
    egenmeldingsperioder = egenmeldingsperioder,
    forespurtData = forespurtData,
    opprettet = LocalDateTime.MAX
)
internal fun mockTrengerArbeidsgiveropplysningerMedInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt",
            "forslag" to mapOf("beregningsmåneder" to listOf(
                YearMonth.of(2022, 8),
                YearMonth.of(2022, 9),
                YearMonth.of(2022, 10)
            ))
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to emptyList<Refusjonsforslag>()
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode",
            "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
        )
    )
)

internal fun mockTrengerArbeidsgiverOpplysningerMedFastsattInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "FastsattInntekt",
            "fastsattInntekt" to 10000.0
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to listOf(
                mapOf(
                    "fom" to LocalDate.MIN,
                    "tom" to LocalDate.MIN.plusDays(10),
                    "beløp" to 10000.0
                ),
                mapOf(
                    "fom" to LocalDate.MIN.plusDays(11),
                    "tom" to null,
                    "beløp" to 9000.0
                )
            )
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode",
            "forslag" to listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN.plusDays(15)))
        )
    )
)

internal fun mockTrengerArbeidsgiverOpplysningerUtenForslag(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_BEGRENSET,
    skjæringstidspunkt = null,
    egenmeldingsperioder = emptyList(),
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt",
            "forslag" to mapOf("beregningsmåneder" to emptyList<String>())
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode",
            "forslag" to emptyList<Map<String, LocalDate>>()
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to emptyList<Map<String, Any>>()
        )
    )
)

internal fun mockInntektsmeldingHåndtert(vedtaksperiodeId: UUID) = InntektsmeldingHåndtertDto(
    type = Meldingstype.INNTEKTSMELDING_HÅNDTERT,
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId
)