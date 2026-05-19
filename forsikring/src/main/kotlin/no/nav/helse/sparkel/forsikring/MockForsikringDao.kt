package no.nav.helse.sparkel.forsikring

import java.time.LocalDate

class MockForsikringDao : ForsikringDao {
    override fun hentForsikringer(fødselsnummer: String, skjæringstidspunkt: LocalDate): List<ForsikringDao.ForsikringDto> =
        when (fødselsnummer) {
            "29500053761" -> listOf(
                ForsikringDao.ForsikringDto(
                    forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagSytten,
                    premiegrunnlag = 450000,
                    virkningsdato = skjæringstidspunkt.minusDays(10),
                    tom = null
                )
            )

            "16500094528" -> listOf(
                ForsikringDao.ForsikringDto(
                    forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagEn,
                    premiegrunnlag = 450000,
                    virkningsdato = skjæringstidspunkt.minusDays(10),
                    tom = null
                )
            )

            "05420167468" -> listOf(
                ForsikringDao.ForsikringDto(
                    forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.ÅttiProsentFraDagEn,
                    premiegrunnlag = 450000,
                    virkningsdato = skjæringstidspunkt.minusDays(10),
                    tom = null
                )
            )

            "24500092005" -> listOf(
                ForsikringDao.ForsikringDto(
                    forsikringstype = ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagEn,
                    premiegrunnlag = 450000,
                    virkningsdato = skjæringstidspunkt.minusDays(10),
                    tom = null
                )
            )

            else -> emptyList()
        }

    override fun hentFullstendigeForsikringer(fødselsnummer: String): List<ForsikringDao.RåForsikringDto> = emptyList()
}
