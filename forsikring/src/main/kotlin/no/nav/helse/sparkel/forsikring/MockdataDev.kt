package no.nav.helse.sparkel.forsikring

import java.time.LocalDate

internal fun mockdataDev(fødselsnummer: Fnr, skjæringstidspunkt: LocalDate): List<ForsikringDao.ForsikringDto> {
    return when (fødselsnummer) {
        Fnr("29500053761") -> listOf(
            ForsikringDao.ForsikringDto(
                forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagSytten,
                premiegrunnlag = 450000,
                virkningsdato = skjæringstidspunkt.minusDays(10),
                tom = null
            )
        )

        Fnr("16500094528") -> listOf(
            ForsikringDao.ForsikringDto(
                forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagEn,
                premiegrunnlag = 450000,
                virkningsdato = skjæringstidspunkt.minusDays(10),
                tom = null
            )
        )

        Fnr("05420167468") -> listOf(
            ForsikringDao.ForsikringDto(
                forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.ÅttiProsentFraDagEn,
                premiegrunnlag = 450000,
                virkningsdato = skjæringstidspunkt.minusDays(10),
                tom = null
            )
        )

        Fnr("24500092005") -> listOf(
            ForsikringDao.ForsikringDto(
                forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagEn,
                premiegrunnlag = 450000,
                virkningsdato = skjæringstidspunkt.minusDays(10),
                tom = null
            )
        )

        else -> emptyList()
    }
}
