package no.nav.helse.sparkel.medlemskap

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ResponseParseTest {

    @Test
    fun `ny og gammel response skal tolkes helt likt`() {
        val speilVurdering = MedlemskapClient.parseSvar(jacksonObjectMapper().readTree(speilvurderingResponse))
        val vurdering = MedlemskapClient.parseSvar(jacksonObjectMapper().readTree(vurderingResponse))

        assertEquals(speilVurdering, vurdering)
        assertEquals("JA", speilVurdering.path("resultat").path("svar").asText())
    }

    @Test
    fun `rart svar gir exception`() {
        assertThrows<MedlemskapException> {
            MedlemskapClient.parseSvar(jacksonObjectMapper().readTree(crazyRespone))
        }
    }
}

@Language("JSON")
private val crazyRespone = """{"nøkkel":"verdi"}""".trimIndent()

@Language("JSON")
private val speilvurderingResponse = """
    {
      "soknadId" : "ed0286f6-6107-3d75-8266-e50d5736f403",
      "fnr" : "11111111111",
      "speilSvar" : "JA"
    }
""".trimIndent()

@Language("JSON")
private val vurderingResponse = """
    {
        "kanal": "kafka",
        "resultat": {
            "svar": "JA",
            "dekning": "",
            "regelId": "REGEL_MEDLEM_KONKLUSJON",
            "årsaker": [],
            "avklaring": "Er bruker medlem?",
            "harDekning": null,
            "begrunnelse": "Bruker er medlem",
            "delresultat": [
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_DOED",
                    "årsaker": [],
                    "avklaring": "Er det avklart om brukeren er død eller ikke?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_13",
                            "årsaker": [],
                            "avklaring": "Er bruker død?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_FELLES_ARBEIDSFORHOLD",
                    "årsaker": [],
                    "avklaring": "Er felles regler for arbeidsforhold avklart?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_17",
                            "årsaker": [],
                            "avklaring": "Har bruker arbeidsforhold?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_17_1",
                            "årsaker": [],
                            "avklaring": "Er bruker frilanser?",
                            "harDekning": null,
                            "begrunnelse": "Bruker er ikke frilanser",
                            "delresultat": []
                        },
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_22",
                            "årsaker": [],
                            "avklaring": "Arbeidsforhold siste 12 måneder: finnes det utenlandsopphold i et av disse arbeidsforholdene?",
                            "harDekning": null,
                            "begrunnelse": "Det finnes ikke utenlandsopphold i et av arbeidsforholdene de siste 12 månedene",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_21",
                            "årsaker": [],
                            "avklaring": "Er bruker arbeidstaker i kontrollperiode for stønadsområde?",
                            "harDekning": null,
                            "begrunnelse": "Bruker er arbeidstaker i kontrollperiode for stønadsområde",
                            "delresultat": []
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_STATSBORGERSKAP",
                    "årsaker": [],
                    "avklaring": "Er statsborgerskap avklart?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_2",
                            "årsaker": [],
                            "avklaring": "Er bruker omfattet av grunnforordningen (EØS)? Dvs er bruker statsborger i et EØS-land inkl. Norge?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_11",
                            "årsaker": [],
                            "avklaring": "Er bruker norsk statsborger?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_MEDL",
                    "årsaker": [],
                    "avklaring": "Har bruker avklarte opplysninger i MEDL?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_OPPLYSNINGER",
                            "årsaker": [],
                            "avklaring": "Finnes det registrerte opplysninger på bruker?",
                            "harDekning": null,
                            "begrunnelse": "Alle de følgende ble NEI",
                            "delresultat": [
                                {
                                    "svar": "NEI",
                                    "dekning": "",
                                    "regelId": "REGEL_A",
                                    "årsaker": [],
                                    "avklaring": "Finnes det registrerte opplysninger i MEDL?",
                                    "harDekning": null,
                                    "begrunnelse": "",
                                    "delresultat": []
                                },
                                {
                                    "svar": "NEI",
                                    "dekning": "",
                                    "regelId": "REGEL_C",
                                    "årsaker": [],
                                    "avklaring": "Finnes det dokumenter i JOARK på medlemskapsområdet?",
                                    "harDekning": null,
                                    "begrunnelse": "",
                                    "delresultat": []
                                },
                                {
                                    "svar": "NEI",
                                    "dekning": "",
                                    "regelId": "REGEL_B",
                                    "årsaker": [],
                                    "avklaring": "Finnes det åpne oppgaver i GOSYS på medlemskapsområdet?",
                                    "harDekning": null,
                                    "begrunnelse": "",
                                    "delresultat": []
                                }
                            ]
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_ARBEIDSFORHOLD",
                    "årsaker": [],
                    "avklaring": "Er arbeidsforhold avklart?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_3",
                            "årsaker": [],
                            "avklaring": "Har bruker hatt et sammenhengende arbeidsforhold i Aa-registeret de siste 12 månedene?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_4",
                            "årsaker": [],
                            "avklaring": "Er foretaket registrert i foretaksregisteret?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_14",
                            "årsaker": [],
                            "avklaring": "Er bruker ansatt i staten eller i en kommune?",
                            "harDekning": null,
                            "begrunnelse": "Bruker er ikke ansatt i staten eller i en kommune",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_5",
                            "årsaker": [],
                            "avklaring": "Har arbeidsgiver sin hovedaktivitet i Norge?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_6",
                            "årsaker": [],
                            "avklaring": "Er foretaket aktivt?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_7",
                            "årsaker": [],
                            "avklaring": "Er arbeidsforholdet maritimt?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_8",
                            "årsaker": [],
                            "avklaring": "Er bruker pilot eller kabinansatt?",
                            "harDekning": null,
                            "begrunnelse": "Bruker er ikke pilot eller kabinansatt",
                            "delresultat": []
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_BOSATT",
                    "årsaker": [],
                    "avklaring": "Er det avklart om bruker bor i Norge?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_10",
                            "årsaker": [],
                            "avklaring": "Er bruker folkeregistrert som bosatt i Norge og har vært det i 12 mnd?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        }
                    ]
                },
                {
                    "svar": "JA",
                    "dekning": "",
                    "regelId": "REGEL_NORSK",
                    "årsaker": [],
                    "avklaring": "Er regler for norske borgere avklart?",
                    "harDekning": null,
                    "begrunnelse": "",
                    "delresultat": [
                        {
                            "svar": "NEI",
                            "dekning": "",
                            "regelId": "REGEL_9",
                            "årsaker": [],
                            "avklaring": "Har bruker utført arbeid utenfor Norge?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        },
                        {
                            "svar": "JA",
                            "dekning": "",
                            "regelId": "REGEL_12",
                            "årsaker": [],
                            "avklaring": "Har bruker vært i minst 25% stilling de siste 12 mnd?",
                            "harDekning": null,
                            "begrunnelse": "",
                            "delresultat": []
                        }
                    ]
                }
            ]
        },
        "tidspunkt": "2023-12-01T10:00:00.00000",
        "konklusjon": [
            {
                "dato": "2023-12-01",
                "hvem": "SP6000",
                "status": "JA",
                "lovvalg": null,
                "medlemskap": {
                    "erMedlem": "JA",
                    "ftlHjemmel": ""
                },
                "dekningForSP": "UAVKLART",
                "reglerKjørt": [
                    {
                        "svar": "NEI",
                        "dekning": "",
                        "regelId": "SP6001",
                        "årsaker": [],
                        "avklaring": "Skal regelmotor prosessere gammel kjøring",
                        "harDekning": null,
                        "begrunnelse": "Årsaker i gammel kjøring tilsier ikke at hale skal utføres",
                        "delresultat": [],
                        "utledetInformasjon": []
                    }
                ],
                "avklaringsListe": [],
                "utledetInformasjoner": [
                    {
                        "kilde": [
                            "REGEL_11"
                        ],
                        "informasjon": "NORSK_BORGER"
                    },
                    {
                        "kilde": [
                            "REGEL_2"
                        ],
                        "informasjon": "EØS_BORGER"
                    }
                ]
            }
        ],
        "datagrunnlag": {
            "fnr": "11111111111",
            "ytelse": "SYKEPENGER",
            "periode": {
                "fom": "2023-01-01",
                "tom": "2023-01-31"
            },
            "dokument": [
                {
                    "sak": {
                        "fagsakId": null
                    },
                    "tema": "MED",
                    "tittel": "Automatisk vurdering: Er medlem i folketrygden pr. 15.05.2023",
                    "dokumenter": [
                        {
                            "tittel": "Automatisk vurdering: Er medlem i folketrygden pr. 15.05.2023",
                            "dokumentId": "111111111"
                        }
                    ],
                    "datoOpprettet": "2023-05-01T10:00:00",
                    "journalpostId": "111111111",
                    "journalstatus": "FERDIGSTILT",
                    "journalposttype": "N",
                    "relevanteDatoer": [
                        {
                            "dato": "2023-05-01T10:00:00",
                            "datotype": "DATO_DOKUMENT"
                        },
                        {
                            "dato": "2023-05-01T10:00:00",
                            "datotype": "DATO_JOURNALFOERT"
                        }
                    ],
                    "journalfortAvNavn": "medlemskap:medlemskap-joark-listener"
                },
                {
                    "sak": {
                        "fagsakId": null
                    },
                    "tema": "MED",
                    "tittel": "Automatisk vurdering: Er medlem i folketrygden pr. 13.12.2022",
                    "dokumenter": [
                        {
                            "tittel": "Automatisk vurdering: Er medlem i folketrygden pr. 13.12.2022",
                            "dokumentId": "111111111"
                        }
                    ],
                    "datoOpprettet": "2022-12-31T10:00:00",
                    "journalpostId": "111111111",
                    "journalstatus": "FERDIGSTILT",
                    "journalposttype": "N",
                    "relevanteDatoer": [
                        {
                            "dato": "2022-12-31T10:00:00",
                            "datotype": "DATO_DOKUMENT"
                        },
                        {
                            "dato": "2022-12-31T10:00:00",
                            "datotype": "DATO_JOURNALFOERT"
                        }
                    ],
                    "journalfortAvNavn": "medlemskap:medlemskap-joark-listener"
                }
            ],
            "oppgaver": [],
            "dataOmBarn": [],
            "medlemskap": [],
            "brukerinput": {
                "oppholdUtenforEos": null,
                "oppholdstilatelse": null,
                "arbeidUtenforNorge": false,
                "oppholdUtenforNorge": null,
                "utfortAarbeidUtenforNorge": null
            },
            "arbeidsforhold": [
                {
                    "periode": {
                        "fom": "2000-01-01",
                        "tom": null
                    },
                    "arbeidsgiver": {
                        "ansatte": [
                            {
                                "antall": 11,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                }
                            }
                        ],
                        "konkursStatus": null,
                        "juridiskeEnheter": [
                            {
                                "enhetstype": "AS",
                                "antallAnsatte": 11,
                                "organisasjonsnummer": "111111111"
                            }
                        ],
                        "organisasjonsnummer": "111111111"
                    },
                    "arbeidsavtaler": [
                        {
                            "periode": {
                                "fom": "2000-01-01",
                                "tom": null
                            },
                            "skipstype": null,
                            "yrkeskode": "7125107",
                            "fartsomraade": null,
                            "skipsregister": null,
                            "stillingsprosent": 100.0,
                            "gyldighetsperiode": {
                                "fom": "2000-01-01",
                                "tom": null
                            },
                            "beregnetAntallTimerPrUke": 37.5
                        }
                    ],
                    "arbeidsgivertype": "Organisasjon",
                    "utenlandsopphold": null,
                    "arbeidsforholdstype": "NORMALT",
                    "permisjonPermittering": null
                }
            ],
            "dataOmEktefelle": {
                "arbeidsforholdEktefelle": [
                    {
                        "periode": {
                            "fom": "2000-01-01",
                            "tom": null
                        },
                        "arbeidsgiver": {
                            "ansatte": [
                                {
                                    "antall": 10,
                                    "gyldighetsperiode": {
                                        "fom": "2000-01-01",
                                        "tom": null
                                    }
                                }
                            ],
                            "konkursStatus": null,
                            "juridiskeEnheter": [
                                {
                                    "enhetstype": "AS",
                                    "antallAnsatte": 10,
                                    "organisasjonsnummer": "111111111"
                                }
                            ],
                            "organisasjonsnummer": "111111111"
                        },
                        "arbeidsavtaler": [
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 40.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "beregnetAntallTimerPrUke": 15.0
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 40.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 15.0
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431112",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            },
                            {
                                "periode": {
                                    "fom": "2000-01-01",
                                    "tom": null
                                },
                                "skipstype": null,
                                "yrkeskode": "7431106",
                                "fartsomraade": null,
                                "skipsregister": null,
                                "stillingsprosent": 100.0,
                                "gyldighetsperiode": {
                                    "fom": "2000-01-01",
                                    "tom": "2099-01-01"
                                },
                                "beregnetAntallTimerPrUke": 37.5
                            }
                        ],
                        "arbeidsgivertype": "Organisasjon",
                        "utenlandsopphold": null,
                        "arbeidsforholdstype": "NORMALT",
                        "permisjonPermittering": null
                    }
                ],
                "personhistorikkEktefelle": {
                    "barn": [],
                    "ident": "11111111111",
                    "bostedsadresser": [
                        {
                            "fom": "2000-01-01",
                            "tom": "2099-01-01",
                            "landkode": "NOR",
                            "historisk": true
                        },
                        {
                            "fom": "2000-01-01",
                            "tom": null,
                            "landkode": "NOR",
                            "historisk": false
                        }
                    ],
                    "kontaktadresser": [],
                    "statsborgerskap": [
                        {
                            "fom": null,
                            "tom": null,
                            "landkode": "NOR",
                            "historisk": false
                        }
                    ],
                    "oppholdsadresser": []
                }
            },
            "overstyrteRegler": {},
            "oppholdstillatelse": null,
            "pdlpersonhistorikk": {
                "navn": [
                    {
                        "fornavn": "ANFINN",
                        "etternavn": "FLATABØ",
                        "mellomnavn": null
                    }
                ],
                "doedsfall": [],
                "sivilstand": [
                    {
                        "type": "GIFT",
                        "gyldigFraOgMed": "2000-01-01",
                        "gyldigTilOgMed": null,
                        "relatertVedSivilstand": "11111111111"
                    }
                ],
                "bostedsadresser": [
                    {
                        "fom": "2000-01-01",
                        "tom": "2099-01-01",
                        "landkode": "NOR",
                        "historisk": true
                    },
                    {
                        "fom": "2000-01-01",
                        "tom": null,
                        "landkode": "NOR",
                        "historisk": false
                    }
                ],
                "kontaktadresser": [],
                "statsborgerskap": [
                    {
                        "fom": null,
                        "tom": null,
                        "landkode": "NOR",
                        "historisk": false
                    }
                ],
                "oppholdsadresser": [],
                "utflyttingFraNorge": [],
                "innflyttingTilNorge": [],
                "forelderBarnRelasjon": [
                    {
                        "minRolleForPerson": "BARN",
                        "relatertPersonsIdent": "11111111111",
                        "relatertPersonsRolle": "FAR"
                    },
                    {
                        "minRolleForPerson": "BARN",
                        "relatertPersonsIdent": "11111111111",
                        "relatertPersonsRolle": "MOR"
                    },
                    {
                        "minRolleForPerson": "FAR",
                        "relatertPersonsIdent": "11111111111",
                        "relatertPersonsRolle": "BARN"
                    }
                ]
            },
            "startDatoForYtelse": "2000-01-01",
            "førsteDagForYtelse": "2099-01-01"
        },
        "vurderingsID": "81983d59-e61b-3bed-b664-996d745dda7b",
        "versjonRegler": "v1",
        "versjonTjeneste": "a7a57a9"
    }
""".trimIndent()