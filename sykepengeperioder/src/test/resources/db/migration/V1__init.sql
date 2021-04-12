create table T_DELYTELSE
(
    VEDTAK_ID       NUMBER           not null,
    TYPE_DELYTELSE  VARCHAR2(2 char) not null,
    TIDSPUNKT_REG   TIMESTAMP(6) default current_timestamp not null,
    FOM             DATE             not null,
    TOM             DATE,
    BELOP           NUMBER(11, 2)    not null,
    OPPGJORSORDNING CHAR(1 char),
    MOTTAKER_LOPENR NUMBER,
    BRUKERID        VARCHAR2(8 char) not null,
    TYPE_SATS       CHAR(1 char)     not null,
    TYPE_UTBETALING CHAR(1 char)     not null,
    LINJE_ID        NUMBER,
    OPPRETTET       TIMESTAMP(6) default current_timestamp,
    OPPDATERT       TIMESTAMP(6) default current_timestamp,
    DB_SPLITT       CHAR(2 char) default '  ',
    constraint PK_DELYTELSE
        primary key (VEDTAK_ID, TYPE_DELYTELSE, TIDSPUNKT_REG)
);



create table T_DELYTELSE_SP_FA_BS
(
    VEDTAK_ID        NUMBER            not null,
    TYPE_DELYTELSE   VARCHAR2(2 char)  not null,
    TIDSPUNKT_REG    TIMESTAMP(6) default current_timestamp not null,
    TYPE_INNTEKT     VARCHAR2(2 char)  not null,
    TYPE_TILTAK      VARCHAR2(2 char)  not null,
    TYPE_FORSIKRING  CHAR(1 char)      not null,
    PERIODE_KARENS   CHAR(1 char)      not null,
    PROSENT_INNT_GRL NUMBER            not null,
    ORGNR            VARCHAR2(9 char)  not null,
    REFUSJON         CHAR(1 char)      not null,
    GRAD             NUMBER            not null,
    DATO_MAX         DATE,
    KODE_KLASSE      VARCHAR2(20 char) not null,
    SATSENDRING      CHAR(1 char)      not null,
    DATO_ANNULLERT   DATE,
    SJOMANN          CHAR(1 char)      not null,
    TYPE_SATS        VARCHAR2(4 char)  not null,
    SATS_DAGER       NUMBER(7, 2)      not null,
    BRUKERID         VARCHAR2(8 char)  not null,
    OPPRETTET        TIMESTAMP(6) default current_timestamp,
    OPPDATERT        TIMESTAMP(6) default current_timestamp,
    DB_SPLITT        CHAR(2 char) default '  ',
    constraint PK_DELYT_SFB
        primary key (VEDTAK_ID, TYPE_DELYTELSE, TIDSPUNKT_REG)
)
;



create table T_FRA_VL_HENDELSE
(
    TYPE_HENDELSE             VARCHAR2(26 char) not null,
    TYPE_YTELSE_FRA_VL        VARCHAR2(4 char)  not null,
    SEKVENSID                 NUMBER            not null,
    AKTOERID                  VARCHAR2(20 char) not null,
    FOERSTESTOENADSDAG        DATE              not null,
    SISTESTOENADSDAG          DATE,
    GSAKID                    VARCHAR2(30 char),
    OPPRETTETDATO             DATE              not null,
    FNR                       VARCHAR2(11 char) not null,
    SP_FORBR_STOENADSDAGER    NUMBER,
    BS_AKTORID_PLEIETRENGENDE VARCHAR2(20 char),
    BS_FNR_PLEIETRENGENDE     VARCHAR2(20 char),
    BRUKERID                  VARCHAR2(8 char)  not null,
    TIDSPUNKT_REG             TIMESTAMP(6)      not null,
    OPPRETTET                 TIMESTAMP(6) default current_timestamp,
    OPPDATERT                 TIMESTAMP(6) default current_timestamp,
    DB_SPLITT                 CHAR(2 char) default '  ',
    --  CONSTRAINT PK_FRA_VL_HENDELSE PRIMARY KEY (TYPE_HENDELSE, TYPE_YTELSE_FRA_VL, SEKVENSID, AKTOERID, FOERSTESTOENADSDAG, SISTESTOENADSDAG, GSAKID, OPPRETTETDATO, FNR, BRUKERID, TIDSPUNKT_REG) --IFVLH01U,
    constraint PK_FRA_VL_HENDELSE
        primary key (TYPE_HENDELSE, TYPE_YTELSE_FRA_VL, SEKVENSID, AKTOERID, FOERSTESTOENADSDAG, OPPRETTETDATO, FNR,
                     BRUKERID, TIDSPUNKT_REG)
)
;



create table T_INNTEKT
(
    STONAD_ID     NUMBER           not null,
    ORGNR         NUMBER           not null,
    INNTEKT_FOM   DATE             not null,
    LOPENR        NUMBER           not null,
    INNTEKT_TOM   DATE,
    TYPE_INNTEKT  CHAR(1 char)     not null,
    INNTEKT       NUMBER(13, 2)    not null,
    PERIODE       CHAR(1 char)     not null,
    REFUSJON      CHAR(1 char)     not null,
    REFUSJON_TOM  DATE,
    STATUS        CHAR(1 char)     not null,
    BRUKERID      VARCHAR2(8 char) not null,
    TIDSPUNKT_REG TIMESTAMP(6)     not null,
    OPPRETTET     TIMESTAMP(6) default current_timestamp,
    OPPDATERT     TIMESTAMP(6) default current_timestamp,
    DB_SPLITT     CHAR(2 char) default 'BS',
    constraint PK_INNTEKT
        primary key (STONAD_ID, ORGNR, INNTEKT_FOM, LOPENR)
)
;



create table T_LOPENR_FNR
(
    PERSON_LOPENR NUMBER            not null
        constraint PK_LOPENR_FNR
            primary key,
    PERSONNR      VARCHAR2(11 char) not null,
    OPPRETTET     TIMESTAMP(6) default current_timestamp,
    OPPDATERT     TIMESTAMP(6) default current_timestamp,
    DB_SPLITT     CHAR(2 char) default '  '
)
;



create table T_STONAD
(
    STONAD_ID         NUMBER       not null
        constraint PK_STONAD
            primary key,
    PERSON_LOPENR     NUMBER       not null,
    KODE_RUTINE       CHAR(2 char) not null,
    DATO_START        DATE         not null,
    KODE_OPPHOR       VARCHAR2(2 char),
    DATO_OPPHOR       DATE,
    OPPDRAG_ID        NUMBER,
    TIDSPUNKT_OPPHORT TIMESTAMP(6),
    TIDSPUNKT_REG     TIMESTAMP(6),
    BRUKERID          VARCHAR2(8 char),
    OPPRETTET         TIMESTAMP(6) default current_timestamp,
    OPPDATERT         TIMESTAMP(6) default current_timestamp,
    DB_SPLITT         CHAR(2 char) default '  '
)
;



create table T_STONAD_BS
(
    STONAD_ID      NUMBER           not null,
    UNNTAK         VARCHAR2(2 char) not null,
    PLEIEPENGEGRAD NUMBER,
    LOPENR_BARN    NUMBER           not null,
    BRUKERID       VARCHAR2(8 char) not null,
    TIDSPUNKT_REG  TIMESTAMP(6)     not null,
    OPPRETTET      TIMESTAMP(6) default current_timestamp,
    OPPDATERT      TIMESTAMP(6) default current_timestamp,
    DB_SPLITT      CHAR(2 char) default 'BS',
    constraint PK_STONAD_BS
        primary key (STONAD_ID, LOPENR_BARN)
)
;



create table T_SU
(
    VEDTAK_ID          NUMBER           not null,
    TIDSPUNKT_REG      TIMESTAMP(6)     not null,
    BELOP_BER_GRUNNLAG NUMBER(11, 2)    not null,
    BRUKERID           VARCHAR2(8 char) not null,
    REVURDERING_DATO   DATE,
    OPPRETTET          TIMESTAMP(6) default current_timestamp,
    OPPDATERT          TIMESTAMP(6) default current_timestamp,
    DB_SPLITT          CHAR(2 char) default 'SU',
    --  CONSTRAINT PK_SU PRIMARY KEY (VEDTAK_ID, TIDSPUNKT_REG) --ISTOT01U,
    constraint PK_SU
        primary key (VEDTAK_ID, TIDSPUNKT_REG)
)
;

create table T_VEDTAK
(
    VEDTAK_ID           NUMBER           not null
        constraint PK_VEDTAK
            primary key,
    PERSON_LOPENR       NUMBER           not null,
    KODE_RUTINE         CHAR(2 char)     not null,
    DATO_START          DATE             not null,
    TKNR                VARCHAR2(4 char) not null,
    SAKSBLOKK           CHAR(1 char)     not null,
    SAKSNR              NUMBER           not null,
    TYPE_SAK            VARCHAR2(2 char) not null,
    KODE_RESULTAT       VARCHAR2(2 char) not null,
    DATO_INNV_FOM       DATE             not null,
    DATO_INNV_TOM       DATE,
    DATO_MOTTATT_SAK    DATE             not null,
    KODE_VEDTAKSNIVAA   VARCHAR2(3 char) not null,
    TYPE_BEREGNING      VARCHAR2(3 char) not null,
    TKNR_BEH            VARCHAR2(4 char) not null,
    TIDSPUNKT_REG       TIMESTAMP(6) default current_timestamp not null,
    BRUKERID            VARCHAR2(8 char) not null,
    NOKKEL_DL1          VARCHAR2(30 char),
    ALTERNATIV_MOTTAKER NUMBER(11),
    STONAD_ID           NUMBER           not null,
    KIDNR               VARCHAR2(25 char),
    FAKTNR              VARCHAR2(33 char),
    OPPRETTET           TIMESTAMP(6) default current_timestamp,
    OPPDATERT           TIMESTAMP(6) default current_timestamp,
    DB_SPLITT           CHAR(2 char) default '  '
)
;

