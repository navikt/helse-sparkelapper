package no.nav.helse.sparkel.infotrygd

import java.time.LocalDate

class Utbetalingshistorikk(
    val inntektsopplysninger: List<Inntektsopplysninger>,
    val utbetalteSykeperioder: List<Utbetaling>,
    val maksDato: LocalDate?,
    val statslønn: Boolean,
    val arbeidsKategoriKode: String
) {
    class Inntektsopplysninger(
        val sykepengerFom: LocalDate,
        val inntekt: Double,
        val orgnummer: String,
        val refusjonTom: LocalDate?,
        val refusjonTilArbeidsgiver: Boolean
    ) {
        enum class PeriodeKode(
            private val fraksjon: Double,
            private val kode: String
        ) {
            Daglig(260.0 / 12.0, "D"),
            Ukentlig(52.0 / 12.0, "U"),
            Biukentlig(26.0 / 12.0, "F"),
            Månedlig(1.0, "M"),
            Årlig(1.0 / 12.0, "Å"),
            SkjønnsmessigFastsatt(1.0 / 12.0, "X"),
            Premiegrunnlag(1.0 / 12.0, "Y");

            fun omregn(lønn: Double): Double = (lønn * fraksjon)

            companion object {
                val gyldigePeriodeKoder = listOf("D", "U", "F", "M", "Å", "X", "Y")
                fun verdiFraKode(kode: String): PeriodeKode {
                    return values().find { it.kode == kode }
                        ?: throw IllegalArgumentException("Ukjent kodetype $kode")
                }
            }
        }
    }

    class Utbetaling(
        val fom: LocalDate?,
        val tom: LocalDate?,
        val utbetalingsGrad: String,
        val oppgjorsType: String,
        val utbetalt: LocalDate?,
        val dagsats: Double,
        val typeKode: String,
        val typeTekst: String,
        val orgnummer: String
    )
}
