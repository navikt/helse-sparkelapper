package no.nav.helse.sparkel.sykepengeperioder

import java.time.LocalDate

data class Sykepenger(
    val sykmeldingsperioder: List<Periode>
) {
    data class Periode(
        val ident: Long,
        val tknr: String,
        val seq: Int,
        val sykemeldtFom: LocalDate,
        val sykemeldtTom: LocalDate,
        val grad: String,
        val slutt: LocalDate?,
        val erArbeidsgiverPeriode: Boolean,
        val ferie1Fom: LocalDate?,
        val ferie1Tom: LocalDate?,
        val ferie2Fom: LocalDate?,
        val ferie2Tom: LocalDate?,
        val stansAarsakKode: String,
        val stansAarsak: String,
        val unntakAktivitet: String,
        val arbeidsKategoriKode: String,
        val arbeidsKategori: String,
        val arbeidsKategori99: String,
        val sanksjonFom: LocalDate?,
        val sanksjonTom: LocalDate?,
        val erSanksjonBekreftet: String,
        val sanksjonsDager: Int,
        val opphoerFom: LocalDate?,
        val sykemelder: String,
        val behandlet: LocalDate,
        val yrkesskadeArt: String,
        val skadet: LocalDate?,
        val vedtatt: LocalDate?,
        val utbetalingList: List<Utbetaling>,
        val inntektList: List<Inntekt>,
        val graderingList: List<Gradering>,
        val forsikring: List<Forsikring>,
        val statslonnList: List<Statslønn>
    ) {
        data class Utbetaling(
            val fom: LocalDate?,
            val tom: LocalDate?,
            val utbetalingsGrad: String,
            val oppgjorsType: String,
            val utbetalt: LocalDate?,
            val dagsats: Double,
            val typeKode: String,
            val typeTekst: String,
            val arbOrgnr: String
        )

        data class Inntekt(
            val orgNr: String,
            val sykepengerFom: LocalDate,
            val refusjonTom: LocalDate?,
            val refusjonsType: String,
            val periodeKode: String,
            val periode: String,
            val loenn: Double
        )

        data class Gradering(
            val gradertFom: LocalDate,
            val gradertTom: LocalDate,
            val grad: String
        )

        class Forsikring(
            val forsikringsOrdning: String,
            val premieGrunnlag: Double,
            val erGyldig: Boolean,
            val forsikretFom: LocalDate,
            val forsikretTom: LocalDate
        )

        class Statslønn(
            val statLonn: Int
        )
    }
}