create table T_VEDTAK_SP_FA_BS
(
    VEDTAK_ID          NUMBER           not null
        constraint PK_VEDTAK_SFB
            primary key,
    ARBKAT             VARCHAR2(2 char) not null,
    KODE_FORELDREKVOTE CHAR(1 char)     not null,
    DEKNINGSGRAD       NUMBER           not null,
    DATO_FODSEL        DATE,
    DATO_ADOPSJON      DATE,
    ANT_BARN           NUMBER,
    ORGNR_JURIDISK     VARCHAR2(9 char),
    DATO_OPPHOR_FOM    DATE,
    PLEIEPENGEGRAD     NUMBER,
    BRUKERID           VARCHAR2(8 char) not null,
    TIDSPUNKT_REG      TIMESTAMP(6)     not null,
    OPPRETTET          TIMESTAMP(6) default current_timestamp,
    OPPDATERT          TIMESTAMP(6) default current_timestamp,
    DB_SPLITT          CHAR(2 char) default '  '
)
;



create table IS_PERSON_01
(
    IS01_PERSONKEY     NUMBER(15)                                  not null,
    IS01_RAADLEGE_DATO NUMBER(8)                                   not null,
    IS01_RAADLEGE_TYPE CHAR(1 char)                                not null,
    IS01_ARBUFOER      NUMBER(8)                                   not null,
    TK_NR              VARCHAR2(4)                                 not null,
    F_NR               VARCHAR2(11)                                not null,
    OPPRETTET          TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE     TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS           VARCHAR2(12 char) default ' '               not null,
    REGION             CHAR(1 char)      default ' '               not null,
    ID_PERS            NUMBER
        constraint PK_IS_PERSON
            primary key,
    OPPDATERT          TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT          CHAR(2 char)      default '99'
)
;

create table IS_PERIODE_10
(
    IS01_PERSONKEY             NUMBER(15)   default 0                 not null,
    IS10_ARBUFOER_SEQ          NUMBER(8)                              not null,
    IS10_ARBUFOER              NUMBER(8)                              not null,
    IS10_ARBKAT                CHAR(2 char)                           not null,
    IS10_STAT                  CHAR(1 char)                           not null,
    IS10_ARBUFOER_TOM          NUMBER(8)                              not null,
    IS10_ARBUFOER_OPPR         NUMBER(8)                              not null,
    IS10_UFOEREGRAD            VARCHAR2(3 char)                       not null,
    IS10_REVURDERT_DATO        NUMBER(8),
    IS10_AARSAK_FORSKYV2       CHAR(2 char),
    IS10_STOENADS_TYPE         CHAR(2 char),
    IS10_REG_DATO_SMII         NUMBER(8),
    IS10_ENDR_DATO_STAT        NUMBER(8),
    IS10_VENTETID              CHAR(1 char),
    IS10_INSTOPPH_FOM          VARCHAR2(8 char),
    IS10_INSTOPPH_TOM          VARCHAR2(8 char),
    IS10_BEHDATO               NUMBER(8),
    IS10_KODE_LEGE_INST        CHAR(1 char),
    IS10_LEGENR                VARCHAR2(11 char),
    IS10_LEGENAVN              VARCHAR2(25 char),
    IS10_SYKM_II_DATO          NUMBER(8),
    IS10_PROGNOSEGRUPPE        CHAR(1 char),
    IS10_UNNTAK                CHAR(1 char),
    IS10_UKER_VENTETID         VARCHAR2(3 char),
    IS10_BEHANDLING            CHAR(1 char),
    IS10_MELDING_STAT          NUMBER(8),
    IS10_SKADEART              CHAR(1 char),
    IS10_SKDATO                NUMBER(8),
    IS10_SKM_MOTT              NUMBER(8),
    IS10_Y_GODKJ               CHAR(1 char),
    IS10_FERIE_FOM             NUMBER(8),
    IS10_FERIE_TOM             NUMBER(8),
    IS10_FDATO                 NUMBER(8),
    IS10_ANT_BARN              CHAR(1 char),
    IS10_MORFNR                NUMBER(11),
    IS10_ARBPER                CHAR(1 char),
    IS10_YRKE                  VARCHAR2(20 char),
    IS10_S_INNT_DATO           NUMBER(8),
    IS10_SPATEST               NUMBER(8),
    IS10_STANS                 VARCHAR2(8 char),
    IS10_FRISK                 CHAR(1 char),
    IS10_REGDAT_FRISK          NUMBER(8),
    IS10_UTBET_FOM             NUMBER(8),
    IS10_UTBET_TOM             NUMBER(8),
    IS10_S_GRAD                NUMBER(3),
    IS10_FRIB_OPPR_SALD        NUMBER(5),
    IS10_FRIB_SALDO            NUMBER(7),
    IS10_MAX                   NUMBER(8),
    IS10_TIDSYK                NUMBER(3),
    IS10_TIDSYK_KODE           CHAR(1 char),
    IS10_SAKSBLOKK             CHAR(1 char),
    IS10_GRUPPE                CHAR(2 char),
    IS10_BRUKERID              VARCHAR2(7 char),
    IS10_SAK_FRAMLEGG          CHAR(1 char),
    IS10_FERIEDAGER_PERIODE    NUMBER(3),
    IS10_FERIEDAGER_PLANLAGT   NUMBER(3),
    IS10_STOENAD_OP_PB         CHAR(1 char),
    IS10_STOENAD_OM_SV         CHAR(1 char),
    IS10_INNT_RED_6G           NUMBER(7),
    IS10_SAK_TYPE_O_S          CHAR(1 char),
    IS10_SAK_PROGNOSEGRP       CHAR(1 char),
    IS10_DEKNINGSGRAD          NUMBER(3),
    IS10_ANT_STOENADSDAGER     NUMBER(3),
    IS10_TIDL_UTBET            NUMBER(8),
    IS10_TIDL_UTBET_K          CHAR(1 char),
    IS10_PROS_INNTEKT_GR       VARCHAR2(3 char),
    IS10_ANT_BARN_U_12AAR      CHAR(2 char),
    IS10_ALENEOMSORG           CHAR(1 char),
    IS10_ADOPSJONS_DATO        VARCHAR2(8 char),
    IS10_RETT_TIL_FEDREKVOTE   CHAR(1 char),
    IS10_FEDREKVOTE            CHAR(1 char),
    IS10_FEDREKVOTE_TOM        VARCHAR2(8 char),
    IS10_TIDSYK_OP_PB          NUMBER(5),
    IS10_EGENOPPL              CHAR(1 char),
    IS10_ANTATT_SYKM_TOM       VARCHAR2(8 char),
    IS10_VEDTAK_12_UKER        VARCHAR2(8 char),
    IS10_UNNTAK_BS             CHAR(2 char),
    IS10_REG_DATO              VARCHAR2(8 char),
    IS10_STILLINGSANDEL_MOR    VARCHAR2(3 char),
    IS10_TIDSK_TYPE            CHAR(2 char),
    IS10_TIDSK_BARNFNR         VARCHAR2(11 char),
    IS10_MAXTIDSK              VARCHAR2(8 char),
    IS10_SAMMENFALENDE_PERIODE CHAR(1 char),
    IS10_SAMMENF_DAGER_MASK    VARCHAR2(3 char),
    IS10_DIAGNOSE_KODE_1       CHAR(1 char),
    IS10_DIAGNOSEGRUPPE        VARCHAR2(6 char),
    IS10_DIAGNOSE              VARCHAR2(70 char),
    IS10_DIAGNOSE_KODE_2       CHAR(1 char),
    IS10_DIAGNOSEGRUPPE_2      VARCHAR2(6 char),
    IS10_DIAGNOSE_2            VARCHAR2(70 char),
    IS10_TERMIN_DATO           VARCHAR2(8 char),
    IS10_KJOEP_HELSETJ         CHAR(2 char),
    IS10_HELSETJ_SENDT         VARCHAR2(8 char),
    IS10_UTREDET_OPERERT       CHAR(1 char),
    IS10_UTREDET_OPERERT_DATO  VARCHAR2(8 char),
    IS10_REG_DATO_HELSETJ      VARCHAR2(8 char),
    IS10_SAMMENHENG_ARB_SIT    CHAR(1 char),
    IS10_ARBEIDSTID_MOR        VARCHAR2(3 char),
    IS10_SITUASJON_MOR         CHAR(1 char),
    IS10_RETTIGHET_MOR         CHAR(1 char),
    IS10_OPPHOLD_FOM           NUMBER(8),
    IS10_OPPHOLD_TOM           NUMBER(8),
    IS10_DEL2_TYPE             CHAR(1 char),
    IS10_DEL2_REGDATO_J        NUMBER(8),
    IS10_DEL2_REGDATO_U        NUMBER(8),
    IS10_DEL2_DATO             NUMBER(8),
    IS10_FRISKM_DATO           NUMBER(8),
    IS10_SVANGER_SYK           CHAR(1 char),
    IS10_PAALOGG_ID            VARCHAR2(7 char),
    IS10_UNNTAK_AKTIVITET      CHAR(1 char),
    IS10_OPPFOLGING_DATO       NUMBER(8),
    IS10_K68_DATO              NUMBER(8),
    IS10_K69_DATO              NUMBER(8),
    IS10_EOS                   CHAR(1 char),
    IS10_ENGANG                CHAR(1 char),
    IS10_HALV_HEL              CHAR(1 char),
    IS10_K28_DATO              NUMBER(8),
    IS10_AARSAK_FORSKYV        CHAR(2 char),
    IS10_STEBARNSADOPSJON      CHAR(1 char),
    IS10_SURROGATMOR           CHAR(1 char),
    IS10_DIALOG1_DATO          NUMBER(8),
    IS10_DIALOG1_KODE          CHAR(1 char),
    IS10_DIALOG2_DATO          NUMBER(8),
    IS10_DIALOG2_KODE          CHAR(1 char),
    IS10_OPPFOLGING_KODE       CHAR(1 char),
    IS10_K69A_DATO             NUMBER(8),
    IS10_POLIKL_BEH            VARCHAR2(8 char),
    IS10_ARENA_F234            CHAR(1 char),
    IS10_AVVENT_SYKMELD        CHAR(1 char),
    IS10_AVVENT_TOM            NUMBER(8),
    IS10_ARENA_F226            CHAR(1 char),
    IS10_ARBKAT_99             CHAR(2 char),
    IS10_PB_BEKREFT            CHAR(1 char),
    IS10_SANKSJON_FOM          NUMBER(8),
    IS10_SANKSJON_TOM          NUMBER(8),
    IS10_SANKSJONSDAGER        NUMBER(3),
    IS10_SANKSJON_BEKREFTET    CHAR(1 char),
    IS10_RETT_TIL_MODREKVOTE   CHAR(1 char),
    IS10_MODREKVOTE            CHAR(1 char),
    IS10_MODREKVOTE_TOM        VARCHAR2(8 char),
    IS10_FERIE_FOM2            NUMBER(8),
    IS10_FERIE_TOM2            NUMBER(8),
    IS10_STOPPDATO             NUMBER(8),
    TK_NR                      VARCHAR2(4)                            not null,
    F_NR                       VARCHAR2(11)                           not null,
    OPPRETTET                  TIMESTAMP(6) default current_timestamp not null,
    ENDRET_I_KILDE             TIMESTAMP(6) default current_timestamp not null,
    KILDE_IS                   VARCHAR2(12) default ' '               not null,
    REGION                     CHAR(1 char) default ' '               not null,
    ID_PERI10                  NUMBER
        constraint PK_IS_PERIODE_10
            primary key,
    OPPDATERT                  TIMESTAMP(6) default current_timestamp,
    DB_SPLITT                  CHAR(2 char) default '  '
)
;


