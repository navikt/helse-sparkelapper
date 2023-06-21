package no.nav.helse.sparkel.arbeidsgiver.inntektsmelding_registrert

import java.time.LocalDateTime
import java.util.UUID

internal data class InntektsmeldingRegistrertDto(
    internal val hendelseId: UUID,
    internal val dokumentId: UUID,
    internal val opprettet: LocalDateTime
)
