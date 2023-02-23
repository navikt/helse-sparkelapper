package no.nav.helse.sparkel.sputnik

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.LocalDateTime

interface Foreldrepengerløser {
    suspend fun hent(aktørId: String, fom: LocalDate, tom: LocalDate): Foreldrepengerløsning
}

data class Foreldrepengerløsning(
    @JsonProperty("Foreldrepengeytelse")
    val foreldrepengeytelse: YtelseDto? = null,
    @JsonProperty("Svangerskapsytelse")
    val svangerskapsytelse: YtelseDto? = null
)

data class YtelseDto(
    val aktørId: String,
    val fom: LocalDate,
    val tom: LocalDate,
    val vedtatt: LocalDateTime,
    val perioder: List<Periode>
)

data class Periode(
    val fom: LocalDate,
    val tom: LocalDate
)