create table IS_DIVERSE_11
(
    IS01_PERSONKEY      NUMBER(15)                                  not null,
    IS10_ARBUFOER_SEQ   NUMBER(8)                                   not null,
    IS11_STLONN         NUMBER(7)                                   not null,
    IS11_NATURAL        NUMBER(5, 2)                                not null,
    IS11_UTB_NAT        CHAR(1 char)                                not null,
    IS11_OPPH_NAT       NUMBER(8)                                   not null,
    IS11_REDUKSJ_BELOEP NUMBER(5, 2)                                not null,
    IS11_REDUKSJ_TYPE   CHAR(1 char)                                not null,
    IS11_REDUKSJ_FOM    NUMBER(8)                                   not null,
    IS11_REDUKSJ_TOM    NUMBER(8)                                   not null,
    TK_NR               VARCHAR2(4)                                 not null,
    F_NR                VARCHAR2(11)                                not null,
    OPPRETTET           TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS            VARCHAR2(12 char) default ' '               not null,
    REGION              CHAR(1 char)      default ' '               not null,
    ID_DIV              NUMBER
        constraint PK_IS_DIVERSE
            primary key,
    OPPDATERT           TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT           CHAR(2 char)      default '  '
)
;


create table IS_HISTORIKK_12
(
    IS01_PERSONKEY                 NUMBER(15)                             not null,
    IS10_ARBUFOER_SEQ              NUMBER(8)                              not null,
    IS12_SEGM_TYPE                 CHAR(1 char)                           not null,
    IS12_REG_DATO_SEQ              NUMBER(8)                              not null,
    IS12_KLOKKESLETT_SEQ           VARCHAR2(6 char)                       not null,
    IS12_REG_DATO                  NUMBER(8)                              not null,
    IS12_KLOKKESLETT               VARCHAR2(6 char)                       not null,
    IS12_REG_ENDER_IS10_DATO       NUMBER(8)                              not null,
    IS12_UFOERE_FOM                NUMBER(8),
    IS12_UFOERE_TOM                NUMBER(8),
    IS12_UFOEREGRAD                VARCHAR2(3 char),
    IS12_UFOEREGRAD_TYPE           CHAR(1 char),
    IS12_HIST_UFOER_BRUKERID       VARCHAR2(7 char),
    IS12_KODE_LEGE_INST            CHAR(1 char),
    IS12_LEGENR                    VARCHAR2(11 char),
    IS12_LEGENAVN                  VARCHAR2(25 char),
    IS12_LEGE_FNR                  VARCHAR2(11 char),
    IS12_LEGE_FOM                  NUMBER(8),
    IS12_LEGE_TOM                  NUMBER(8),
    IS12_HIST_LEGE_BRUKERID        VARCHAR2(7 char),
    IS12_DIAGNOSE_KODE_1           CHAR(1 char),
    IS12_DIAGNOSEGRUPPE_1          VARCHAR2(6 char),
    IS12_DIAGNOSE_1                VARCHAR2(70 char),
    IS12_DIAGNOSE_KODE_2           CHAR(1 char),
    IS12_DIAGNOSEGRUPPE_2          VARCHAR2(6 char),
    IS12_DIAGNOSE_2                VARCHAR2(70 char),
    IS12_HIST_DIAGNOSE_BRUKERID    VARCHAR2(7 char),
    IS12_SMII_REG_DATO             NUMBER(8),
    IS12_SMII_PROGNOSEGR           CHAR(1 char),
    IS12_SMII_UNNTAK               CHAR(1 char),
    IS12_SMII_DATO                 NUMBER(8),
    IS12_HIST_SMII_BRUKERID        VARCHAR2(7 char),
    IS12_UNNTAK_KODE               CHAR(1 char),
    IS12_UNNTAK_BRUKERID           VARCHAR2(7 char),
    IS12_DIAGNOSE_KODE_BI          CHAR(1 char),
    IS12_DIAGNOSEGRUPPE_BI         VARCHAR2(6 char),
    IS12_DIAGNOSE_BI               VARCHAR2(70 char),
    IS12_HIST_DIAGNOSE_BI_BRUKERID VARCHAR2(7 char),
    IS12_KJOEP_HELSETJ             CHAR(2 char),
    IS12_HELSETJ_SENDT             VARCHAR2(8 char),
    IS12_UTREDET_OPERERT           CHAR(1 char),
    IS12_UTREDET_OPERERT_DATO      VARCHAR2(8 char),
    IS12_REG_DATO_HELSETJ          VARCHAR2(8 char),
    IS12_HIST_HELSE_TJ_BRUKERID    VARCHAR2(7 char),
    IS12_DEL2_TYPE                 CHAR(1 char),
    IS12_DEL2_REGDATO_J            VARCHAR2(8 char),
    IS12_DEL2_REGDATO_U            VARCHAR2(8 char),
    IS12_DEL2_DATO                 VARCHAR2(8 char),
    IS12_HIST_DEL2_BRUKERID        VARCHAR2(7 char),
    IS12_TILTAK_FOM                NUMBER(8),
    IS12_TILTAK_TOM                NUMBER(8),
    IS12_FORL_PERIODE_TOM          NUMBER(8),
    IS12_TILTAK_TYPE               CHAR(2 char),
    IS12_SVANGER_SYK               CHAR(1 char),
    IS12_HIST_SVANGER_BRUKERID     VARCHAR2(7 char),
    IS12_BRUKERID                  VARCHAR2(7 char),
    IS12_OPPFLG_DIALG_DATO         NUMBER(8),
    IS12_OPPFLG_DIALG_KODE         CHAR(1 char),
    IS12_OPPFLG_DIALG_BRUKERID     VARCHAR2(7 char),
    TK_NR                          VARCHAR2(4)                            not null,
    F_NR                           VARCHAR2(11)                           not null,
    OPPRETTET                      TIMESTAMP(6) default current_timestamp not null,
    ENDRET_I_KILDE                 TIMESTAMP(6) default current_timestamp not null,
    KILDE_IS                       VARCHAR2(12) default ''                not null,
    REGION                         CHAR(1 char) default ''                not null,
    ID_HIST                        NUMBER
        constraint PK_IS_HIST
            primary key,
    OPPDATERT                      TIMESTAMP(6) default current_timestamp,
    DB_SPLITT                      CHAR(2 char) default 'SP'
)
;


create table IS_INNTEKT_13
(
    IS01_PERSONKEY      NUMBER(15)                             not null,
    IS10_ARBUFOER_SEQ   NUMBER(8)                              not null,
    IS13_SPFOM          NUMBER(8)                              not null,
    IS13_ARBGIVNR       NUMBER(11)                             not null,
    IS13_LOENN          NUMBER(11, 2)                          not null,
    IS13_PERIODE        CHAR(1 char)                           not null,
    IS13_REF            CHAR(1 char)                           not null,
    IS13_REF_TOM        NUMBER(8)                              not null,
    IS13_GML_SPFOM      NUMBER(8)                              not null,
    IS13_GML_LOENN      NUMBER(11, 2)                          not null,
    IS13_GML_PERIODE    CHAR(1 char)                           not null,
    IS13_PO_INNT        CHAR(1 char)                           not null,
    IS13_UTBET          CHAR(1 char)                           not null,
    IS13_TIDSKONTO_KODE CHAR(1 char)                           not null,
    TK_NR               VARCHAR2(4)                            not null,
    F_NR                VARCHAR2(11)                           not null,
    OPPRETTET           TIMESTAMP(6) default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6) default current_timestamp not null,
    KILDE_IS            VARCHAR2(12) default ' '               not null,
    REGION              CHAR(1 char) default ' '               not null,
    ID_INNT             NUMBER
        constraint PK_IS_INNTEKT
            primary key,
    OPPDATERT           TIMESTAMP(6) default current_timestamp,
    DB_SPLITT           CHAR(2 char) default '  '
)
;


