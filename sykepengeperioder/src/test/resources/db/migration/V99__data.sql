INSERT INTO IS_PERIODE_10 (IS01_PERSONKEY,
                           TK_NR,
                           IS10_ARBUFOER_SEQ,
                           IS10_ARBUFOER,
                           IS10_ARBUFOER_TOM,
                           IS10_UFOEREGRAD,
                           IS10_MAX,
                           IS10_ARBPER,
                           IS10_FERIE_FOM,
                           IS10_FERIE_TOM,
                           IS10_FERIE_FOM2,
                           IS10_FERIE_TOM2,
                           IS10_STANS,
                           IS10_UNNTAK_AKTIVITET,
                           IS10_ARBKAT,
                           IS10_ARBKAT_99,
                           IS10_SANKSJON_FOM,
                           IS10_SANKSJON_TOM,
                           IS10_SANKSJON_BEKREFTET,
                           IS10_SANKSJONSDAGER,
                           IS10_STOPPDATO,
                           IS10_LEGENAVN,
                           IS10_BEHDATO,
                           IS10_SKADEART,
                           IS10_SKDATO,
                           IS10_SKM_MOTT,
                           F_NR,
                           IS10_FRISK,
-- What??
                           IS10_STOENADS_TYPE,
--Uinteressante påkrevde
                           IS10_STAT,
                           IS10_ARBUFOER_OPPR,
--ID
                           ID_PERI10)
VALUES (111111111145680, 1111, 79111111, 20200819, 20201218, 100, 20210817, 'j', null, null, null, null, '', 'M', '01',
        '', null, null, '', 0, null, 'TROND-VIGGO TORGERSEN', 20200819, '', null, null, 34121456789, null,
-- What??
        '  ',
--Uinteressante påkrevde
        '?', -1,
--ID
        1);

INSERT INTO IS_UTBETALING_15 (IS15_UTBETFOM,
                              IS15_UTBETTOM,
                              IS15_GRAD,
                              IS15_OP,
                              IS15_UTBETDATO,
                              IS15_DSATS,
                              IS15_TYPE,
                              IS15_ARBGIVNR,
                              F_NR,
                              IS10_ARBUFOER_SEQ,
                              IS15_KORR,
                              ID_UTBT,
-- Andre påkrevde
                              IS01_PERSONKEY,
                              IS15_UTBETFOM_SEQ,
                              IS15_BILAG,
                              IS15_TILB_UTBETDATO,
                              IS15_TILB_BILAG,
                              IS15_TILB_OP,
                              IS15_TIDSKONTO_KODE,
                              IS15_BRUKERID,
                              IS15_REGDATO_BATCH,
                              IS15_TILTAK_TYPE,
                              IS15_AARSAK_FORSKYV,
                              IS15_BEREGNET_I_OS,
                              TK_NR,
                              KILDE_IS,
                              REGION)
