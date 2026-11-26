# Infotrygd-replikaen

Modulen `infotrygd` leser sykepengedata fra replikaen av Infotrygd i Oracle. Her står det vi vet om tabellene og kolonnene vi bruker, og hvor vi vet det fra. Skjemaet heter `INFOTRYGD_SPQ` i Q1. Tabellene skal være like i prod, men det har vi ikke sjekket.

> **Forbehold:** Dokumentasjonen er satt sammen av Team SAS med hjelp av en kodeagent, ut fra kildene under. Team Infotrygd har ikke kvalitetssikret den, og den kan ha feil. Rett den når du finner noe som ikke stemmer, og oppgi kilden.

Kilder:

- **I**: Informasjonsmodellen fra Team Infotrygd, *IT16_SP MOD*. Den beskriver feltene i `hentSykepengerListe` i infotrygd-facade.
- **S**: Koden i Sparkel (`PeriodeDAO`, `UtbetalingDAO`, `Fnr`, `Datotolker`).
- **T**: Svar fra Team Infotrygd.
- **R**: Spørringer mot replikaen i november 2026.

Kolonner som ikke er forklart, er ikke dokumentert ennå.

## Felles for tabellene `IS_*`

- Datoer lagres som tall på formen `ÅÅÅÅMMDD`. `0` betyr at datoen mangler. Sparkel tolker tall under `10101` som manglende dato. (S)
- `F_NR` er fødselsnummeret på formen `ÅÅMMDD` + personnummer, altså med dag og år byttet om. `T_LOPENR_FNR.PERSONNR` bruker vanlig form, `DDMMÅÅ` + personnummer. (S, R)

## `IS_PERIODE_10`

Én rad per sykmeldingsperiode for en person. Utbetalingene i `IS_UTBETALING_15` og inntektene i `IS_INNTEKT_13` peker på sykmeldingsperioden med `F_NR` og `IS10_ARBUFOER_SEQ`. Tabellen har også helseopplysninger, for eksempel diagnose. Dem leser ikke Sparkel.

| Kolonne | Forklaring | Kilde |
|---|---|---|
| `F_NR` | Fødselsnummeret, se over | S |
| `IS10_ARBUFOER_SEQ` | Identifiserer sykmeldingsperioden sammen med `F_NR`. Verdien er `99999999 − IS10_ARBUFOER`, altså første dag i sykmeldingsperioden sortert baklengs. Det gjaldt alle de 4,46 millioner periodene fra 2024 i Q1 | S, R |
| `IS10_ARBUFOER` | Første dag i sykmeldingsperioden. Brukes som skjæringstidspunkt for alle utbetalingene i perioden. Manglet ikke for noen utbetalte rader i november 2026 | I, R |
| `IS10_ARBUFOER_TOM` | Siste dag i sykmeldingsperioden | I |
| `IS10_UFOEREGRAD` | Sykmeldingsgraden i prosent | I |
| `IS10_MAX` | Maksdatoen | I |
| `IS10_ARBPER` | `J` når arbeidsgiveren betaler de første 16 dagene (arbeidsgiverperioden) | I, S |
| `IS10_FERIE_FOM`, `IS10_FERIE_TOM` | Første ferieperiode | I |
| `IS10_FERIE_FOM2`, `IS10_FERIE_TOM2` | Andre ferieperiode | I |
| `IS10_STANS` | Kode for stansårsak | I |
| `IS10_UNNTAK_AKTIVITET` | Kode for unntak fra aktivitetsplikten | I |
| `IS10_ARBKAT`, `IS10_ARBKAT_99` | Kode for arbeidskategori | I |
| `IS10_SANKSJON_FOM`, `IS10_SANKSJON_TOM` | Periode med planlagt sanksjon, altså stopp i utbetalingen | I |
| `IS10_SANKSJONSDAGER` | Antall dager med sanksjon | I |
| `IS10_SANKSJON_BEKREFTET` | Om sanksjonen er bekreftet | I |
| `IS10_STOPPDATO` | Avslutningsdato. Brukes til å forkorte en periode som er registrert tidligere | I |
| `IS10_LEGENAVN` | Sykmelderen | I |
| `IS10_BEHDATO` | Datoen sykmelderen behandlet personen | I |
| `IS10_SKADEART` | Kode for yrkesskade | I |
| `IS10_SKDATO` | Datoen skaden skjedde | I |
| `IS10_SKM_MOTT` | Datoen skademeldingen ble mottatt | I |
| `IS10_STOENADS_TYPE` | To mellomrom betyr sykepenger. Sparkel leser bare disse | S |
| `IS10_FRISK` | Sparkel hopper over rader med `H`. Betydningen er ikke dokumentert | S |

## `IS_UTBETALING_15`

Én rad per utbetalingsperiode, og rader for ferie, opphold og sanksjon. Radene peker på sykmeldingsperioden med `F_NR` og `IS10_ARBUFOER_SEQ`.

| Kolonne | Forklaring | Kilde |
|---|---|---|
| `F_NR` | Fødselsnummeret, se over | S |
| `IS10_ARBUFOER_SEQ` | Sykmeldingsperioden raden hører til | S |
| `IS15_UTBETFOM_SEQ` | Ser ut til å være `99999999 − IS15_UTBETFOM`. Det er ikke sjekket for alle rader | R |
| `IS15_UTBETFOM`, `IS15_UTBETTOM` | Perioden raden gjelder | I |
| `IS15_UTBETDATO` | Datoen utbetalingen ble gjort. Kan mangle | I |
| `IS15_ARBGIVNR` | Organisasjonsnummeret til arbeidsgiveren. Sparkel bruker det bare når det har 9 siffer | S |
| `IS15_DSATS` | Dagsatsen i kroner, gradert. Det er satsen for hver dag fra mandag til fredag, også helligdager: det årlige grunnlaget delt på 260. Ferieradene vi har sett, har dagsats 0 | I, R |
| `IS15_GRAD` | Utbetalingsgraden i prosent. Tekst, kan være blank | I, S |
| `IS15_OP` | Kode for oppgjørstype | I |
| `IS15_TYPE` | Kode for periodetype, se under | I |
| `IS15_KORR` | Rader med `KORR` er korrigert, og Sparkel hopper over dem | S |
| `IS15_BRUKERID` | Saksbehandleren | I |