create table IS_UTBETALING_15
(
    IS01_PERSONKEY      NUMBER(15)                              not null,
    IS10_ARBUFOER_SEQ   NUMBER(8)                               not null,
    IS15_UTBETFOM_SEQ   NUMBER(8)                               not null,
    IS15_UTBETFOM       NUMBER(8)                               not null,
    IS15_UTBETTOM       NUMBER(8)                               not null,
    IS15_UTBETDATO      NUMBER(8)                               not null,
    IS15_ARBGIVNR       NUMBER(11)                              not null,
    IS15_BILAG          NUMBER(7)                               not null,
    IS15_DSATS          NUMBER(9, 2)                            not null,
    IS15_GRAD           CHAR(3 char)                            not null,
    IS15_OP             CHAR(2 char)                            not null,
    IS15_TYPE           CHAR(1 char)                            not null,
    IS15_TILB_UTBETDATO NUMBER(8)                               not null,
    IS15_TILB_BILAG     NUMBER(7)                               not null,
    IS15_TILB_OP        CHAR(2 char)                            not null,
    IS15_TIDSKONTO_KODE CHAR(1 char)                            not null,
    IS15_BRUKERID       CHAR(7 char)                            not null,
    IS15_REGDATO_BATCH  NUMBER(8)                               not null,
    IS15_TILTAK_TYPE    CHAR(2 char)                            not null,
    IS15_KORR           CHAR(4 char)                            not null,
    IS15_AARSAK_FORSKYV CHAR(2 char)                            not null,
    IS15_BEREGNET_I_OS  CHAR(1 char)                            not null,
    TK_NR               CHAR(4)                                 not null,
    F_NR                CHAR(11)                                not null,
    OPPRETTET           TIMESTAMP(6)  default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6)  default current_timestamp not null,
    KILDE_IS            CHAR(12 char) default ' '               not null,
    REGION              CHAR(1 char)  default ' '               not null,
    ID_UTBT             NUMBER
        constraint PK_IS_UTBET
            primary key,
    OPPDATERT           TIMESTAMP(6)  default current_timestamp,
    DB_SPLITT           CHAR(2 char)  default 'SP'
)
;

create table IS_PERIODE_17
(
    IS01_PERSONKEY             NUMBER(15)                                  not null,
    IS17_ARBUFOER_SEQ          NUMBER(8)                                   not null,
    IS17_ARBUFOER              NUMBER(8)                                   not null,
    IS17_ARBKAT                CHAR(2 char)                                not null,
    IS17_STAT                  CHAR(1 char)                                not null,
    IS17_ARBUFOER_TOM          NUMBER(8)                                   not null,
    IS17_ARBUFOER_OPPR         NUMBER(8)                                   not null,
    IS17_UFOEREGRAD            VARCHAR2(3 char)                            not null,
    IS17_REVURDERT_DATO        NUMBER(8)                                   not null,
    IS17_STOENADS_TYPE         CHAR(2 char)                                not null,
    IS17_REG_DATO_SMII         NUMBER(8)                                   not null,
    IS17_ENDR_DATO_STAT        NUMBER(8)                                   not null,
    IS17_VENTETID              CHAR(1 char)                                not null,
    IS17_INSTOPPH_FOM          VARCHAR2(8 char)                            not null,
    IS17_INSTOPPH_TOM          VARCHAR2(8 char)                            not null,
    IS17_BEHDATO               NUMBER(8)                                   not null,
    IS17_KODE_LEGE_INST        CHAR(1 char)                                not null,
    IS17_LEGENR                VARCHAR2(11 char)                           not null,
    IS17_LEGENAVN              VARCHAR2(25 char)                           not null,
    IS17_SYKM_II_DATO          NUMBER(8)                                   not null,
    IS17_PROGNOSEGRUPPE        CHAR(1 char)                                not null,
    IS17_UNNTAK                CHAR(1 char)                                not null,
    IS17_UKER_VENTETID         VARCHAR2(3 char)                            not null,
    IS17_BEHANDLING            CHAR(1 char)                                not null,
    IS17_MELDING_STAT          NUMBER(8)                                   not null,
    IS17_SKADEART              CHAR(1 char)                                not null,
    IS17_SKDATO                NUMBER(8)                                   not null,
    IS17_SKM_MOTT              NUMBER(8)                                   not null,
    IS17_Y_GODKJ               CHAR(1 char)                                not null,
    IS17_FERIE_FOM             NUMBER(8)                                   not null,
    IS17_FERIE_TOM             NUMBER(8)                                   not null,
    IS17_FDATO                 NUMBER(8)                                   not null,
    IS17_ANT_BARN              CHAR(1 char)                                not null,
    IS17_MORFNR                NUMBER(11)                                  not null,
    IS17_ARBPER                CHAR(1 char)                                not null,
    IS17_YRKE                  VARCHAR2(20 char)                           not null,
    IS17_S_INNT_DATO           NUMBER(8)                                   not null,
    IS17_SPATEST               NUMBER(8)                                   not null,
    IS17_STANS                 VARCHAR2(8 char)                            not null,
    IS17_FRISK                 CHAR(1 char)                                not null,
    IS17_REGDAT_FRISK          NUMBER(8)                                   not null,
    IS17_UTBET_FOM             NUMBER(8)                                   not null,
    IS17_UTBET_TOM             NUMBER(8)                                   not null,
    IS17_S_GRAD                NUMBER(3)                                   not null,
    IS17_FRIB_OPPR_SALD        NUMBER(5)                                   not null,
    IS17_FRIB_SALDO            NUMBER(7)                                   not null,
    IS17_MAX                   NUMBER(8)                                   not null,
    IS17_TIDSYK                NUMBER(3)                                   not null,
    IS17_TIDSYK_KODE           CHAR(1 char)                                not null,
    IS17_SAKSBLOKK             CHAR(1 char)                                not null,
    IS17_GRUPPE                CHAR(2 char)                                not null,
    IS17_BRUKERID              VARCHAR2(7 char)                            not null,
    IS17_SAK_FRAMLEGG          CHAR(1 char)                                not null,
    IS17_FERIEDAGER_PERIODE    NUMBER(3)                                   not null,
    IS17_FERIEDAGER_PLANLAGT   NUMBER(3)                                   not null,
    IS17_STOENAD_OP_PB         CHAR(1 char)                                not null,
    IS17_STOENAD_OM_SV         CHAR(1 char)                                not null,
    IS17_INNT_RED_6G           NUMBER(7)                                   not null,
    IS17_SAK_TYPE_O_S          CHAR(1 char)                                not null,
    IS17_SAK_PROGNOSEGRP       CHAR(1 char)                                not null,
    IS17_DEKNINGSGRAD          NUMBER(3)                                   not null,
    IS17_ANT_STOENADSDAGER     NUMBER(3)                                   not null,
    IS17_TIDL_UTBET            NUMBER(8)                                   not null,
    IS17_TIDL_UTBET_K          CHAR(1 char)                                not null,
    IS17_PROS_INNTEKT_GR       VARCHAR2(3 char)                            not null,
    IS17_ANT_BARN_U_12AAR      CHAR(2 char)                                not null,
    IS17_ALENEOMSORG           CHAR(1 char)                                not null,
    IS17_ADOPSJONS_DATO        VARCHAR2(8 char)                            not null,
    IS17_RETT_TIL_FEDREKVOTE   CHAR(1 char)                                not null,
    IS17_FEDREKVOTE            CHAR(1 char)                                not null,
    IS17_FEDREKVOTE_TOM        VARCHAR2(8 char)                            not null,
    IS17_TIDSYK_OP_PB          NUMBER(5)                                   not null,
    IS17_EGENOPPL              CHAR(1 char)                                not null,
    IS17_ANTATT_SYKM_TOM       VARCHAR2(8 char)                            not null,
    IS17_VEDTAK_12_UKER        VARCHAR2(8 char)                            not null,
    IS17_REG_DATO              VARCHAR2(8 char)                            not null,
    IS17_STILLINGSANDEL_MOR    VARCHAR2(3 char)                            not null,
    IS17_TIDSK_TYPE            CHAR(2 char)                                not null,
    IS17_TIDSK_BARNFNR         VARCHAR2(11 char)                           not null,
    IS17_MAXTIDSK              VARCHAR2(8 char)                            not null,
    IS17_SAMMENFALENDE_PERIODE CHAR(1 char)                                not null,
    IS17_SAMMENF_DAGER_MASK    VARCHAR2(3 char)                            not null,
    IS17_DIAGNOSE_KODE_1       CHAR(1 char)                                not null,
    IS17_DIAGNOSEGRUPPE        VARCHAR2(6 char)                            not null,
    IS17_DIAGNOSE              VARCHAR2(70 char)                           not null,
    IS17_DIAGNOSE_KODE_2       CHAR(1 char)                                not null,
    IS17_DIAGNOSEGRUPPE_2      VARCHAR2(6 char)                            not null,
    IS17_DIAGNOSE_2            VARCHAR2(70 char)                           not null,
    IS17_TERMIN_DATO           VARCHAR2(8 char)                            not null,
    IS17_KJOEP_HELSETJ         CHAR(2 char)                                not null,
    IS17_HELSETJ_SENDT         VARCHAR2(8 char)                            not null,
    IS17_UTREDET_OPERERT       CHAR(1 char)                                not null,
    IS17_UTREDET_OPERERT_DATO  VARCHAR2(8 char)                            not null,
    IS17_REG_DATO_HELSETJ      VARCHAR2(8 char)                            not null,
    IS17_SAMMENHENG_ARB_SIT    CHAR(1 char)                                not null,
    IS17_ARBEIDSTID_MOR        VARCHAR2(3 char)                            not null,
    IS17_SITUASJON_MOR         CHAR(1 char)                                not null,
    IS17_RETTIGHET_MOR         CHAR(1 char)                                not null,
    IS17_OPPHOLD_FOM           NUMBER(8)                                   not null,
    IS17_OPPHOLD_TOM           NUMBER(8)                                   not null,
    IS17_DEL2_TYPE             CHAR(1 char)                                not null,
    IS17_DEL2_REGDATO_J        NUMBER(8)                                   not null,
    IS17_DEL2_REGDATO_U        NUMBER(8)                                   not null,
    IS17_DEL2_DATO             NUMBER(8)                                   not null,
    IS17_FRISKM_DATO           NUMBER(8)                                   not null,
    IS17_SVANGER_SYK           CHAR(1 char)                                not null,
    IS17_PAALOGG_ID            VARCHAR2(7 char)                            not null,
    IS17_UNNTAK_AKTIVITET      CHAR(1 char)                                not null,
    IS17_OPPFOLGING_DATO       NUMBER(8)                                   not null,
    IS17_K68_DATO              NUMBER(8)                                   not null,
    IS17_K69_DATO              NUMBER(8)                                   not null,
    IS17_EOS                   CHAR(1 char)                                not null,
    IS17_ENGANG                CHAR(1 char)                                not null,
    IS17_HALV_HEL              CHAR(1 char)                                not null,
    IS17_K28_DATO              NUMBER(8)                                   not null,
    IS17_AARSAK_FORSKYV        CHAR(2 char)                                not null,
    IS17_STEBARNSADOPSJON      CHAR(1 char)                                not null,
    IS17_SURROGATMOR           CHAR(1 char)                                not null,
    IS17_DIALOG1_DATO          NUMBER(8)                                   not null,
    IS17_DIALOG1_KODE          CHAR(1 char)                                not null,
    IS17_DIALOG2_DATO          NUMBER(8)                                   not null,
    IS17_DIALOG2_KODE          CHAR(1 char)                                not null,
    IS17_OPPFOLGING_KODE       CHAR(1 char)                                not null,
    IS17_K69A_DATO             NUMBER(8)                                   not null,
    IS17_POLIKL_BEH            VARCHAR2(8 char)                            not null,
    IS17_ARENA_F234            CHAR(1 char)                                not null,
    IS17_AVVENT_SYKMELD        CHAR(1 char)                                not null,
    IS17_AVVENT_TOM            NUMBER(8)                                   not null,
    IS17_ARENA_F226            CHAR(1 char)                                not null,
    IS17_ARBKAT_99             CHAR(2 char)                                not null,
    IS17_PB_BEKREFT            CHAR(1 char)                                not null,
    IS17_SANKSJON_FOM          NUMBER(8)                                   not null,
    IS17_SANKSJON_TOM          NUMBER(8)                                   not null,
    IS17_SANKSJONSDAGER        NUMBER(3)                                   not null,
    IS17_SANKSJON_BEKREFTET    CHAR(1 char)                                not null,
    IS17_RETT_TIL_MODREKVOTE   CHAR(1 char)                                not null,
    IS17_MODREKVOTE            CHAR(1 char)                                not null,
    IS17_MODREKVOTE_TOM        VARCHAR2(8 char)                            not null,
    TK_NR                      VARCHAR2(4)                                 not null,
    F_NR                       VARCHAR2(11)                                not null,
    OPPRETTET                  TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE             TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                   VARCHAR2(12 char) default ' '               not null,
    REGION                     CHAR(1 char)      default ' '               not null,
    ID_PERI17                  NUMBER
        constraint PK_IS_PERIODE_17
            primary key,
    OPPDATERT                  TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT                  CHAR(2 char)      default 'FP'
)
;

