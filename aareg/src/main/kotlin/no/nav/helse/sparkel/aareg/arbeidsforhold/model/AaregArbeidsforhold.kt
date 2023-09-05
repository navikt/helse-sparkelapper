package no.nav.helse.sparkel.aareg.arbeidsforhold.model

import java.time.LocalDate
import no.nav.helse.sparkel.aareg.arbeidsforhold.Arbeidsforholdtype

data class AaregArbeidsforhold(
    val type: AaregArbeidsforholdtype,
    val arbeidssted: Arbeidssted,
    val ansettelsesperiode: Ansettelsesperiode,
    val ansettelsesdetaljer: List<Ansettelsesdetaljer>,
)

data class AaregArbeidsforholdtype(
    val kode: Arbeidsforholdtype,
    val beskrivelse: String,
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
    val type: Arbeidsstedtype,
    val identer: List<Ident>,
) {
    fun getOrgnummer(): String {
        return identer.first { it.type == Identtype.ORGANISASJONSNUMMER }.ident
    }
}

enum class Arbeidsstedtype {
    Underenhet,
    Person
}

data class Ident(
    val type: Identtype,
    val ident: String,
)

enum class Identtype {
    AKTORID,
    FOLKEREGISTERIDENT,
    ORGANISASJONSNUMMER
}