VALUES (20200904, 20200904, '100', ' ', 20200922, 2176, '5', 70000000, 34121456789, 79111111, 'NOTK', 1,
-- Andre påkrevde
 111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20200905, 20200925, '100', ' ', 20201007, 2176, '5', 70000000, 34121456789, 79111111, 'NOTK', 2,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e');

INSERT INTO IS_INNTEKT_13(IS13_ARBGIVNR,
                          IS13_SPFOM,
                          IS13_REF_TOM,
                          IS13_REF,
                          IS13_PERIODE,
                          IS13_LOENN,
                          F_NR,
                          IS10_ARBUFOER_SEQ,
                          ID_INNT,
-- Andre påkrevde
                          IS01_PERSONKEY,
                          IS13_GML_SPFOM,
                          IS13_GML_LOENN,
                          IS13_GML_PERIODE,
                          IS13_PO_INNT,
                          IS13_UTBET,
                          IS13_TIDSKONTO_KODE,
                          TK_NR,
                          KILDE_IS,
                          REGION)
VALUES (70000000, 20200904, 0, 'J', 'Å', 565700, 34121456789, 79111111, 1,
           -- Andre påkrevde
        111111111145680, 1, 1, 'e', 'e', 'e', 'e', 'tknr', 'ee', 'e');

INSERT INTO IS_DIVERSE_11(F_NR,
                          IS10_ARBUFOER_SEQ,
                          ID_DIV,
-- Statslønn
                          IS11_STLONN,
-- Andre påkrevde
                          IS01_PERSONKEY,
                          IS11_NATURAL,
                          IS11_UTB_NAT,
                          IS11_OPPH_NAT,
                          IS11_REDUKSJ_BELOEP,
                          IS11_REDUKSJ_TYPE,
                          IS11_REDUKSJ_FOM,
                          IS11_REDUKSJ_TOM,
                          TK_NR)
VALUES (34121456789, 79111111, 1,
-- Statslønn
        1000,
-- Andre påkrevde
        111111111145680, 0, 'e', 0, 0, 'e', 0, 0, 'tnkr');

INSERT INTO IS_PERIODE_10 (IS01_PERSONKEY,
                           TK_NR,
                           IS10_ARBUFOER_SEQ,
                           IS10_ARBUFOER,
                           IS10_ARBUFOER_TOM,
                           IS10_UFOEREGRAD,
                           IS10_MAX,
                           IS10_ARBPER,
                           IS10_FERIE_FOM,
                           IS10_FERIE_TOM,
                           IS10_FERIE_FOM2,
                           IS10_FERIE_TOM2,
                           IS10_STANS,
                           IS10_UNNTAK_AKTIVITET,
                           IS10_ARBKAT,
                           IS10_ARBKAT_99,
                           IS10_SANKSJON_FOM,
                           IS10_SANKSJON_TOM,
                           IS10_SANKSJON_BEKREFTET,
                           IS10_SANKSJONSDAGER,
                           IS10_STOPPDATO,
                           IS10_LEGENAVN,
                           IS10_BEHDATO,
                           IS10_SKADEART,
                           IS10_SKDATO,
                           IS10_SKM_MOTT,
                           F_NR,
                           IS10_FRISK,
-- What??
                           IS10_STOENADS_TYPE,
--Uinteressante påkrevde
                           IS10_STAT,
                           IS10_ARBUFOER_OPPR,
--ID
                           ID_PERI10)
VALUES (111111111145680, '2222', 79809999, 20190121, 20190620, '035', 20200115, 'j', null, null, null, null, 'AF', '',
        '01',
        '', null, null, '', 0, 20190621, 'TROND-VIGGO TORGERSEN', 20190121, '', null, null, 34121456789, null,
-- What??
        '  ',
--Uinteressante påkrevde
        '?', -1,
--ID
        2);

INSERT INTO IS_UTBETALING_15 (IS15_UTBETFOM,
                              IS15_UTBETTOM,
                              IS15_GRAD,
                              IS15_OP,
                              IS15_UTBETDATO,
                              IS15_DSATS,
                              IS15_TYPE,
                              IS15_ARBGIVNR,
                              F_NR,
                              IS10_ARBUFOER_SEQ,
                              IS15_KORR,
                              ID_UTBT,
-- Andre påkrevde
                              IS01_PERSONKEY,
                              IS15_UTBETFOM_SEQ,
                              IS15_BILAG,
                              IS15_TILB_UTBETDATO,
                              IS15_TILB_BILAG,
                              IS15_TILB_OP,
                              IS15_TIDSKONTO_KODE,
                              IS15_BRUKERID,
                              IS15_REGDATO_BATCH,
                              IS15_TILTAK_TYPE,
                              IS15_AARSAK_FORSKYV,
                              IS15_BEREGNET_I_OS,
                              TK_NR,
                              KILDE_IS,
                              REGION)
VALUES (20190602, 20190620, '035', ' ', 20190625, 683, '5', 70000000, 34121456789, 79809999, 'NOTK', 3,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20190517, 20190601, '035', ' ', 20190611, 683, '5', 70000000, 34121456789, 79809999, 'NOTK', 4,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20190501, 20190516, '035', ' ', 20190523, 683, '5', 70000000, 34121456789, 79809999, 'NOTK', 5,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20190415, 20190430, '035', ' ', 20190509, 683, '5', 70000000, 34121456789, 79809999, 'NOTK', 6,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20190330, 20190414, '035', ' ', 20190426, 683, '5', 70000000, 34121456789, 79809999, 'NOTK', 7,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20190204, 20190329, '070', ' ', 20190405, 1367, '5', 70000000, 34121456789, 79809999, 'NOTK', 8,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e');

INSERT INTO IS_INNTEKT_13(IS13_ARBGIVNR,
                          IS13_SPFOM,
                          IS13_REF_TOM,
                          IS13_REF,
                          IS13_PERIODE,
                          IS13_LOENN,
                          F_NR,
                          IS10_ARBUFOER_SEQ,
                          ID_INNT,
-- Andre påkrevde
                          IS01_PERSONKEY,
                          IS13_GML_SPFOM,
                          IS13_GML_LOENN,
                          IS13_GML_PERIODE,
                          IS13_PO_INNT,
                          IS13_UTBET,
                          IS13_TIDSKONTO_KODE,
                          TK_NR,
                          KILDE_IS,
                          REGION)
VALUES (70000000, 20190204, 0, 'J', 'Å', 507680, 34121456789, 79809999, 2,
           -- Andre påkrevde
        111111111145680, 1, 1, 'e', 'e', 'e', 'e', 'tknr', 'ee', 'e');