create table IS_VEDTAK_BARN_18
(
    IS01_PERSONKEY              NUMBER(15)                                  not null,
    IS18_ARBUFOER_SEQ           VARCHAR2(8 char)                            not null,
    IS18_KODE                   CHAR(1 char)                                not null,
    IS18_ARBUFOER               VARCHAR2(8 char)                            not null,
    IS18_STOENADS_TYPE          CHAR(2 char)                                not null,
    IS18_STANS                  VARCHAR2(8 char)                            not null,
    IS18_FRISK                  CHAR(1 char)                                not null,
    IS18_STILLINGSANDEL         VARCHAR2(3 char)                            not null,
    IS18_ARB_TIDS_PROS_GML      CHAR(2 char)                                not null,
    IS18_TIDSK_PROS             NUMBER(4, 2)                                not null,
    IS18_TIDSK_DAGER            VARCHAR2(3 char)                            not null,
    IS18_TIDSK_DAGER_FORB       VARCHAR2(3 char)                            not null,
    IS18_FOM                    VARCHAR2(8 char)                            not null,
    IS18_TOM                    VARCHAR2(8 char)                            not null,
    IS18_DEKNINGSGRAD           VARCHAR2(3 char)                            not null,
    IS18_TIDSK_DISPONIBELT      NUMBER(5, 2)                                not null,
    IS18_TIDSK_HELE_DAGER_FORB  NUMBER(5, 2)                                not null,
    IS18_STOENADSPERIODEN       VARCHAR2(3 char)                            not null,
    IS18_TIDSYK                 VARCHAR2(3 char)                            not null,
    IS18_FARFNR                 VARCHAR2(11 char)                           not null,
    IS18_MORFNR                 VARCHAR2(11 char)                           not null,
    IS18_TOT_TK_DAGER           VARCHAR2(3 char)                            not null,
    IS18_TOT_TK_DAGER_FORB      VARCHAR2(3 char)                            not null,
    IS18_TOT_HELE_TK_DAGER_FORB NUMBER(5, 2)                                not null,
    IS18_REG_DATO               VARCHAR2(8 char)                            not null,
    IS18_TOT_DISPONIBELT        NUMBER(5, 2)                                not null,
    IS18_KOMBI                  CHAR(1 char)                                not null,
    IS18_FATOM                  VARCHAR2(8 char)                            not null,
    IS18_ARB_TIDS_PROS          NUMBER(4, 2)                                not null,
    TK_NR                       VARCHAR2(4)                                 not null,
    F_NR                        VARCHAR2(11)                                not null,
    OPPRETTET                   TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE              TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                    VARCHAR2(12 char) default ' ',
    REGION                      CHAR(1 char)      default ' ',
    ID_VEDBA                    NUMBER
        constraint PK_IS_VEDTAK_BARN
            primary key,
    OPPDATERT                   TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT                   CHAR(2 char)      default 'FP'
)
;

create table IS_STONAD_19
(
    IS01_PERSONKEY             NUMBER(15)                                  not null,
    IS19_FOEDSEL_ADOP_DATO_SEQ VARCHAR2(8 char)                            not null,
    IS19_STOENADS_TYPE         CHAR(2 char)                                not null,
    IS19_FOEDSEL_ADOP_DATO     VARCHAR2(8 char)                            not null,
    IS19_ANT_BARN              CHAR(2 char)                                not null,
    IS19_FNR_MAKKER            VARCHAR2(11 char)                           not null,
    IS19_REG_DATO              VARCHAR2(8 char)                            not null,
    IS19_ARBUFOER_LAVESTE      VARCHAR2(8 char)                            not null,
    IS19_START_FOM             VARCHAR2(8 char)                            not null,
    IS19_SISTE_TOM             VARCHAR2(8 char)                            not null,
    IS19_ADOPSJON_4_UKER       CHAR(2 char)                                not null,
    IS19_SAMMENF_DAGER_MASK    VARCHAR2(3 char)                            not null,
    IS19_SAMMENF_DAGER_TRYGDEK VARCHAR2(3 char)                            not null,
    TK_NR                      VARCHAR2(4)                                 not null,
    F_NR                       VARCHAR2(11)                                not null,
    OPPRETTET                  TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE             TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                   VARCHAR2(12 char) default ' ',
    REGION                     CHAR(1 char)      default ' ',
    ID_STONAD                  NUMBER
        constraint PK_IS_STONAD
            primary key,
    OPPDATERT                  TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT                  CHAR(2 char)      default 'FP'
)
;

create table IF_FORSIKRING_01
(
    IF01_KODE      CHAR(1 char)                           not null,
    IF01_AGNR_FNR  NUMBER(11)                             not null,
    IF01_TKNR      NUMBER(4)                              not null,
    IF01_FOMDATO   NUMBER(8)                              not null,
    IF01_TYPE_DATO CHAR(1 char)                           not null,
    OPPRETTET      TIMESTAMP(6) default current_timestamp not null,
    ENDRET_I_KILDE TIMESTAMP(6) default current_timestamp not null,
    KILDE_IF       VARCHAR2(12 char)                      not null,
    ID_FORS        NUMBER
        constraint PK_IF_FORSIKRING
            primary key,
    OPPDATERT      TIMESTAMP(6) default current_timestamp
)
;

