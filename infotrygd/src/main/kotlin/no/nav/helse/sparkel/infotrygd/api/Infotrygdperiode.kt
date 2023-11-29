package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate

class Infotrygdperiode(
    val personidentifikator: Personidentifikator,
    val organisasjonsnummer: Organisasjonsnummer?,
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int,
    val pålitelig: Boolean = true
)