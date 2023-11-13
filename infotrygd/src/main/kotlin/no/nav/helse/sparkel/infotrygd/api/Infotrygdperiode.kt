package no.nav.helse.sparkel.infotrygd.api

import java.time.LocalDate

class Infotrygdperiode(
    val fom: LocalDate,
    val tom: LocalDate,
    val grad: Int
)