create table IF_VEDFRIVT_10
(
    IF01_KODE          CHAR(1 char)                           not null,
    IF01_AGNR_FNR      NUMBER(11)                             not null,
    IF10_FORSFOM_SEQ   NUMBER(8)                              not null,
    IF10_GODKJ         CHAR(1 char)                           not null,
    IF10_FORSFOM       NUMBER(8)                              not null,
    IF10_VIRKDATO      NUMBER(8)                              not null,
    IF10_TYPE          CHAR(1 char)                           not null,
    IF10_SELVFOM       VARCHAR2(4 char)                       not null,
    IF10_KOMBI         CHAR(1 char)                           not null,
    IF10_PREMGRL       NUMBER(7)                              not null,
    IF10_FOM           NUMBER(8)                              not null,
    IF10_PREMIE        NUMBER(5)                              not null,
    IF10_GML_PREMGRL   NUMBER(7)                              not null,
    IF10_GML_FOM       NUMBER(8)                              not null,
    IF10_GML_PREMIE    NUMBER(5)                              not null,
    IF10_FRIFOM        NUMBER(8)                              not null,
    IF10_FORSTOM       NUMBER(8)                              not null,
    IF10_OPPHGR        VARCHAR2(10 char)                      not null,
    IF10_VARSEL        NUMBER(8)                              not null,
    IF10_TERM_KV       CHAR(1 char)                           not null,
    IF10_TERM_AAR      VARCHAR2(4 char)                       not null,
    IF10_VARSEL_BELOEP NUMBER(5)                              not null,
    IF10_BETALT_BELOEP NUMBER(5)                              not null,
    IF10_PURR          NUMBER(8)                              not null,
    IF10_TKNR_BOST     NUMBER(4)                              not null,
    IF10_TKNR_BEH      NUMBER(4)                              not null,
    OPPRETTET          TIMESTAMP(6) default current_timestamp not null,
    ENDRET_I_KILDE     TIMESTAMP(6) default current_timestamp not null,
    KILDE_IF           VARCHAR2(12 char)                      not null,
    ID_VED             NUMBER
        constraint PK_IF_VEDFRIVT
            primary key,
    OPPDATERT          TIMESTAMP(6) default current_timestamp
)
;


create table BA_BARN_10
(
    B01_PERSONKEY    NUMBER(15)                                  not null,
    B10_BARN_FNR     NUMBER(11)                                  not null,
    B10_BA_IVER      VARCHAR2(6 char)                            not null,
    B10_BA_VFOM      VARCHAR2(6 char)                            not null,
    B10_BA_TOM       VARCHAR2(6 char)                            not null,
    B10_STONADS_TYPE CHAR(2 char)                                not null,
    TK_NR            VARCHAR2(4)                                 not null,
    F_NR             VARCHAR2(11)                                not null,
    OPPRETTET        TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE   TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS         VARCHAR2(12 char) default ' '               not null,
    REGION           CHAR(1 char)      default ' '               not null,
    ID_BA_BARN       NUMBER
        constraint PK_BA_BARN
            primary key,
    OPPDATERT        TIMESTAMP(6)      default current_timestamp not null
)
;

create table BA_PERSON_01
(
    B01_PERSONKEY       NUMBER(15)                                  not null,
    B01_BT_STATUS       CHAR(1 char)                                not null,
    B01_MOTTAKER_KODE   VARCHAR2(3 char)                            not null,
    B01_MOTTAKER_NR     NUMBER(11)                                  not null,
    B01_AKONTO_BELOP    NUMBER(7)                                   not null,
    B01_PENSJONSTRYGDET CHAR(1 char)                                not null,
    TK_NR               VARCHAR2(4)                                 not null,
    F_NR                VARCHAR2(11)                                not null,
    OPPRETTET           TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS            VARCHAR2(12 char) default ' '               not null,
    REGION              CHAR(1 char)      default ' '               not null,
    ID_BA_PERS          NUMBER
        constraint PK_BA_PERSON
            primary key,
    OPPDATERT           TIMESTAMP(6)      default current_timestamp not null
)
;

create table BA_STOENAD_20
(
    B01_PERSONKEY     NUMBER(15)                                  not null,
    B20_IVERFOM_SEQ   VARCHAR2(6 char)                            not null,
    B20_VIRKFOM_SEQ   VARCHAR2(6 char)                            not null,
    B20_GRUPPE        CHAR(2 char)                                not null,
    B20_BRUKERID      VARCHAR2(7 char)                            not null,
    B20_TKNR          VARCHAR2(4 char)                            not null,
    B20_REG_DATO      NUMBER(8)                                   not null,
    B20_SOK_DATO      NUMBER(8)                                   not null,
    B20_BLOKK         CHAR(1 char)                                not null,
    B20_SAK_NR        CHAR(2 char)                                not null,
    B20_STATUS        CHAR(2 char)                                not null,
    B20_TEKSTKODE     CHAR(2 char)                                not null,
    B20_EBET_FOM      VARCHAR2(6 char)                            not null,
    B20_EBET_TOM      VARCHAR2(6 char)                            not null,
    B20_OPPHOERT_IVER VARCHAR2(6 char)                            not null,
    B20_OPPHORSGRUNN  CHAR(1 char)                                not null,
    B20_ANT_BARN      NUMBER(2)                                   not null,
    B20_OMREGN        CHAR(1 char)                                not null,
    B20_EOS           CHAR(1 char)                                not null,
    B20_EKSTRA_SMAB   CHAR(1 char)                                not null,
    B20_SVALBARD      CHAR(1 char)                                not null,
    B20_SAMBOERTYPE   CHAR(1 char)                                not null,
    B20_OPPHOERT_VFOM VARCHAR2(6 char)                            not null,
    B20_ANT_SMAABARN  NUMBER(2)                                   not null,
    B20_DELT_OMS_PROS CHAR(2 char)                                not null,
    TK_NR             VARCHAR2(4)                                 not null,
    F_NR              VARCHAR2(11)                                not null,
    OPPRETTET         TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE    TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS          VARCHAR2(12 char) default ' '               not null,
    REGION            CHAR(1 char)      default ' '               not null,
    ID_BA_STOENAD     NUMBER
        constraint PK_BA_STOENAD
            primary key,
    OPPDATERT         TIMESTAMP(6)      default current_timestamp not null
)
;

create table BA_UTBETALING_30
(
    B01_PERSONKEY           NUMBER(15)                                  not null,
    B30_START_UTBET_MND_SEQ VARCHAR2(6 char)                            not null,
    B30_VFOM_SEQ            VARCHAR2(6 char)                            not null,
    B30_KONTONR             VARCHAR2(8 char)                            not null,
    B30_UTBET_TYPE          CHAR(1 char)                                not null,
    B30_GRUPPE              CHAR(2 char)                                not null,
    B30_BRUKERID            VARCHAR2(7 char)                            not null,
    B30_UTBET_FOM           VARCHAR2(6 char)                            not null,
    B30_UTBET_TOM           VARCHAR2(6 char)                            not null,
    B30_UTBETALT            CHAR(1 char)                                not null,
    B30_BELOP               NUMBER(7)                                   not null,
    B30_UTBET_DATO          NUMBER(8)                                   not null,
    B30_KODE                CHAR(1 char)                                not null,
    TK_NR                   VARCHAR2(4)                                 not null,
    F_NR                    VARCHAR2(11)                                not null,
    OPPRETTET               TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE          TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                VARCHAR2(12 char) default ' '               not null,
    REGION                  CHAR(1 char)      default ' '               not null,
    ID_BA_UTBET             NUMBER
        constraint PK_BA_UTBETALING
            primary key,
    OPPDATERT               TIMESTAMP(6)      default current_timestamp not null
)
;

create table BA_UTBET_HIST_40
(
    B01_PERSONKEY      NUMBER(15)                                  not null,
    B40_UTBET_DATO_SEQ VARCHAR2(8 char)                            not null,
    B40_NETTO_UTBET    NUMBER(7)                                   not null,
    B40_KORT_FRA       VARCHAR2(10 char)                           not null,
    B40_KORT_TIL       VARCHAR2(10 char)                           not null,
    B40_MOTTAKER_KODE  VARCHAR2(3 char)                            not null,
    B40_MOTTAKER_NR    NUMBER(11)                                  not null,
    B40_GIRONR         VARCHAR2(11 char)                           not null,
    B40_UTBET_KODE     VARCHAR2(1 char)                            not null,
    TK_NR              VARCHAR2(4)                                 not null,
    F_NR               VARCHAR2(11)                                not null,
    OPPRETTET          TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE     TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS           VARCHAR2(12 char) default ' '               not null,
    REGION             CHAR(1 char)      default ' '               not null,
    ID_BA_UTHIST       NUMBER
        constraint PK_BA_UTBET_HIST
            primary key,
    OPPDATERT          TIMESTAMP(6)      default current_timestamp not null
)
;

create table KS_PERSON_01
(
    K01_PERSONKEY    NUMBER(15)                                  not null,
    K01_AKONTO_BELOP NUMBER(7)                                   not null,
    TK_NR            VARCHAR2(4)                                 not null,
    F_NR             VARCHAR2(11)                                not null,
    OPPRETTET        TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE   TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS         VARCHAR2(12 char) default ' '               not null,
    REGION           CHAR(1 char)      default ' '               not null,
    ID_PERS          NUMBER
        constraint PK_KS_PERSON
            primary key,
    OPPDATERT        TIMESTAMP(6)      default current_timestamp
)
;

create table KS_BARN_10
(
    K01_PERSONKEY    NUMBER(15)                                  not null,
    K10_BARN_FNR     NUMBER(11)                                  not null,
    K10_BA_IVER_SEQ  VARCHAR2(6 char)                            not null,
    K10_BA_VFOM_SEQ  VARCHAR2(6 char)                            not null,
    K10_BA_TOM_SEQ   VARCHAR2(6 char)                            not null,
    K10_TIMER_PR_UKE CHAR(2 char)                                not null,
    K10_STOTTETYPE   CHAR(2 char)                                not null,
    TK_NR            VARCHAR2(4)                                 not null,
    F_NR             VARCHAR2(11)                                not null,
    OPPRETTET        TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE   TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS         VARCHAR2(12 char) default ' '               not null,
    REGION           CHAR(1 char)      default ' '               not null,
    ID_BARN          NUMBER
        constraint PK_KS_BARN
            primary key,
    OPPDATERT        TIMESTAMP(6)      default current_timestamp
)
;

