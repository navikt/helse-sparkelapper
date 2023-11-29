package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate

open class Infotrygdperiode(
    val personidentifikator: Personidentifikator,
    val organisasjonsnummer: Organisasjonsnummer?,
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int
)