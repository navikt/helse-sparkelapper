package no.nav.helse.sparkel.arbeidsgiver

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.sparkel.arbeidsgiver.ForespurtOpplysning.Companion.toJsonMap

internal data class TrengerArbeidsgiveropplysningerDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val skjæringstidspunkt: LocalDate,
    val sykmeldingsperioder: List<Map<String, LocalDate>>,
    val forespurtData: List<Map<String, Any>>,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toTrengerArbeidsgiverDto(): TrengerArbeidsgiveropplysningerDto =
    TrengerArbeidsgiveropplysningerDto(
        type = Meldingstype.TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        skjæringstidspunkt = this["skjæringstidspunkt"].asLocalDate(),
        sykmeldingsperioder = this["sykmeldingsperioder"].map {
            mapOf(
                "fom" to it["fom"].asLocalDate(),
                "tom" to it["tom"].asLocalDate()
            )
        },
        forespurtData = this["forespurteOpplysninger"].asForespurteOpplysninger().toJsonMap(),
        opprettet = this["@opprettet"].asLocalDateTime()
    )

internal enum class Meldingstype {
    TRENGER_OPPLYSNINGER_FRA_ARBEIDSGIVER
}
