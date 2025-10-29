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
    fun hentForsikringer(fødselsnummer: Fnr, sikkerlogg: org.slf4j.Logger): List<ForsikringDto> {
        return sessionOf(dataSource()).use { session ->

            @Language("Oracle")
            val statement = """
                    select
                        IF10_GODKJ,
                        IF10_VIRKDATO,
                        IF10_TYPE,
                        IF10_FORSTOM
                    FROM IF_VEDFRIVT_10
                    WHERE IF01_KODE = '1' AND IF01_AGNR_FNR = ?
                """
            session.run(
                queryOf(statement, fødselsnummer.formatAsITFnr()).map { rs ->
                    sikkerlogg.info("Svar rad: Type: ${rs.string("IF10_TYPE")}, Godkjent: ${rs.string("IF10_GODKJ")}, Virkdato: ${rs.int("IF10_VIRKDATO")}, Forstom: ${rs.int("IF10_FORSTOM")}\n")
                    ForsikringDto(
                        forsikringstype = when(rs.string("IF10_TYPE").trim()) {
                            "1" -> ForsikringDto.Forsikringstype.ÅttiProsentDagEn
                            "2" -> ForsikringDto.Forsikringstype.HundreProsentDagSytten
                            "3" -> ForsikringDto.Forsikringstype.HundreProsentDagEn
                            else -> ForsikringDto.Forsikringstype.IkkeInteressert
                        },
                        godkjent = rs.string("IF10_GODKJ").trim(),
                        virkningsdato = LocalDate.now(),
                        tom = LocalDate.now()
                    )
                }.asList
            )
        }
    }

    fun Row.intToLocalDate(label: String) = int(label).toLocalDate()

    private fun Int.toLocalDate() =
        LocalDate.parse(this.toString().padStart(8, '0'), DateTimeFormatter.ofPattern("yyyyMMdd"))

    data class ForsikringDto (
        val forsikringstype: Forsikringstype,
        val godkjent: String,
        val virkningsdato: LocalDate,
        val tom: LocalDate
    ) {
        enum class Forsikringstype {
            ÅttiProsentDagEn,
            HundreProsentDagEn,
            HundreProsentDagSytten,
            IkkeInteressert
        }

        internal fun erAktivPå(dato: LocalDate): Boolean {
            if (godkjent != "J") return false
            return (dato.isAfter(virkningsdato) || dato.isEqual(virkningsdato)) && (dato.isBefore(tom) || dato.isEqual(tom))
        }
    }
}
