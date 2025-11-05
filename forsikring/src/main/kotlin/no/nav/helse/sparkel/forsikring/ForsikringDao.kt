package no.nav.helse.sparkel.forsikring

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource
import kotliquery.Row
import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language

class ForsikringDao (
    private val dataSource: () -> DataSource
){
    fun hentForsikringer(fødselsnummer: Fnr): List<ForsikringDto> {
        return sessionOf(dataSource()).use { session ->

            @Language("Oracle")
            val statement = """
                    select
                        IF10_VIRKDATO,
                        IF10_TYPE,
                        IF10_FORSTOM
                    FROM IF_VEDFRIVT_10
                    WHERE IF01_KODE = '1' AND IF01_AGNR_FNR = ? AND IF10_GODKJ = 'J'
                """
            session.run(
                queryOf(statement, fødselsnummer.formatAsITFnr()).map { rs ->
                    ForsikringDto(
                        forsikringstype = when(rs.string("IF10_TYPE").trim()) {
                            "1" -> ForsikringDto.Forsikringstype.ÅttiProsentFraDagEn
                            "2" -> ForsikringDto.Forsikringstype.HundreProsentFraDagSytten

                            "3",
                            "4" -> ForsikringDto.Forsikringstype.HundreProsentFraDagEn

                            else -> ForsikringDto.Forsikringstype.IkkeInteressert
                        },
                        virkningsdato = rs.intToLocalDate("IF10_VIRKDATO")!!,
                        tom = rs.intToLocalDate("IF10_FORSTOM")
                    )
                }.asList
            )
        }
    }

    fun Row.intToLocalDate(label: String) = int(label).toLocalDate()

    private fun Int.toLocalDate() =
        if (this == 0) null else LocalDate.parse(this.toString().padStart(8, '0'), DateTimeFormatter.ofPattern("yyyyMMdd"))

    data class ForsikringDto (
        val forsikringstype: Forsikringstype,
        val virkningsdato: LocalDate,
        val tom: LocalDate?
    ) {
        enum class Forsikringstype {
            ÅttiProsentFraDagEn,
            HundreProsentFraDagEn,
            HundreProsentFraDagSytten,
            IkkeInteressert
        }

        internal fun erAktivPå(dato: LocalDate): Boolean {
            return (dato.isAfter(virkningsdato) || dato.isEqual(virkningsdato)) && (tom == null || dato.isBefore(tom) || dato.isEqual(tom))
        }
    }
}
