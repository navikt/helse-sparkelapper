package no.nav.helse.sparkel.arbeidsgiver

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.Refusjonsforslag
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
internal fun mockTrengerIkkeArbeidsgiveropplysningerDto(
    vedtaksperiodeId: UUID,
    type: Meldingstype,
) = TrengerIkkeArbeidsgiveropplysningerDto(
    type = type,
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    opprettet = LocalDateTime.MAX
)

internal fun mockTrengerArbeidsgiveropplysningerMedForrigeInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt",
            "forslag" to mapOf(
                "forrigeInntekt" to mapOf(
                    "skjæringstidspunkt" to LocalDate.MIN,
                    "kilde" to "INNTEKTSMELDING",
                    "beløp" to 31000.0
                )
            )
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to emptyList<Refusjonsforslag>()
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        )
    )
)
internal fun mockTrengerArbeidsgiveropplysningerMedInntekt(vedtaksperiodeId: UUID) = mockTrengerArbeidsgiveropplysningerDto(
    vedtaksperiodeId = vedtaksperiodeId,
    type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
    forespurtData = listOf(
        mapOf(
            "opplysningstype" to "Inntekt",
            "forslag" to mapOf("forrigeInntekt" to null)
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to emptyList<Refusjonsforslag>()
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
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
            "opplysningstype" to "Arbeidsgiverperiode"
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
            "forslag" to mapOf("forrigeInntekt" to null)
        ),
        mapOf(
            "opplysningstype" to "Arbeidsgiverperiode"
        ),
        mapOf(
            "opplysningstype" to "Refusjon",
            "forslag" to emptyList<Map<String, Any>>()
        )
    )
)

internal fun mockInntektsmeldingHåndtert(vedtaksperiodeId: UUID, dokumentId: UUID?) = InntektsmeldingHåndtertDto(
    type = Meldingstype.INNTEKTSMELDING_HÅNDTERT,
    fødselsnummer = FNR,
    organisasjonsnummer = ORGNUMMER,
    vedtaksperiodeId = vedtaksperiodeId,
    dokumentId = dokumentId,
    opprettet = LocalDateTime.MAX
)