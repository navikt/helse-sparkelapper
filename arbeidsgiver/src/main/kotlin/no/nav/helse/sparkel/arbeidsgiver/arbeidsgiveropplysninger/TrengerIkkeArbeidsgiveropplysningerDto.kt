package no.nav.helse.sparkel.arbeidsgiver.arbeidsgiveropplysninger

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class TrengerIkkeArbeidsgiveropplysningerDto(
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val type = Meldingstype.TRENGER_IKKE_OPPLYSNINGER_FRA_ARBEIDSGIVER
}

internal fun JsonMessage.toTrengerIkkeArbeidsgiverDto(): TrengerIkkeArbeidsgiveropplysningerDto =
    TrengerIkkeArbeidsgiveropplysningerDto(
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
