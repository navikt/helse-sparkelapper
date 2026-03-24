package no.nav.helse.sparkel.infotrygd.api

enum class Periodetype {
    UTBETALING,
    REDUKSJON_MEDLEM,
    ETTERBETALING_FORHØYET_LØNN,
    ETTERBETALING_INNTEKT_MANGLER,
    KONTERT_REGNSKAP,
    ARB_REF,
    REDUKSJON_ARB_REF,
    TILBAKEFØRT,
    KONVERTERT,
    FERIE,
    OPPHOLD,
    SANKSJON,
    UKJENT;

    companion object {
        fun fraKode(kode: String) = when (kode) {
            "0" -> UTBETALING
            "1" -> REDUKSJON_MEDLEM
            "2" -> ETTERBETALING_FORHØYET_LØNN
            "3" -> ETTERBETALING_INNTEKT_MANGLER
            "4" -> KONTERT_REGNSKAP
            "5" -> ARB_REF
            "6" -> REDUKSJON_ARB_REF
            "7" -> TILBAKEFØRT
            "8" -> KONVERTERT
            "9" -> FERIE
            "O" -> OPPHOLD
            "S" -> SANKSJON
            else -> UKJENT
        }
    }
}
