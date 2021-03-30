package no.nav.helse.sparkel.sykepengeperioder

import org.slf4j.LoggerFactory
import java.time.LocalDate

internal class Utbetalingshistorikk(
    val inntektsopplysninger: List<Inntektsopplysninger>,
    val utbetalteSykeperioder: List<Utbetaling>,
    val maksDato: LocalDate?,
    val statslønn: Boolean,
    val arbeidsKategoriKode: String
) {

    internal companion object {
        private val log = LoggerFactory.getLogger(Utbetalingshistorikk::class.java)
        private val tjenestekallLog = LoggerFactory.getLogger("tjenestekall")

        internal fun tilPerioder(sykepenger: Sykepenger) = sykepenger
            .sykmeldingsperioder
            .sortedByDescending { it.sykemeldtFom }
            .mapIndexed { index, periode ->
                Utbetalingshistorikk(
                    inntektsopplysninger = Inntektsopplysninger.tilInntektsopplysninger(periode.inntektList),
                    utbetalteSykeperioder = Utbetaling.tilUtbetalinger(periode.utbetalingList) + ferie(periode),
                    maksDato = periode.slutt,
                    statslønn = index == 0 && periode.statslonnList.isNotEmpty(),
                    arbeidsKategoriKode = periode.arbeidsKategoriKode
                )
            }

        private fun ferie(periode: Sykepenger.Periode) = listOfNotNull(
            feriemapper(periode.ferie1Fom, periode.ferie1Tom),
            feriemapper(periode.ferie2Fom, periode.ferie2Tom)
        )

        private fun feriemapper(fom: LocalDate?, tom: LocalDate?): Utbetaling? =
            if (fom == null || tom == null) null
            else Utbetaling(fom, tom, "", "", fom, 0.0, "9", "Ferie", "0")
    }

    internal class Inntektsopplysninger(
        val sykepengerFom: LocalDate,
        val inntekt: Double,
        val orgnummer: String,
        val refusjonTom: LocalDate?,
        val refusjonTilArbeidsgiver: Boolean
    ) {

        internal companion object {
            internal val gyldigePeriodeKoder = listOf("D", "U", "F", "M", "Å", "X", "Y")
            fun tilInntektsopplysninger(inntektList: List<Sykepenger.Periode.Inntekt>) = inntektList
                .filter {
                    when (val periodeKode = it.periodeKode) {
                        in gyldigePeriodeKoder -> true
                        else -> {
                            log.warn("Ukjent periodetype i respons fra Infotrygd: $periodeKode")
                            tjenestekallLog.warn("Ukjent periodetype i respons fra Infotrygd: $periodeKode")
                            false
                        }
                    }
                }
                .filter { PeriodeKode.verdiFraKode(it.periodeKode) != PeriodeKode.Premiegrunnlag }
                .map {
                    Inntektsopplysninger(
                        it.sykepengerFom,
                        PeriodeKode.verdiFraKode(it.periodeKode).omregn(it.loenn),
                        it.orgNr,
                        it.refusjonTom,
                        "J" == it.refusjonsType
                    )
                }
        }

        internal enum class PeriodeKode(
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

            internal fun omregn(lønn: Double): Double = (lønn * fraksjon)

            internal companion object {
                internal fun verdiFraKode(kode: String): PeriodeKode {
                    return values().find { it.kode == kode }
                        ?: throw IllegalArgumentException("Ukjent kodetype $kode")
                }
            }
        }
    }

    internal class Utbetaling(
        val fom: LocalDate?,
        val tom: LocalDate?,
        val utbetalingsGrad: String,
        val oppgjorsType: String,
        val utbetalt: LocalDate?,
        val dagsats: Double,
        val typeKode: String,
        val typeTekst: String,
        val orgnummer: String
    ) {
        internal companion object {
            internal fun tilUtbetalinger(utbetalingList: List<Sykepenger.Periode.Utbetaling>) = utbetalingList
                .map {
                    Utbetaling(
                        fom = it.fom,
                        tom = it.tom,
                        utbetalingsGrad = it.utbetalingsGrad,
                        oppgjorsType = it.oppgjorsType,
                        utbetalt = it.utbetalt,
                        dagsats = it.dagsats,
                        typeKode = it.typeKode,
                        typeTekst = it.typeTekst,
                        orgnummer = it.arbOrgnr
                    )
                }
        }
    }
}