create table KS_STONAD_20
(
    K01_PERSONKEY      NUMBER(15)                                  not null,
    K20_IVERFOM_SEQ    VARCHAR2(6 char)                            not null,
    K20_VIRKFOM_SEQ    VARCHAR2(6 char)                            not null,
    K20_GRUPPE         CHAR(2 char)                                not null,
    K20_BRUKERID       VARCHAR2(7 char)                            not null,
    K20_TKNR           VARCHAR2(4 char)                            not null,
    K20_REG_DATO       NUMBER(8)                                   not null,
    K20_SOK_DATO       NUMBER(8)                                   not null,
    K20_BLOKK          CHAR(1 char)                                not null,
    K20_SAK_NR         CHAR(2 char)                                not null,
    K20_TEKSTKODE      CHAR(2 char)                                not null,
    K20_TOT_ANT_BARN   CHAR(2 char)                                not null,
    K20_ANT_KS_BARN    CHAR(2 char)                                not null,
    K20_EBET_FOM       VARCHAR2(6 char)                            not null,
    K20_EBET_TOM       VARCHAR2(6 char)                            not null,
    K20_OPPHOERT_IVER  VARCHAR2(6 char)                            not null,
    K20_OPPHOERT_VFOM  VARCHAR2(6 char)                            not null,
    K20_OPPHORSGRUNN   CHAR(1 char)                                not null,
    K20_OMREGN         CHAR(1 char)                                not null,
    K20_EOS            CHAR(1 char)                                not null,
    K20_ADOPTIV_SAK    CHAR(1 char)                                not null,
    K20_ANT_ADOP_BARN  CHAR(1 char)                                not null,
    K20_OPPHOR_ADOPSAK VARCHAR2(6 char)                            not null,
    K20_STATUS_X       CHAR(1 char)                                not null,
    TK_NR              VARCHAR2(4)                                 not null,
    F_NR               VARCHAR2(11)                                not null,
    OPPRETTET          TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE     TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS           VARCHAR2(12 char) default ' '               not null,
    REGION             CHAR(1 char)      default ' '               not null,
    ID_STND            NUMBER
        constraint PK_KS_STONAD
            primary key,
    OPPDATERT          TIMESTAMP(6)      default current_timestamp
)
;

create table KS_UTBETALING_30
(
    K01_PERSONKEY           NUMBER(15)                                  not null,
    K30_START_UTBET_MND_SEQ VARCHAR2(6 char)                            not null,
    K30_VFOM_SEQ            VARCHAR2(6 char)                            not null,
    K30_KONTONR             VARCHAR2(8 char)                            not null,
    K30_UTBET_TYPE          CHAR(1 char)                                not null,
    K30_GRUPPE              CHAR(2 char)                                not null,
    K30_BRUKERID            VARCHAR2(7 char)                            not null,
    K30_UTBET_FOM           VARCHAR2(6 char)                            not null,
    K30_UTBET_TOM           VARCHAR2(6 char)                            not null,
    K30_UTBETALT            CHAR(1 char)                                not null,
    K30_BELOP               NUMBER(7)                                   not null,
    K30_UTBET_DATO          NUMBER(8)                                   not null,
    TK_NR                   VARCHAR2(4)                                 not null,
    F_NR                    VARCHAR2(11)                                not null,
    OPPRETTET               TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE          TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                VARCHAR2(12 char) default ' '               not null,
    REGION                  CHAR(1 char)      default ' '               not null,
    ID_UTBET                NUMBER
        constraint PK_KS_UTBETALING
            primary key,
    OPPDATERT               TIMESTAMP(6)      default current_timestamp
)
;

create table KS_UTBET_HIST_40
(
    K01_PERSONKEY      NUMBER(15)                                  not null,
    K40_UTBET_DATO_SEQ VARCHAR2(8 char)                            not null,
    K40_NETTO_UTBET    NUMBER(7)                                   not null,
    K40_KORT_FRA       VARCHAR2(10 char)                           not null,
    K40_KORT_TIL       VARCHAR2(10 char)                           not null,
    K40_GIRONR         VARCHAR2(11 char)                           not null,
    TK_NR              VARCHAR2(4)                                 not null,
    F_NR               VARCHAR2(11)                                not null,
    OPPRETTET          TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE     TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS           VARCHAR2(12 char) default ' '               not null,
    REGION             CHAR(1 char)      default ' '               not null,
    ID_UHIST           NUMBER
        constraint PK_KS_UTBET_HIST
            primary key,
    OPPDATERT          TIMESTAMP(6)      default current_timestamp
)
;

create table SA_PERSON_01
(
    S01_PERSONKEY  NUMBER(15)                                  not null,
    TK_NR          VARCHAR2(4)                                 not null,
    F_NR           VARCHAR2(11)                                not null,
    OPPRETTET      TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS       VARCHAR2(12 char) default ' '               not null,
    REGION         CHAR(1 char)      default ' '               not null,
    ID_PERS        NUMBER
        constraint PK_SA_PERSON
            primary key,
    OPPDATERT      TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT      CHAR(2 char)      default '  '
)
;

create table SA_SAKSBLOKK_05
(
    S01_PERSONKEY    NUMBER(15)                                  not null,
    S05_SAKSBLOKK    CHAR(1 char)                                not null,
    S05_GRUPPE       CHAR(2 char)                                not null,
    S05_BRUKERID     VARCHAR2(7 char)                            not null,
    S05_MENGDETELLET CHAR(1 char)                                not null,
    TK_NR            VARCHAR2(4)                                 not null,
    F_NR             VARCHAR2(11)                                not null,
    OPPRETTET        TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE   TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS         VARCHAR2(12 char) default ' '               not null,
    REGION           CHAR(1 char)      default ' '               not null,
    ID_SBLK          NUMBER
        constraint PK_SA_SAKSBLOKK
            primary key,
    OPPDATERT        TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT        CHAR(2 char)      default '  '
)
;

create table SA_SAK_10
(
    S01_PERSONKEY          NUMBER(15)                                  not null,
    S05_SAKSBLOKK          CHAR(1 char)                                not null,
    S10_SAKSNR             CHAR(2 char)                                not null,
    S10_REG_DATO           NUMBER(8)                                   not null,
    S10_MOTTATTDATO        NUMBER(8)                                   not null,
    S10_KAPITTELNR         CHAR(2 char)                                not null,
    S10_VALG               CHAR(2 char)                                not null,
    S10_UNDERVALG          CHAR(2 char)                                not null,
    S10_DUBLETT_FEIL       CHAR(1 char)                                not null,
    S10_TYPE               CHAR(2 char)                                not null,
    S10_INNSTILLING        CHAR(2 char)                                not null,
    S10_RESULTAT           CHAR(2 char)                                not null,
    S10_NIVAA              VARCHAR2(3 char)                            not null,
    S10_INNSTILLDATO       NUMBER(8)                                   not null,
    S10_VEDTAKSDATO        NUMBER(8)                                   not null,
    S10_IVERKSATTDATO      NUMBER(8)                                   not null,
    S10_GRUNNBL_DATO       NUMBER(8)                                   not null,
    S10_AARSAKSKODE        CHAR(2 char)                                not null,
    S10_TELLEPUNKT         VARCHAR2(3 char)                            not null,
    S10_TELLETYPE          CHAR(1 char)                                not null,
    S10_TELLEDATO          NUMBER(8)                                   not null,
    S10_EVAL_KODE          VARCHAR2(4 char)                            not null,
    S10_EVAL_TIR           CHAR(1 char)                                not null,
    S10_FREMLEGG           VARCHAR2(3 char)                            not null,
    S10_INNSTILLING2       CHAR(2 char)                                not null,
    S10_INNSTILLDATO2      NUMBER(8)                                   not null,
    S10_ANNEN_INSTANS      CHAR(1 char)                                not null,
    S10_BEHEN_TYPE         VARCHAR2(3 char)                            not null,
    S10_BEHEN_ENHET        VARCHAR2(4 char)                            not null,
    S10_REG_AV_TYPE        VARCHAR2(3 char)                            not null,
    S10_REG_AV_ENHET       VARCHAR2(4 char)                            not null,
    S10_DIFF_FRAMLEGG      VARCHAR2(3 char)                            not null,
    S10_INNSTILLT_AV_TYPE  VARCHAR2(3 char)                            not null,
    S10_INNSTILLT_AV_ENHET VARCHAR2(4 char)                            not null,
    S10_VEDTATT_AV_TYPE    VARCHAR2(3 char)                            not null,
    S10_VEDTATT_AV_ENHET   VARCHAR2(4 char)                            not null,
    S10_PRIO_TAB           VARCHAR2(6 char)                            not null,
    S10_AOE                VARCHAR2(3 char)                            not null,
    S10_ES_SYSTEM          CHAR(1 char)                                not null,
    S10_ES_GSAK_OPPDRAGSID NUMBER(10)                                  not null,
    S10_KNYTTET_TIL_SAK    CHAR(2 char)                                not null,
    S10_VEDTAKSTYPE        CHAR(1 char)                                not null,
    S10_REELL_ENHET        VARCHAR2(4 char)                            not null,
    S10_MOD_ENDRET         CHAR(1 char)                                not null,
    TK_NR                  VARCHAR2(4)                                 not null,
    F_NR                   VARCHAR2(11)                                not null,
    OPPRETTET              TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE         TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS               VARCHAR2(12 char) default ' '               not null,
    REGION                 CHAR(1 char)      default ' '               not null,
    ID_SAK                 NUMBER
        constraint PK_SA_SAK
            primary key,
    OPPDATERT              TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT              CHAR(2 char)      default '  '
)
;

