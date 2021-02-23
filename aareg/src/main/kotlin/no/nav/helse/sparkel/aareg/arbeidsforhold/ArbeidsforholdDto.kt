package no.nav.helse.sparkel.aareg.arbeidsforhold

import java.time.LocalDate

data class ArbeidsforholdDto(
    val stillingstittel: String,
    val stillingsprosent: Int,
    val startdato: LocalDate,
    val sluttdato: LocalDate? = null
)
