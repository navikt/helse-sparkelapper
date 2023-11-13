package no.nav.helse.sparkel.infotrygd

import kotliquery.queryOf
import kotliquery.sessionOf
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.sql.DataSource

class PeriodeDAO(
    private val dataSource: DataSource
) {
    fun perioder(fnr: Fnr, fom: LocalDate, tom: LocalDate): List<PeriodeDTO> {
        return sessionOf(dataSource).use { session ->
            @Language("Oracle")
            val statement = """
                    select 
           is01_personkey
         , f_nr
         , tk_nr
         , is10_arbufoer_seq

        -- Periode
         , is10_arbufoer           -- sykmeldtfom
         , is10_arbufoer_tom       -- sykmeldttom
         , is10_ufoeregrad         -- grad
         , is10_max                -- slutt
         , is10_arbper             -- erarbeidsgiverperiode
         , is10_ferie_fom          -- ferie1 fom
         , is10_ferie_tom          -- ferie1 tom
         , is10_ferie_fom2         -- ferie2 fom
         , is10_ferie_tom2         -- ferie2 tom
         , is10_stans              -- stansaarsak
         , is10_unntak_aktivitet   -- unntakaktivitet
         , is10_arbkat             -- arbeidskategori
         , is10_arbkat_99          -- arbeidskategori_99
         , is10_sanksjon_fom       -- sanksjon fom
         , is10_sanksjon_tom       -- sanksjon tom
         , is10_sanksjon_bekreftet -- ersanksjonbekreftet
         , is10_sanksjonsdager     -- sanksjonsdager
         , is10_stoppdato          -- opphoerfom

        -- Sykemelding
         , is10_legenavn           -- sykmelder
         , is10_behdato            -- behandlet


        -- Yrkesskade
         , is10_skadeart           -- skadeart
         , is10_skdato             -- skadet
         , is10_skm_mott           -- vedtatt

         from is_periode_10
         where f_nr = :fnr
         and is10_stoenads_type = '  ' -- dvs. sykepenger
         and (is10_frisk is null or is10_frisk != 'H')
         and :fom <= is10_arbufoer_tom
         and :tom >= is10_arbufoer
         order by is10_arbufoer desc
                """
            session.run(
                queryOf(
                    statement,
                    mapOf(
                        "fnr" to fnr.formatAsITFnr(),
                        "fom" to fom.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt(),
                        "tom" to tom.format(DateTimeFormatter.ofPattern("yyyyMMdd")).toInt()
                    )
                ).map { rs ->
                    PeriodeDTO(
                        ident = rs.long("is01_personkey"),
                        tknr = rs.string("tk_nr").trim(),
                        seq = rs.int("is10_arbufoer_seq"),
                        sykemeldtFom = rs.intOrNullToLocalDate("is10_arbufoer"),
                        sykemeldtTom = rs.intOrNullToLocalDate("is10_arbufoer_tom"),
                        grad = rs.stringOrNull("is10_ufoeregrad")?.trim(),
                        slutt = rs.intOrNullToLocalDate("is10_max"),
                        erArbeidsgiverPeriode = rs.stringOrNull("is10_arbper")?.trim().equals("j", ignoreCase = true),
                        ferie1Fom = rs.intOrNullToLocalDate("is10_ferie_fom"),
                        ferie1Tom = rs.intOrNullToLocalDate("is10_ferie_tom"),
                        ferie2Fom = rs.intOrNullToLocalDate("is10_ferie_fom2"),
                        ferie2Tom = rs.intOrNullToLocalDate("is10_ferie_tom2"),
                        stansAarsak = rs.stringOrNull("is10_stans")?.trim(),
                        unntakAktivitet = rs.stringOrNull("is10_unntak_aktivitet")?.trim(),
                        arbeidsKategori = rs.string("is10_arbkat").trim(),
                        arbeidsKategori99 = rs.stringOrNull("is10_arbkat_99")?.trim(),
                        sanksjonFom = rs.intOrNullToLocalDate("is10_sanksjon_fom"),
                        sanksjonTom = rs.intOrNullToLocalDate("is10_sanksjon_tom"),
                        erSanksjonBekreftet = rs.stringOrNull("is10_sanksjon_bekreftet")?.trim(),
                        sanksjonsDager = rs.int("is10_sanksjonsdager"),
                        opphoerFom = rs.intOrNullToLocalDate("is10_stoppdato"),
                        sykemelder = rs.stringOrNull("is10_legenavn")?.trim(),
                        behandlet = rs.intOrNullToLocalDate("is10_behdato"),
                        yrkesskadeArt = rs.stringOrNull("is10_skadeart")?.trim(),
                        skadet = rs.intOrNullToLocalDate("is10_skdato"),
                        vedtatt = rs.intOrNullToLocalDate("is10_skm_mott")
                    )
                }.asList
            )
        }
    }

    data class PeriodeDTO(
        val ident: Long,
        val tknr: String,
        val seq: Int,
        val sykemeldtFom: LocalDate?,
        val sykemeldtTom: LocalDate?,
        val grad: String?,
        val slutt: LocalDate?,
        val erArbeidsgiverPeriode: Boolean,
        val ferie1Fom: LocalDate?,
        val ferie1Tom: LocalDate?,
        val ferie2Fom: LocalDate?,
        val ferie2Tom: LocalDate?,
        val stansAarsak: String?,
        val unntakAktivitet: String?,
        val arbeidsKategori: String,
        val arbeidsKategori99: String?,
        val sanksjonFom: LocalDate?,
        val sanksjonTom: LocalDate?,
        val erSanksjonBekreftet: String?,
        val sanksjonsDager: Int,
        val opphoerFom: LocalDate?,
        val sykemelder: String?,
        val behandlet: LocalDate?,
        val yrkesskadeArt: String?,
        val skadet: LocalDate?,
        val vedtatt: LocalDate?
    ) {
        companion object {
            fun List<PeriodeDTO>.ekstraFerieperioder() = flatMap { it.ferie() }
        }

        fun tilUtbetalingshistorikk(
            utbetalingList: List<Utbetalingshistorikk.Utbetaling>,
            inntektList: List<Utbetalingshistorikk.Inntektsopplysninger>,
            statslønn: Boolean
        ) = Utbetalingshistorikk(
            inntektsopplysninger = inntektList,
            utbetalteSykeperioder = utbetalingList + ferie(),
            maksDato = slutt,
            statslønn = statslønn,
            arbeidsKategoriKode = arbeidsKategori
        )

        private fun ferie() = listOfNotNull(
            feriemapper(ferie1Fom, ferie1Tom),
            feriemapper(ferie2Fom, ferie2Tom)
        )

        private fun feriemapper(fom: LocalDate?, tom: LocalDate?): Utbetalingshistorikk.Utbetaling? =
            if (fom == null || tom == null) null
            else Utbetalingshistorikk.Utbetaling(fom, tom, "", "", fom, 0.0, "9", "Ferie", "0")

        fun tilUtbetalingsperiode(utbetalingList: List<UtbetalingDAO.UtbetalingDTO>) =
            utbetalingList.map { it.tilUtbetalingsperiode(arbeidsKategori) }
    }
}