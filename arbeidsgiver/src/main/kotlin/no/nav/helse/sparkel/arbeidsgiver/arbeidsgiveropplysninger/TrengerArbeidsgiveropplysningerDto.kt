package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.ForespurtOpplysning.Companion.toJsonMap
import no.nav.helse.sparkel.arbeidsgiver.toPerioder

internal data class TrengerArbeidsgiveropplysningerDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val skjæringstidspunkt: LocalDate?,
    val bestemmendeFraværsdager: Map<String, LocalDate>,
    val sykmeldingsperioder: List<Map<String, LocalDate>>,
    val egenmeldingsperioder: List<Map<String, LocalDate>>,
    val forespurtData: List<Map<String, Any>>,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toKomplettTrengerArbeidsgiveropplysningerDto(): TrengerArbeidsgiveropplysningerDto =
    TrengerArbeidsgiveropplysningerDto(
        type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        skjæringstidspunkt = this["skjæringstidspunkt"].asLocalDate(),
        bestemmendeFraværsdager = this["førsteFraværsdager"].asBestemmendeFraværsdager(),
        sykmeldingsperioder = this["sykmeldingsperioder"].toPerioder(),
        egenmeldingsperioder = this["egenmeldingsperioder"].toPerioder(),
        forespurtData = this["forespurteOpplysninger"].asForespurteOpplysninger().toJsonMap(),
        opprettet = this["@opprettet"].asLocalDateTime()
    )

internal fun JsonMessage.toPotensiellTrengerArbeidsgiveropplysningerDto(): TrengerArbeidsgiveropplysningerDto =
    TrengerArbeidsgiveropplysningerDto(
        type = Meldingstype.TRENGER_POTENSIELT_OPPLYSNINGER_FRA_ARBEIDSGIVER,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        skjæringstidspunkt = this["skjæringstidspunkt"].asLocalDate(),
        bestemmendeFraværsdager = emptyMap(),
        sykmeldingsperioder = this["sykmeldingsperioder"].toPerioder(),
        egenmeldingsperioder = this["egenmeldingsperioder"].toPerioder(),
        forespurtData = emptyList(),
        opprettet = this["@opprettet"].asLocalDateTime()
    )

internal fun JsonMessage.toBegrensetTrengerArbeidsgiveropplysningerDto(): TrengerArbeidsgiveropplysningerDto =
    TrengerArbeidsgiveropplysningerDto(
        type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_BEGRENSET,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        skjæringstidspunkt = null,
        bestemmendeFraværsdager = emptyMap(),
        sykmeldingsperioder = this["sykmeldingsperioder"].toPerioder(),
        egenmeldingsperioder = emptyList(),
        forespurtData = listOf(
            Inntekt(Inntektsforslag()),
            Arbeidsgiverperiode,
            Refusjon(emptyList())
        ).toJsonMap(),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