Periodetypene (`IS15_TYPE`):

| Kode | Tekst | Mottaker |
|---|---|---|
| `0` | Utbetaling | Personen |
| `1` | Reduksjon medlem | Personen |
| `2` | Etterbetaling, økt lønn | |
| `3` | Etterbetaling, inntekt mangler | |
| `4` | Kontert regnskap | |
| `5` | Refusjon til arbeidsgiver (`IS15-ARB-REF`) | Arbeidsgiveren |
| `6` | Reduksjon refusjon | Arbeidsgiveren |
| `7` | Tilbakeført | |
| `8` | Konvertert | |
| `9` | Ferie eller ferie retur | |
| `O` | Opphold eller opphold retur | |
| `S` | Sanksjon eller sanksjon retur | |

Sparkel gir bare typene 0, 1, 5 og 6, med mindre den som kaller, ber om alle typene. Type 1 og 6 er nesten ikke i bruk: i Q1 var det én rad av hver fra 2020. De har positiv dagsats og overlapper ingen rad av type 0 eller 5, så de regnes som vanlige utbetalinger. (R)

### Utbetalt beløp

Replikaen har ikke beløpet som ble utbetalt. Det regnes ut per rad av type 0, 1, 5 og 6:

> dagsats × (antall dager fra mandag til fredag i `IS15_UTBETFOM`–`IS15_UTBETTOM`, også helligdager − antall dager fra mandag til fredag i ferierader (type 9) for samme sykmeldingsperiode som overlapper raden)

Feriedager er ikke utbetalt. Infotrygd deler vanligvis utbetalingen rundt ferien, men en utbetalingsrad kan også ha en ferierad inni seg. Da er feriedagene trukket fra. (T, R)

Ferieradene har ikke organisasjonsnummer, så ferien gjelder alle utbetalingene i sykmeldingsperioden, uansett arbeidsgiver. I Q1 var det rundt 60 000 ferierader fra 2024, og rundt 270 av dem lå inni en utbetalingsrad. (R)

## Andre tabeller

Ikke dokumentert ennå, utenom det som står her.

| Tabell | Det vi vet |
|---|---|
| `IF_FKONTO_12` | |
| `IF_FORSIKRING_01` | |
| `IF_SMAABEDR_AVREGN_22` | |
| `IF_SMAABEDRIFT_20` | |
| `IF_VEDFRIVT_10` | |
| `IP_MERKNAD_20` | |
| `IP_PERSONKORT_90` | |
| `IS_DIVERSE_11` | |
| `IS_INNTEKT_13` | Inntektene i sykmeldingsperioden, per arbeidsgiver. `IS13_LOENN` er inntekten per periode, og `IS13_PERIODE` er perioden: `D` daglig, `U` ukentlig, `F` 14-daglig, `M` månedlig og `Å` årlig (I, S) |
| `IS_PERSON_01` | |
| `IS_TEKST_14` | |
| `KT_KODETEKST_01` | Tekster for koder i Infotrygd, gruppert i `KT1_TEKSTTYPE`, for eksempel ytelser, land, tiltak og saksstatuser. Har ikke kodene i `IS_UTBETALING_15`, som periodetype og oppgjørstype. Tekstene for dem ligger i Infotrygd-programmene (I, R) |
| `LS_LEGE_01` | |
| `LS_SYKEMELDING_10` | |
| `SA_HENDELSE_20` | |
| `SA_SAK_10` | |
| `SA_STATUS_15` | |
| `T_BELOPSTYPE` | Kodeverk |
| `T_BEREGNINGSTYPE` | Kodeverk |
| `T_BESLUT` | |
| `T_DELYTELSE` | Trolig linjene i oppdraget, med `LINJE_ID`. Sykepenger har `TYPE_DELYTELSE = 'SP'` og `TYPE_SATS = 'D'`, og da er `BELOP` dagsatsen. En linje kan dekke flere utbetalingsrader i `IS_UTBETALING_15`. Sparkel bruker tabellen til feriepenger (R, S) |
| `T_DELYTELSE_LOGG` | |
| `T_DELYTELSE_SP_FA_BS` | Tilleggsopplysninger til `T_DELYTELSE`, for eksempel grad og organisasjonsnummer (S) |
| `T_DELYTELSESTYPE` | Kodeverk |
| `T_KJOREPLAN_AVST` | |
| `T_KLASSEKODE_FERIEPENGER` | Kodeverk |
| `T_KLASSEKODE_MANUELL` | Kodeverk |
| `T_LOGG_AVVIK` | |
| `T_LOPENR_FNR` | Kobler `PERSON_LOPENR` til fødselsnummeret (`PERSONNR`, vanlig form) (S, R) |
| `T_MAP_DELYTELSE` | |
| `T_OMREGNING` | |
| `T_STONAD` | Stønadene til en person, med `KODE_RUTINE`, for eksempel `SP` og `SR` for sykepenger og `FY` for feriepenger (S, R) |
| `T_TEKSTU` | |
| `T_VEDTAK` | Vedtakene for en stønad (S) |
| `T_VEDTAK_SP_FA_BS` | |
