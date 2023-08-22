package no.nav.helse.sparkel.aareg.arbeidsforhold.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import no.nav.helse.sparkel.aareg.objectMapper
import org.intellij.lang.annotations.Language

fun aaregMockClientV1(aaregResponse: String = defaultArbeidsforholdResponseV1()) = HttpClient(MockEngine) {
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/v1/arbeidstaker/arbeidsforhold") -> respond(aaregResponse)
                else -> error("Endepunktet finnes ikke: ${request.url.fullPath}")
            }
        }
    }
}

fun aaregMockClient(aaregResponse: String = defaultArbeidsforholdResponse()) = HttpClient(MockEngine) {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
    expectSuccess = false
    engine {
        addHandler { request ->
            when {
                request.url.fullPath.startsWith("/v2/arbeidstaker/arbeidsforhold") -> respond(
                    content = aaregResponse,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json")
                )
                else -> error("Endepunktet finnes ikke: ${request.url.fullPath}")
            }
        }
    }
}

@Language("JSON")
fun defaultArbeidsforholdResponseV1() = """[
    {
        "ansettelsesperiode": {
            "bruksperiode": {
                "fom": "2015-01-06T21:44:04.748",
                "tom": "2015-12-06T19:45:04"
            },
            "periode": {
                "fom": "2014-07-01",
                "tom": "2015-12-31"
            },
            "sporingsinformasjon": {
                "endretAv": "Z990693",
                "endretKilde": "AAREG",
                "endretKildereferanse": "referanse-fra-kilde",
                "endretTidspunkt": "2018-09-19T12:11:20.79",
                "opprettetAv": "srvappserver",
                "opprettetKilde": "EDAG",
                "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
                "opprettetTidspunkt": "2018-09-19T12:10:58.059"
            },
            "varslingskode": "ERKONK"
        },
        "antallTimerForTimeloennet": [
            {
                "antallTimer": 37.5,
                "periode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "rapporteringsperiode": "2018-05",
                "sporingsinformasjon": {
                    "endretAv": "Z990693",
                    "endretKilde": "AAREG",
                    "endretKildereferanse": "referanse-fra-kilde",
                    "endretTidspunkt": "2018-09-19T12:11:20.79",
                    "opprettetAv": "srvappserver",
                    "opprettetKilde": "EDAG",
                    "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
                    "opprettetTidspunkt": "2018-09-19T12:10:58.059"
                }
            }
        ],
        "arbeidsavtaler": [
            {
                "antallTimerPrUke": 37.5,
                "arbeidstidsordning": "ikkeSkift",
                "beregnetAntallTimerPrUke": 37.5,
                "bruksperiode": {
                    "fom": "2015-01-06T21:44:04.748",
                    "tom": "2015-12-06T19:45:04"
                },
                "gyldighetsperiode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "sistLoennsendring": "string",
                "sistStillingsendring": "string",
                "sporingsinformasjon": {
                    "endretAv": "Z990693",
                    "endretKilde": "AAREG",
                    "endretKildereferanse": "referanse-fra-kilde",
                    "endretTidspunkt": "2018-09-19T12:11:20.79",
                    "opprettetAv": "srvappserver",
                    "opprettetKilde": "EDAG",
                    "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
                    "opprettetTidspunkt": "2018-09-19T12:10:58.059"
                },
                "stillingsprosent": 49.5,
                "yrke": 2130123
            }
        ],
        "arbeidsforholdId": "abc-321",
        "arbeidsgiver": {
            "type": "Organisasjon",
            "organisasjonsnummer": "organisasjonsnummer"
        },
        "arbeidstaker": {
            "type": "Person",
            "aktoerId": 1234567890,
            "offentligIdent": 31126700000
        },
        "innrapportertEtterAOrdningen": true,
        "navArbeidsforholdId": 123456,
        "opplysningspliktig": {
            "type": "Organisasjon",
            "organisasjonsnummer": "organisasjonsnummer"
        },
        "permisjonPermitteringer": [
            {
                "periode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "permisjonPermitteringId": "123-xyz",
                "prosent": 50.5,
                "sporingsinformasjon": {
                    "endretAv": "Z990693",
                    "endretKilde": "AAREG",
                    "endretKildereferanse": "referanse-fra-kilde",
                    "endretTidspunkt": "2018-09-19T12:11:20.79",
                    "opprettetAv": "srvappserver",
                    "opprettetKilde": "EDAG",
                    "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
                    "opprettetTidspunkt": "2018-09-19T12:10:58.059"
                },
                "type": "permisjonMedForeldrepenger",
                "varslingskode": "string"
            }
        ],
        "registrert": "2018-09-18T11:12:29",
        "sistBekreftet": "2018-09-19T12:10:31",
        "sporingsinformasjon": {
            "endretAv": "Z990693",
            "endretKilde": "AAREG",
            "endretKildereferanse": "referanse-fra-kilde",
            "endretTidspunkt": "2018-09-19T12:11:20.79",
            "opprettetAv": "srvappserver",
            "opprettetKilde": "EDAG",
            "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
            "opprettetTidspunkt": "2018-09-19T12:10:58.059"
        },
        "type": "ordinaertArbeidsforhold",
        "utenlandsopphold": [
            {
                "landkode": "JPN",
                "periode": {
                    "fom": "2014-07-01",
                    "tom": "2015-12-31"
                },
                "rapporteringsperiode": "2017-12",
                "sporingsinformasjon": {
                    "endretAv": "Z990693",
                    "endretKilde": "AAREG",
                    "endretKildereferanse": "referanse-fra-kilde",
                    "endretTidspunkt": "2018-09-19T12:11:20.79",
                    "opprettetAv": "srvappserver",
                    "opprettetKilde": "EDAG",
                    "opprettetKildereferanse": "22a26849-aeef-4b81-9174-e238c11e1081",
                    "opprettetTidspunkt": "2018-09-19T12:10:58.059"
                }
            }
        ]
    }
]
"""

