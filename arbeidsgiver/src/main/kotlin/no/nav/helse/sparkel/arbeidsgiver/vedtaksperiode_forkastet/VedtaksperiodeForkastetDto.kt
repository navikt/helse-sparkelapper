package no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet

import java.time.LocalDateTime
import java.util.UUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class VedtaksperiodeForkastetDto(
    val type: Meldingstype,
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val meldingstype get() = type.name.lowercase().toByteArray()
}

internal fun JsonMessage.toVedtaksperiodeForkastetDto(): VedtaksperiodeForkastetDto =
    VedtaksperiodeForkastetDto(
        type = Meldingstype.VEDTAKSPERIODE_FORKASTET,
        fødselsnummer = this["fødselsnummer"].asText(),
        organisasjonsnummer = this["organisasjonsnummer"].asText(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asText()),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
