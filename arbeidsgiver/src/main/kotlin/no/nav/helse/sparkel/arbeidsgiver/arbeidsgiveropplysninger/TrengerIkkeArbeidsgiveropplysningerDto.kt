package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class TrengerIkkeArbeidsgiveropplysningerDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toTrengerIkkeArbeidsgiverDto(): TrengerIkkeArbeidsgiveropplysningerDto =
    TrengerIkkeArbeidsgiveropplysningerDto(
        type = Meldingstype.TRENGER_IKKE_OPPLYSNINGER_FRA_ARBEIDSGIVER,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
