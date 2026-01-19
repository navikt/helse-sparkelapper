package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype
import no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger.ForespurtOpplysning.Companion.toJsonMap
import no.nav.helse.sparkel.arbeidsgiver.toPerioder

internal data class TrengerArbeidsgiveropplysningerDto(
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
    val type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_KOMPLETT
}
internal data class TrengerArbeidsgiveropplysningerBegrensetDto(
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
    val type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER_BEGRENSET
}

internal fun JsonMessage.toKomplettTrengerArbeidsgiveropplysningerDto(): TrengerArbeidsgiveropplysningerDto =
    TrengerArbeidsgiveropplysningerDto(
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

internal fun JsonMessage.toBegrensetTrengerArbeidsgiveropplysningerDto(): TrengerArbeidsgiveropplysningerBegrensetDto =
    TrengerArbeidsgiveropplysningerBegrensetDto(
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        skjæringstidspunkt = null,
        bestemmendeFraværsdager = emptyMap(),
        sykmeldingsperioder = this["sykmeldingsperioder"].toPerioder(),
        egenmeldingsperioder = emptyList(),
        forespurtData = listOf(
            Inntekt,
            Arbeidsgiverperiode,
            Refusjon
        ).toJsonMap(),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
