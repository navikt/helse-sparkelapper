package no.nav.helse.sparkel.forsikring

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language

class ReplikabaseForsikringDao(private val dataSource: DataSource) : ForsikringDao {
    override fun hentForsikringer(fødselsnummer: Fnr, skjæringstidspunkt: LocalDate): List<ForsikringDao.ForsikringDto> {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                    SELECT IF10_VIRKDATO, IF10_TYPE, IF10_FORSTOM, IF10_PREMGRL
                    FROM IF_VEDFRIVT_10
                    WHERE IF01_KODE = '1' AND IF01_AGNR_FNR = ? AND IF10_GODKJ = 'J'
                """
            session.run(
                queryOf(statement, fødselsnummer.formatAsITFnr()).map { rs ->
                    ForsikringDao.ForsikringDto(
                        forsikringstype = when (rs.string("IF10_TYPE").trim()) {
                            "1" -> ForsikringDao.ForsikringDto.Forsikringstype.ÅttiProsentFraDagEn
                            "2" -> ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagSytten

                            "3",
                            "4" -> ForsikringDao.ForsikringDto.Forsikringstype.HundreProsentFraDagEn

                            else -> ForsikringDao.ForsikringDto.Forsikringstype.IkkeInteressert
                        },
                        premiegrunnlag = rs.int("IF10_PREMGRL"),
                        virkningsdato = rs.intToLocalDate("IF10_VIRKDATO")!!,
                        tom = rs.intToLocalDate("IF10_FORSTOM")
                    )
                }.asList
            )
        }
    }

    private fun Row.intToLocalDate(label: String) = int(label).toLocalDate()

    private fun Int.toLocalDate() =
        if (this == 0) null else LocalDate.parse(this.toString().padStart(8, '0'), DateTimeFormatter.ofPattern("yyyyMMdd"))
}
