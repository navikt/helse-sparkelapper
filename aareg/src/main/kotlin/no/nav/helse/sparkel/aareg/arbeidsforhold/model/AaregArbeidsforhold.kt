package no.nav.helse.sparkel.aareg.arbeidsforhold.model

import java.time.LocalDate

data class AaregArbeidsforhold(
    val arbeidssted: Arbeidssted,
    val ansettelsesperiode: Ansettelsesperiode,
    val ansettelsesdetaljer: Ansettelsesdetaljer,
)

data class Ansettelsesperiode(
    val startdato: LocalDate,
    val sluttdato: LocalDate?,
)

data class Ansettelsesdetaljer(
    val avtaltStillingsprosent: Int,
    val yrke: Yrke,
)

data class Yrke(
    val kode: String,
    val beskrivelse: String,
)

data class Arbeidssted(
    val type: ArbeidsstedType,
    val identer: List<Ident>,
) {
    fun getOrgnummer(): String {
        return identer.first { it.type == IdentType.ORGANISASJONSNUMMER }.ident
    }
}

enum class ArbeidsstedType {
    Underenhet,
    Person
}

data class Ident(
    val type: IdentType,
    val ident: String,
)

enum class IdentType {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER
}

