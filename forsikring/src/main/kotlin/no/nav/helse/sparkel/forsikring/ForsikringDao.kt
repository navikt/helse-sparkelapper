package no.nav.helse.sparkel.forsikring

import java.time.LocalDate

interface ForsikringDao {
    fun hentForsikringer(fødselsnummer: String, skjæringstidspunkt: LocalDate): List<ForsikringDto>

    data class ForsikringDto(
        val forsikringstype: Forsikringstype,
        val premiegrunnlag: Int,
        val virkningsdato: LocalDate,
        val tom: LocalDate?
    ) {
        enum class Forsikringstype {
            ÅttiProsentFraDagEn,
            HundreProsentFraDagEn,
            HundreProsentFraDagSytten,
            IkkeInteressert
        }

        internal fun erAktivPå(dato: LocalDate): Boolean =
            !(dato.isBefore(virkningsdato) || tom != null && dato.isAfter(tom))
    }
}
