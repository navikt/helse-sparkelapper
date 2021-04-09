package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

internal class Utbetalingsperiode(
    val arbeidsKategoriKode: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val dagsats: Double,
    val grad: String,
    val typetekst: String?,
    val organisasjonsnummer: String?
)
