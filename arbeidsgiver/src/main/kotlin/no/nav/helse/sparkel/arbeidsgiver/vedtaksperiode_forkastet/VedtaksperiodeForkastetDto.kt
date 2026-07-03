package no.nav.helse.sparkel.arbeidsgiver.vedtaksperiode_forkastet

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import java.time.LocalDateTime
import java.util.*
import no.nav.helse.sparkel.arbeidsgiver.Meldingstype

internal data class VedtaksperiodeForkastetDto(
    val fødselsnummer: String,
    val organisasjonsnummer: String,
    val vedtaksperiodeId: UUID,
    val opprettet: LocalDateTime = LocalDateTime.now()
) {
    val type = Meldingstype.VEDTAKSPERIODE_FORKASTET
}

internal fun JsonMessage.toVedtaksperiodeForkastetDto(): VedtaksperiodeForkastetDto =
    VedtaksperiodeForkastetDto(
        fødselsnummer = this["fødselsnummer"].asString(),
        organisasjonsnummer = this["organisasjonsnummer"].asString(),
        vedtaksperiodeId = UUID.fromString(this["vedtaksperiodeId"].asString()),
        opprettet = this["@opprettet"].asLocalDateTime()
    )
