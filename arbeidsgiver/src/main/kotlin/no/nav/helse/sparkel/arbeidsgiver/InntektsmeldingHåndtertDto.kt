package no.nav.helse.sparkel.arbeidsgiver

import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage

internal data class InntektsmeldingHåndtertDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toInntektsmeldingHåndtertDto() = InntektsmeldingHåndtertDto(
    type = Meldingstype.INNTEKTSMELDING_HÅNDTERT,
    fødselsnummer = this["fødselsnummer"].asText(),
    organisasjonsnummer = this["organisasjonsnummer"].asText(),
    vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText())
)