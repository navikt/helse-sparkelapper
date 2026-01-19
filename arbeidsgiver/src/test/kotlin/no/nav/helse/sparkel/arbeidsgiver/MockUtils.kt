package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerBegrensetDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.TrengerIkkeArbeidsgiveropplysningerDto
import no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert.InntektsmeldingHåndtertDto

internal const val FNR = "fnr"
internal const val ORGNUMMER = "orgnummer"

internal val objectMapper = jacksonObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModules(JavaTimeModule())

private fun mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId: UUID,
    forespurtData: List<Map<String, Any>>,
    skjæringstidspunkt: LocalDate? = LocalDate.MIN,
    sykmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
    egenmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
    bestemmendeFraværsdager: Map<String, LocalDate> = mapOf(ORGNUMMER to LocalDate.MIN)
) = TrengerArbeidsgiveropplysningerDto(
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    skjæringstidspunkt = skjæringstidspunkt,
    sykmeldingsperioder = sykmeldingsperioder,
    egenmeldingsperioder = egenmeldingsperioder,
    forespurtData = forespurtData,
    bestemmendeFraværsdager = bestemmendeFraværsdager,
    opprettet = LocalDateTime.MAX
)
private fun mockTrengerArbeidsgiveropplysningerBegrensetDto(
    vedtaksperiodeId: UUID,
    forespurtData: List<Map<String, Any>>,
    skjæringstidspunkt: LocalDate? = LocalDate.MIN,
    sykmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN.plusDays(1), "tom" to LocalDate.MIN.plusDays(30))),
    egenmeldingsperioder: List<Map<String, LocalDate>> = listOf(mapOf("fom" to LocalDate.MIN, "tom" to LocalDate.MIN)),
    bestemmendeFraværsdager: Map<String, LocalDate> = mapOf(ORGNUMMER to LocalDate.MIN)
) = TrengerArbeidsgiveropplysningerBegrensetDto(
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    skjæringstidspunkt = skjæringstidspunkt,
    sykmeldingsperioder = sykmeldingsperioder,
    egenmeldingsperioder = egenmeldingsperioder,
    forespurtData = forespurtData,
    bestemmendeFraværsdager = bestemmendeFraværsdager,
    opprettet = LocalDateTime.MAX
)

internal fun mockTrengerIkkeArbeidsgiveropplysningerDto(
    vedtaksperiodeId: UUID,
) = TrengerIkkeArbeidsgiveropplysningerDto(
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    opprettet = LocalDateTime.MAX
)

internal fun mockTrengerArbeidsgiveropplysningerMedForrigeInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt"
        ),
        mapOf(
            "opplysningstype" to "Refusjon"
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        )
    )
)
internal fun mockTrengerArbeidsgiveropplysningerMedInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt"
        ),
        mapOf(
            "opplysningstype" to "Refusjon"
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        )
    )
)

internal fun mockTrengerArbeidsgiverOpplysningerMedFastsattInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Refusjon"
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        )
    )
)

internal fun mockTrengerArbeidsgiverOpplysningerForespørAlt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerBegrensetDto(
    vedtaksperiodeId = vedtaksperiodeId,
    skjæringstidspunkt = null,
    egenmeldingsperioder = emptyList(),
    bestemmendeFraværsdager = emptyMap(),
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt"
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        ),
        mapOf(
            "opplysningstype" to "Refusjon"
        )
    )
)

internal fun mockInntektsmeldingHåndtert(vedtaksperiodeId: UUID, dokumentId: UUID) = InntektsmeldingHåndtertDto(
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    dokumentId = dokumentId,
    opprettet = LocalDateTime.MAX
)
