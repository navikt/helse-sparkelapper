package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_håndtert

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class InntektsmeldingHåndtertDto(
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val dokumentId: UUID,
    val opprettet: LocalDateTime
) {
    val type: Meldingstype = Meldingstype.INNTEKTSMELDING_HÅNDTERT
}

internal fun JsonMessage.toInntektsmeldingHåndtertDto(dokumentId: UUID) = InntektsmeldingHåndtertDto(
    fødselsnummer = this["fødselsnummer"].asText(),
    organisasjonsnummer = this["organisasjonsnummer"].asText(),
    vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
    dokumentId = dokumentId,
    opprettet = this["@opprettet"].asLocalDateTime()
)