create table SA_STATUS_15
(
    S01_PERSONKEY       NUMBER(15)                                  not null,
    S05_SAKSBLOKK       CHAR(1 char)                                not null,
    S10_SAKSNR          CHAR(2 char)                                not null,
    S15_LOPENR          CHAR(2 char)                                not null,
    S15_BEH_ENHET_TYPE  VARCHAR2(3 char)                            not null,
    S15_BEH_ENHET_ENHET VARCHAR2(4 char)                            not null,
    S15_STATUS          CHAR(2 char)                                not null,
    S15_STATUS_DATO     NUMBER(8)                                   not null,
    S15_BRUKERID        VARCHAR2(7 char)                            not null,
    S15_STATUS_KLOKKE   VARCHAR2(6 char)                            not null,
    S15_STATUS_BRUKERID VARCHAR2(7 char)                            not null,
    S15_ENDRINGS_KODE   CHAR(1 char)                                not null,
    S15_TYPE_GML        CHAR(2 char)                                not null,
    S15_TYPE_NY         CHAR(2 char)                                not null,
    S15_LOVETSVAR_DATO  NUMBER(8)                                   not null,
    S15_ANT_LOFTER      CHAR(2 char)                                not null,
    S15_GRUPPE          CHAR(2 char)                                not null,
    S15_SPERR           CHAR(1 char)                                not null,
    TK_NR               VARCHAR2(4)                                 not null,
    F_NR                VARCHAR2(11)                                not null,
    OPPRETTET           TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS            VARCHAR2(12 char) default ' '               not null,
    REGION              CHAR(1 char)      default ' '               not null,
    ID_STATUS           NUMBER
        constraint PK_SA_STATUS
            primary key,
    OPPDATERT           TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT           CHAR(2 char)      default '  '
)
;



create table SA_HENDELSE_20
(
    S01_PERSONKEY       NUMBER(15)                                  not null,
    S05_SAKSBLOKK       CHAR(1 char)                                not null,
    S20_AKSJONSDATO_SEQ NUMBER(8)                                   not null,
    S20_S_B_KODE        CHAR(1 char)                                not null,
    S20_BREVNUMMER      CHAR(2 char)                                not null,
    S20_MOTTAKERKODE    VARCHAR2(3 char)                            not null,
    S20_MOTTAKERNR      VARCHAR2(11 char)                           not null,
    S20_FNR_ANR_INSTNR  NUMBER(11)                                  not null,
    S20_OPPLYSNING      VARCHAR2(38 char)                           not null,
    S20_ANT_PURREUKER_1 CHAR(2 char)                                not null,
    S20_ANT_PURREUKER_2 CHAR(2 char)                                not null,
    S20_ANT_PURREUKER_3 CHAR(2 char)                                not null,
    S20_S_PURRENR       CHAR(1 char)                                not null,
    S20_1_PURREDATO     NUMBER(8)                                   not null,
    S20_2_PURREDATO     NUMBER(8)                                   not null,
    S20_3_PURREDATO     NUMBER(8)                                   not null,
    S20_RETURDATO       NUMBER(8)                                   not null,
    S20_NESTE_PURREDATO NUMBER(8)                                   not null,
    S20_SAKSNR          CHAR(2 char)                                not null,
    S20_KLADD           CHAR(1 char)                                not null,
    S20_LOPENR_FM01     CHAR(1 char)                                not null,
    S20_LOPENR_PRINT    VARCHAR2(3 char)                            not null,
    S20_SENTRALPRINT    CHAR(1 char)                                not null,
    S20_BREVTYPE        CHAR(1 char)                                not null,
    S20_TEKSTKODE_1     VARCHAR2(4 char)                            not null,
    S20_TEKSTKODE_2     VARCHAR2(4 char)                            not null,
    S20_TEKSTKODE_3     VARCHAR2(4 char)                            not null,
    S20_TEKSTKODE_4     VARCHAR2(4 char)                            not null,
    S20_TEKSTKODE_5     VARCHAR2(4 char)                            not null,
    S20_PURRETEKST_1    VARCHAR2(4 char)                            not null,
    S20_PURRETEKST_2    VARCHAR2(4 char)                            not null,
    S20_PURRETEKST_3    VARCHAR2(4 char)                            not null,
    S20_DATO_DANNET     NUMBER(8)                                   not null,
    S20_LAGER           CHAR(1 char)                                not null,
    S20_REG_AV          VARCHAR2(3 char)                            not null,
    S20_OPPR_TKNR       VARCHAR2(4 char)                            not null,
    S20_OPPR_BRUKERID   VARCHAR2(7 char)                            not null,
    S20_IKKE_AVBRYT     CHAR(1 char)                                not null,
    S20_MAALFORM        CHAR(1 char)                                not null,
    TK_NR               VARCHAR2(4)                                 not null,
    F_NR                VARCHAR2(11)                                not null,
    OPPRETTET           TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE      TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS            VARCHAR2(12 char) default ' '               not null,
    REGION              CHAR(1 char)      default ' '               not null,
    ID_HEND             NUMBER
        constraint PK_SA_HENDELSE
            primary key,
    OPPDATERT           TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT           CHAR(2 char)      default '  '
)
;


create table IP_PERSON_01
(
    IP01_PERSNKEY           RAW(7)                                      not null,
    IP01_PERSONKEY          NUMBER(15)                                  not null,
    IP01_TKAVD              CHAR(1 char)                                not null,
    IP01_DOEDSDATO          NUMBER(8)                                   not null,
    IP01_PENS_STAT          CHAR(1 char)                                not null,
    IP01_SAK_STATUS         CHAR(1 char)                                not null,
    IP01_STATBORGER         CHAR(1 char)                                not null,
    IP01_EKTEF_PENSJ        CHAR(1 char)                                not null,
    IP01_BARNE_PENSJ        CHAR(1 char)                                not null,
    IP01_SP_STAT            CHAR(1 char)                                not null,
    IP01_AL_STAT            CHAR(1 char)                                not null,
    IP01_AF_STAT            CHAR(1 char)                                not null,
    IP01_IN_STAT            CHAR(1 char)                                not null,
    IP01_FORSIKR_STAT       CHAR(1 char)                                not null,
    IP01_MAALFORM           CHAR(1 char)                                not null,
    IP01_AVG_STAT           CHAR(1 char)                                not null,
    IP01_BI_STAT            CHAR(1 char)                                not null,
    IP01_DIV_STAT           CHAR(1 char)                                not null,
    IP01_PERS_STATUS        CHAR(1 char)                                not null,
    IP01_REG_KILDE          CHAR(1 char)                                not null,
    IP01_FORS_DIFF          NUMBER(7)                                   not null,
    IP01_YTELSE             VARCHAR2(2 char)                            not null,
    IP01_POBA_GIRO          NUMBER(11)                                  not null,
    IP01_SAK_TELLER         NUMBER(3)                                   not null,
    IP01_LEVEATTEST_UTGAATT CHAR(1 char)                                not null,
    IP01_AKT_STAT           VARCHAR2(3 char)                            not null,
    IP01_KJOENN             CHAR(1 char)                                not null,
    IP01_SYSTAVD            CHAR(1 char)                                not null,
    IP01_FNR_ENDRET         CHAR(1 char)                                not null,
    IP01_BS_STAT            CHAR(1 char)                                not null,
    IP01_FA_STAT            CHAR(1 char)                                not null,
    IP01_RP_STAT            CHAR(1 char)                                not null,
    TK_NR                   VARCHAR2(4)                                 not null,
    F_NR                    VARCHAR2(11)                                not null,
    OPPRETTET               TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE          TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS                VARCHAR2(12 char) default ' '               not null,
    REGION                  CHAR(1 char)      default ' '               not null,
    ID_IPERS                NUMBER
        constraint PK_IP_PERSON
            primary key,
    OPPDATERT               TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT               CHAR(2 char)      default '99'
)
;

create table IP_PERSONKORT_90
(
    IP01_PERSNKEY        RAW(7)                                      not null,
    IP01_PERSONKEY       NUMBER(15)                                  not null,
    IP90_DATO_SEQ        NUMBER(8)                                   not null,
    IP90_KONTONR         NUMBER(7)                                   not null,
    IP90_DATO            NUMBER(8)                                   not null,
    IP90_BEVILGET_BELOEP NUMBER(9, 2)                                not null,
    IP90_FOM             NUMBER(8)                                   not null,
    IP90_TOM             NUMBER(8)                                   not null,
    IP90_TEKST           VARCHAR2(30 char)                           not null,
    IP90_BEV_PROS        NUMBER(5, 2)                                not null,
    IP90_BETALT_BELOEP   NUMBER(9, 2)                                not null,
    IP90_GRUPPE          CHAR(1 char)                                not null,
    IP90_EIENDOM         CHAR(1 char)                                not null,
    IP90_EIENDOM_KODE    VARCHAR2(2 char)                            not null,
    IP90_OPPDAT_KODE     CHAR(1 char)                                not null,
    TK_NR                VARCHAR2(4)                                 not null,
    F_NR                 VARCHAR2(11)                                not null,
    OPPRETTET            TIMESTAMP(6)      default current_timestamp not null,
    ENDRET_I_KILDE       TIMESTAMP(6)      default current_timestamp not null,
    KILDE_IS             VARCHAR2(12 char) default ' '               not null,
    REGION               CHAR(1 char)      default ' '               not null,
    ID_PERSK             NUMBER
        constraint PK_IP_PERSKORT
            primary key,
    OPPDATERT            TIMESTAMP(6)      default current_timestamp,
    DB_SPLITT            CHAR(2 char)      default '99'
)
;
