package no.nav.helse.sparkel.infotrygd

import java.time.LocalDate

class Utbetalingsperiode(
    val arbeidsKategoriKode: String,
    val fom: LocalDate?,
    val tom: LocalDate?,
    val dagsats: Double,
    val grad: String,
    val typetekst: String?,
    val organisasjonsnummer: String?
)