@Language("JSON")
fun defaultArbeidsforholdResponse() = """[
    {
        "id": "1",
        "type": {
            "kode": "ordinaertArbeidsforhold",
            "beskrivelse": "Ordinært arbeidsforhold"
        },
        "arbeidstaker": {
            "identer": [
                {
                    "type": "AKTORID",
                    "ident": "2840402937960",
                    "gjeldende": true
                },
                {
                    "type": "FOLKEREGISTERIDENT",
                    "ident": "03856698016",
                    "gjeldende": true
                }
            ]
        },
        "arbeidssted": {
            "type": "Underenhet",
            "identer": [
                {
                    "type": "ORGANISASJONSNUMMER",
                    "ident": "896929119"
                }
            ]
        },
        "opplysningspliktig": {
            "type": "Hovedenhet",
            "identer": [
                {
                    "type": "ORGANISASJONSNUMMER",
                    "ident": "963743254"
                }
            ]
        },
        "ansettelsesperiode": {
            "startdato": "2003-08-03",
            "sluttdato": "2010-08-03"
        },
        "ansettelsesdetaljer": [
            {
                "type": "Ordinaer",
                "arbeidstidsordning": {
                    "kode": "ikkeSkift",
                    "beskrivelse": "Ikke skift"
                },
                "ansettelsesform": {
                    "kode": "fast",
                    "beskrivelse": "Fast ansettelse"
                },
                "yrke": {
                    "kode": "5141103",
                    "beskrivelse": "FRISØR"
                },
                "antallTimerPrUke": 37.5,
                "avtaltStillingsprosent": 100.0,
                "rapporteringsmaaneder": {
                    "fra": "2003-08",
                    "til": null
                }
            }
        ],
        "varsler": [
            {
                "entitet": "Arbeidsforhold",
                "varsling": {
                    "kode": "NAVEND",
                    "beskrivelse": "NAV har opprettet eller endret arbeidsforholdet"
                }
            }
        ],
        "rapporteringsordning": {
            "kode": "A_ORDNINGEN",
            "beskrivelse": "Rapportert via a-ordningen (2015-d.d.)"
        },
        "navArbeidsforholdId": 60510125,
        "navVersjon": 0,
        "navUuid": "04f2c7e0-12fc-4db9-a8a6-7c5298816923",
        "opprettet": "2023-08-03T12:26:28.921",
        "sistBekreftet": "2023-08-03T12:26:28",
        "sistEndret": "2023-08-03T12:26:48",
        "bruksperiode": {
            "fom": "2023-08-03T12:26:28.931",
            "tom": null
        }
    }
]
"""
