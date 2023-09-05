package no.nav.helse.sparkel.aareg.arbeidsforhold.util

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.JacksonConverter
import no.nav.helse.sparkel.aareg.objectMapper
import org.intellij.lang.annotations.Language

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
                    "ident": "123456789"
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
