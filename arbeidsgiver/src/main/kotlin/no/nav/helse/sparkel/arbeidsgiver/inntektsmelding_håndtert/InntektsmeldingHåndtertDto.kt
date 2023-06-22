package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert

import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class InntektsmeldingHåndtertDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val dokumentId: UUID?
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toInntektsmeldingHåndtertDto(dokumentId: UUID?) = InntektsmeldingHåndtertDto(
    type = Meldingstype.INNTEKTSMELDING_HÅNDTERT,
    fødselsnummer = this["fødselsnummer"].asText(),
    organisasjonsnummer = this["organisasjonsnummer"].asText(),
    vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
    dokumentId = dokumentId
)