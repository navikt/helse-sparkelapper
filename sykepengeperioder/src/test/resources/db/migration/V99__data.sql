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
VALUES (20200905, 20200925, 100, ' ', 20201007, 2176, '5', 70000000, 34121456789, 79111111, 'NOTK', 1,
-- Andre påkrevde
        111111111145680, 1, 1, 1, 1, 'ee', 'f', 'brukeri', 1, 'ee', 'ee', 'e', 'tknr', 'ee', 'e'),
       (20200904, 20200904, 100, ' ', 20200922, 2176, '5', 70000000, 34121456789, 79111111, 'NOTK', 2,
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
VALUES (70000000, 20200904, 0, 'J', 'Å', 565700, 34121456789, 79111111,1,
           -- Andre påkrevde
        111111111145680,1,1,'e','e','e','e','tknr','ee','e');